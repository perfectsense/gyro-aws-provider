package gyro.aws.waf;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class IpSetResource extends AbstractWafResource {
    private String name;
    private String ipSetId;
    private List<IpSetDescriptorResource> ipSetDescriptor;

    /**
     * The name of the ip set condition. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ResourceOutput
    public String getIpSetId() {
        return ipSetId;
    }

    public void setIpSetId(String ipSetId) {
        this.ipSetId = ipSetId;
    }

    /**
     * List of ip set descriptor data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.IpSetDescriptorResource
     */
    @ResourceDiffProperty(updatable = true, subresource = true)
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
    public void update(Resource current, Set<String> changedProperties) {

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
