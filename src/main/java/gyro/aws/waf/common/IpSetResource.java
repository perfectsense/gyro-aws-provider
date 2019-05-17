package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceOutput;
import java.util.Set;

public abstract class IpSetResource extends AbstractWafResource {
    private String name;
    private String ipSetId;

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
