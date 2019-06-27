package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.waf.model.CreateIpSetResponse;
import software.amazon.awssdk.services.waf.model.GetIpSetResponse;
import software.amazon.awssdk.services.waf.model.IPSet;
import software.amazon.awssdk.services.waf.model.IPSetDescriptor;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.HashSet;
import java.util.Set;

/**
 * Creates a regional IP match set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 * aws::waf-ip-set-regional ip-set-example
 *     name: "ip-set-example"
 *
 *     ip-set-descriptor
 *         type: "IPV4"
 *         value: "190.0.0.26/32"
 *     end
 * end
 */
@Type("waf-ip-set-regional")
public class IpSetResource extends gyro.aws.waf.common.IpSetResource {
    private Set<IpSetDescriptorResource> ipSetDescriptor;

    /**
     * List of ip set descriptor data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.regional.IpSetDescriptorResource
     */
    @Updatable
    public Set<IpSetDescriptorResource> getIpSetDescriptor() {
        if (ipSetDescriptor == null) {
            ipSetDescriptor = new HashSet<>();
        }

        return ipSetDescriptor;
    }

    public void setIpSetDescriptor(Set<IpSetDescriptorResource> ipSetDescriptor) {
        this.ipSetDescriptor = ipSetDescriptor;
    }

    @Override
    public void copyFrom(IPSet ipSet) {
        setId(ipSet.ipSetId());
        setName(ipSet.name());

        getIpSetDescriptor().clear();
        for (IPSetDescriptor ipSetDescriptor : ipSet.ipSetDescriptors()) {
            IpSetDescriptorResource ipSetDescriptorResource = newSubresource(IpSetDescriptorResource.class);
            ipSetDescriptorResource.copyFrom(ipSetDescriptor);
            getIpSetDescriptor().add(ipSetDescriptorResource);
        }
    }

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getId())) {
            return false;
        }

        GetIpSetResponse response = getRegionalClient().getIPSet(
            r -> r.ipSetId(getId())
        );

        this.copyFrom(response.ipSet());

        return true;
    }

    @Override
    public void create() {
        WafRegionalClient client = getRegionalClient();

        CreateIpSetResponse response = client.createIPSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setId(response.ipSet().ipSetId());
    }

    @Override
    public void delete() {
        WafRegionalClient client = getRegionalClient();

        client.deleteIPSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .ipSetId(getId())
        );
    }
}
