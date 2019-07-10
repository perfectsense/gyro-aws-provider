package gyro.aws.waf.global;

import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.IPSetDescriptor;

public class IpSetDescriptorResource extends gyro.aws.waf.common.IpSetDescriptorResource {
    @Override
    protected void saveIpSetDescriptor(IPSetDescriptor ipSetDescriptor, boolean isDelete) {
        WafClient client = getGlobalClient();

        client.updateIPSet(toUpdateIpSetRequest(ipSetDescriptor, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }
}
