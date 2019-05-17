package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceOutput;

import java.util.Set;

public abstract class GeoMatchSetResource extends AbstractWafResource {
    private String name;
    private String geoMatchSetId;

    /**
     * The name of the geo match condition. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ResourceOutput
    public String getGeoMatchSetId() {
        return geoMatchSetId;
    }

    public void setGeoMatchSetId(String geoMatchSetId) {
        this.geoMatchSetId = geoMatchSetId;
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("geo match set");

        if (!ObjectUtils.isBlank(getName())) {
            sb.append(" - ").append(getName());
        }

        if (!ObjectUtils.isBlank(getGeoMatchSetId())) {
            sb.append(" - ").append(getGeoMatchSetId());
        }

        return sb.toString();
    }
}
