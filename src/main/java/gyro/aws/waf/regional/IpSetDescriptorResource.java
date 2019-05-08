package gyro.aws.waf.regional;

import software.amazon.awssdk.services.waf.model.IPSetDescriptor;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

//@ResourceName(parent = "ip-set", value = "ip-set-descriptor")
public class IpSetDescriptorResource extends gyro.aws.waf.common.IpSetDescriptorResource {
    public IpSetDescriptorResource() {

    }

    public IpSetDescriptorResource(IPSetDescriptor ipSetDescriptor) {
        setType(ipSetDescriptor.typeAsString());
        setValue(ipSetDescriptor.value());
    }

    @Override
    protected void saveIpSetDescriptor(IPSetDescriptor ipSetDescriptor, boolean isDelete) {
        WafRegionalClient client = getRegionalClient();

        client.updateIPSet(getUpdateIpSetRequest(ipSetDescriptor, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }
}
