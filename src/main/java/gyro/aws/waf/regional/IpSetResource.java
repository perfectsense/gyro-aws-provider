package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.waf.model.CreateIpSetResponse;
import software.amazon.awssdk.services.waf.model.GetIpSetResponse;
import software.amazon.awssdk.services.waf.model.IPSet;
import software.amazon.awssdk.services.waf.model.IPSetDescriptor;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

//@ResourceName("ip-set")
public class IpSetResource extends gyro.aws.waf.common.IpSetResource {
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
            ipSetDescriptorResource.parent(this);
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
