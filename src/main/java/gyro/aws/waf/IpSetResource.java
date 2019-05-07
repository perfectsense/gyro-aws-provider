package gyro.aws.waf;

import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import gyro.core.resource.ResourceOutput;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateIpSetResponse;
import software.amazon.awssdk.services.waf.model.GetIpSetResponse;
import software.amazon.awssdk.services.waf.model.IPSet;
import software.amazon.awssdk.services.waf.model.IPSetDescriptor;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Creates a ip set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::ip-set ip-set-example
 *         name: "ip-set-example"
 *
 *         ip-set-descriptor
 *             type: "IPV4"
 *             value: "190.0.0.26/32"
 *         end
 *     end
 */
@ResourceName("ip-set")
public class IpSetResource extends AbstractWafResource {
    private String name;
    private String ipSetId;
    private List<IpSetDescriptorResource> ipSetDescriptor;

    /**
     * The name of the ip set condition. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ResourceOutput
    public String getIpSetId() {
        return ipSetId;
    }

    public void setIpSetId(String ipSetId) {
        this.ipSetId = ipSetId;
    }

    /**
     * List of ip set descriptor data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.IpSetDescriptorResource
     */
    @ResourceDiffProperty(updatable = true, subresource = true)
    public List<IpSetDescriptorResource> getIpSetDescriptor() {
        if (ipSetDescriptor == null) {
            ipSetDescriptor = new ArrayList<>();
        }

        return ipSetDescriptor;
    }

    public void setIpSetDescriptor(List<IpSetDescriptorResource> ipSetDescriptor) {
        this.ipSetDescriptor = ipSetDescriptor;
    }

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getIpSetId())) {
            return false;
        }

        GetIpSetResponse response;

        if (getRegionalWaf()) {
            response = getRegionalClient().getIPSet(
                r -> r.ipSetId(getIpSetId())
            );
        } else {
            response = getGlobalClient().getIPSet(
                r -> r.ipSetId(getIpSetId())
            );
        }

        IPSet ipSet = response.ipSet();
        setName(ipSet.name());

        getIpSetDescriptor().clear();
        for (IPSetDescriptor ipSetDescriptor : ipSet.ipSetDescriptors()) {
            IpSetDescriptorResource ipSetDescriptorResource = new IpSetDescriptorResource(ipSetDescriptor);
            ipSetDescriptorResource.parent(this);
            getIpSetDescriptor().add(ipSetDescriptorResource);
        }

        return true;
    }

    @Override
    public void create() {
        CreateIpSetResponse response;

        if (getRegionalWaf()) {
            WafRegionalClient client = getRegionalClient();

            response = client.createIPSet(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .name(getName())
            );
        } else {
            WafClient client = getGlobalClient();

            response = client.createIPSet(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .name(getName())
            );
        }

        IPSet ipSet = response.ipSet();
        setIpSetId(ipSet.ipSetId());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        if (getRegionalWaf()) {
            WafRegionalClient client = getRegionalClient();

            client.deleteIPSet(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .ipSetId(getIpSetId())
            );
        } else {
            WafClient client = getGlobalClient();

            client.deleteIPSet(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .ipSetId(getIpSetId())
            );
        }
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("ip set");

        if (!ObjectUtils.isBlank(getName())) {
            sb.append(" - ").append(getName());
        }

        if (!ObjectUtils.isBlank(getIpSetId())) {
            sb.append(" - ").append(getIpSetId());
        }

        return sb.toString();
    }
}
