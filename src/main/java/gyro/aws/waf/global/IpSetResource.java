package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateIpSetResponse;
import software.amazon.awssdk.services.waf.model.GetIpSetResponse;
import software.amazon.awssdk.services.waf.model.IPSet;
import software.amazon.awssdk.services.waf.model.IPSetDescriptor;

//@ResourceName("ip-set")
public class IpSetResource extends gyro.aws.waf.common.IpSetResource {

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
            ipSetDescriptorResource.parent(this);
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
