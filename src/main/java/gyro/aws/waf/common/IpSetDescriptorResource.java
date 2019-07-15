package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import org.apache.commons.lang.StringUtils;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.IPSetDescriptor;
import software.amazon.awssdk.services.waf.model.IPSetUpdate;
import software.amazon.awssdk.services.waf.model.UpdateIpSetRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class IpSetDescriptorResource extends AbstractWafResource implements Copyable<IPSetDescriptor> {
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

    @Override
    public void copyFrom(IPSetDescriptor ipSetDescriptor) {
        setType(ipSetDescriptor.typeAsString());
        setValue(ipSetDescriptor.value());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(State state) {
        saveIpSetDescriptor(toIpSetDescriptor(), false);
    }

    @Override
    public void update(State state, Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete(State state) {
        saveIpSetDescriptor(toIpSetDescriptor(), true);

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

    protected abstract void saveIpSetDescriptor(IPSetDescriptor ipSetDescriptor, boolean isDelete);

    private IPSetDescriptor toIpSetDescriptor() {
        return IPSetDescriptor.builder()
            .type(getType())
            .value(getValue())
            .build();
    }

    protected UpdateIpSetRequest.Builder toUpdateIpSetRequest(IPSetDescriptor ipSetDescriptor, boolean isDelete) {
        IpSetResource parent = (IpSetResource) parent();

        IPSetUpdate ipSetUpdate = IPSetUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .ipSetDescriptor(ipSetDescriptor)
            .build();

        return UpdateIpSetRequest.builder()
            .ipSetId(parent.getId())
            .updates(ipSetUpdate);
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
