package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.core.diff.ResourceName;
import gyro.lang.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateIpSetResponse;
import software.amazon.awssdk.services.waf.model.GetIpSetResponse;
import software.amazon.awssdk.services.waf.model.IPSet;
import software.amazon.awssdk.services.waf.model.IPSetDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ResourceName("ip-set")
public class IpSetResource extends AwsResource {
    private String name;
    private String ipSetId;
    private List<IpSetDescriptorResource> ipSetDescriptor;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpSetId() {
        return ipSetId;
    }

    public void setIpSetId(String ipSetId) {
        this.ipSetId = ipSetId;
    }

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

        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        GetIpSetResponse response = client.getIPSet(
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
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        CreateIpSetResponse response = client.createIPSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        IPSet ipSet = response.ipSet();
        setIpSetId(ipSet.ipSetId());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        client.deleteIPSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .ipSetId(getIpSetId())
        );
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
