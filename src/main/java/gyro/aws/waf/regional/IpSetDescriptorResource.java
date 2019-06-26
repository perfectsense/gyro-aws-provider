package gyro.aws.waf.regional;

import software.amazon.awssdk.services.waf.model.IPSetDescriptor;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

public class IpSetDescriptorResource extends gyro.aws.waf.common.IpSetDescriptorResource {
    @Override
    protected void saveIpSetDescriptor(IPSetDescriptor ipSetDescriptor, boolean isDelete) {
        WafRegionalClient client = getRegionalClient();

        client.updateIPSet(toUpdateIpSetRequest(ipSetDescriptor, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }
}
