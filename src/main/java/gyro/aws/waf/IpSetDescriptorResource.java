package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.core.diff.ResourceName;
import gyro.lang.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.IPSetDescriptor;
import software.amazon.awssdk.services.waf.model.IPSetUpdate;

import java.util.Set;

@ResourceName(parent = "ip-set", value = "ip-set-descriptor")
public class IpSetDescriptorResource extends AwsResource {
    private String value;
    private String type;

    /**
     * The ip to be filtered on. (Required)
     */
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * The type of ip provided. Valid values ```IPV4``` or ```IPV6```. (Required)
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
}
