package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.ResourceType;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateIpSetResponse;
import software.amazon.awssdk.services.waf.model.GetIpSetResponse;
import software.amazon.awssdk.services.waf.model.IPSet;
import software.amazon.awssdk.services.waf.model.IPSetDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a global ip set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 * aws::ip-set ip-set-example
 *     name: "ip-set-example"
 *
 *     ip-set-descriptor
 *         type: "IPV4"
 *         value: "190.0.0.26/32"
 *     end
 * end
 */
@ResourceType("ip-set")
public class IpSetResource extends gyro.aws.waf.common.IpSetResource {
    private List<IpSetDescriptorResource> ipSetDescriptor;

    /**
     * List of ip set descriptor data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.global.IpSetDescriptorResource
     */
    @Updatable
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

        GetIpSetResponse response = getGlobalClient().getIPSet(
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
        WafClient client = getGlobalClient();

        CreateIpSetResponse response = client.createIPSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setIpSetId(response.ipSet().ipSetId());
    }

    @Override
    public void delete() {
        WafClient client = getGlobalClient();

        client.deleteIPSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .ipSetId(getIpSetId())
        );
    }
}
