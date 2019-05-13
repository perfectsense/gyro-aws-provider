package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceUpdatable;
import software.amazon.awssdk.services.waf.model.CreateIpSetResponse;
import software.amazon.awssdk.services.waf.model.GetIpSetResponse;
import software.amazon.awssdk.services.waf.model.IPSet;
import software.amazon.awssdk.services.waf.model.IPSetDescriptor;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a regional IP match set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 * aws::ip-set-regional ip-set-example
 *     name: "ip-set-example"
 *
 *     ip-set-descriptor
 *         type: "IPV4"
 *         value: "190.0.0.26/32"
 *     end
 * end
 */
@ResourceType("ip-set-regional")
public class IpSetResource extends gyro.aws.waf.common.IpSetResource {
    private List<IpSetDescriptorResource> ipSetDescriptor;

    /**
     * List of ip set descriptor data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.regional.IpSetDescriptorResource
     */
    @ResourceUpdatable
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

        GetIpSetResponse response = getRegionalClient().getIPSet(
            r -> r.ipSetId(getIpSetId())
        );

        IPSet ipSet = response.ipSet();
        setName(ipSet.name());

        getIpSetDescriptor().clear();
        for (IPSetDescriptor ipSetDescriptor : ipSet.ipSetDescriptors()) {
            IpSetDescriptorResource ipSetDescriptorResource = new IpSetDescriptorResource(ipSetDescriptor);
            getIpSetDescriptor().add(ipSetDescriptorResource);
        }

        return true;
    }

    @Override
    public void create() {
        WafRegionalClient client = getRegionalClient();

        CreateIpSetResponse response = client.createIPSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setIpSetId(response.ipSet().ipSetId());
    }

    @Override
    public void delete() {
        WafRegionalClient client = getRegionalClient();

        client.deleteIPSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .ipSetId(getIpSetId())
        );
    }
}
