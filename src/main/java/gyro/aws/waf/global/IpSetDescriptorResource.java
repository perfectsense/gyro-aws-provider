package gyro.aws.waf.global;

import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.IPSetDescriptor;

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
        WafClient client = getGlobalClient();

        client.updateIPSet(getUpdateIpSetRequest(ipSetDescriptor, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }
}
