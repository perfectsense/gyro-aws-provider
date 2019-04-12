package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.core.GyroException;
import gyro.core.diff.ResourceName;
import gyro.lang.Resource;
import com.psddev.dari.util.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.IPSetDescriptor;
import software.amazon.awssdk.services.waf.model.IPSetUpdate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@ResourceName(parent = "ip-set", value = "ip-set-descriptor")
public class IpSetDescriptorResource extends AwsResource {
    private String value;
    private String type;

    /**
     * The ip to be filtered on. (Required)
     */
    public String getValue() {
        return getCidrValue();
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * The type of ip provided. Valid values are ``IPV4`` or ``IPV6``. (Required)
     */
    public String getType() {
        return type != null ? type.toUpperCase() : null;
    }

    public void setType(String type) {
        this.type = type;
    }

    public IpSetDescriptorResource() {

    }

    public IpSetDescriptorResource(IPSetDescriptor ipSetDescriptor) {
        setType(ipSetDescriptor.typeAsString());
        setValue(ipSetDescriptor.value());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        saveIpSetDescriptor(client, getIpSetDescriptor(), false);
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        saveIpSetDescriptor(client, getIpSetDescriptor(), true);
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("ip set descriptor");

        if (!ObjectUtils.isBlank(getType())) {
            sb.append(" - ").append(getType());
        }

        if (!ObjectUtils.isBlank(getValue())) {
            sb.append(" - ").append(getValue());
        }

        return sb.toString();
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s", getType(), getValue());
    }

    @Override
    public String resourceIdentifier() {
        return null;
    }

    private IPSetDescriptor getIpSetDescriptor() {
        return IPSetDescriptor.builder()
            .type(getType())
            .value(getValue())
            .build();
    }

    private void saveIpSetDescriptor(WafClient client, IPSetDescriptor ipSetDescriptor, boolean isDelete) {
        IpSetResource parent = (IpSetResource) parent();

        IPSetUpdate ipSetUpdate = IPSetUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .ipSetDescriptor(ipSetDescriptor)
            .build();

        client.updateIPSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .ipSetId(parent.getIpSetId())
                .updates(ipSetUpdate)
        );
    }

    private String getCidrValue() {
        String cidr = value;

        boolean isValid = false;

        if (!ObjectUtils.isBlank(cidr) && cidr.split("/").length == 2) {
            isValid = true;
        }

        if (isValid) {
            isValid = false;
            String ip = cidr.split("/")[0];
            String range = cidr.split("/")[1];

            if (getType().equals("IPV4")) {
                isValid = (range.equals("8") || range.equals("16") || range.equals("24") || range.equals("32"));
            } else if (getType().equals("IPV6")) {
                isValid = (range.equals("24") || range.equals("32")
                    || range.equals("48") || range.equals("56") || range.equals("64") || range.equals("128"));
            }

            if (isValid) {
                try {
                    InetAddress byName = InetAddress.getByName(ip);
                    String hostAddress = byName.getHostAddress();
                    if (getType().equals("IPV6")) {
                        isValid = !ip.contains(".") && ip.contains(":");
                        ip = Arrays.stream(hostAddress.split(":")).map(o -> StringUtils.leftPad(o, 4, "0")).collect(Collectors.joining(":"));
                    } else if (getType().equals("IPV4")) {
                        isValid = ip.contains(".") && !ip.contains(":");
                    }

                    cidr = ip + "/" + range;
                } catch (UnknownHostException ex) {
                    isValid = false;
                }
            }

            if (!isValid) {
                throw new GyroException(String.format("Invalid cidr - %s of type %s.", value, getType()));
            }
        }

        return cidr;
    }
}
