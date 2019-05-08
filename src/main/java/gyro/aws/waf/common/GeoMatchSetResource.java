package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class GeoMatchSetResource extends AbstractWafResource {
    private String name;
    private String geoMatchSetId;
    private List<GeoMatchConstraintResource> geoMatchConstraint;

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

    /**
     * List of geo match constraint data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.GeoMatchConstraintResource
     */
    @ResourceDiffProperty(updatable = true, subresource = true)
    public List<GeoMatchConstraintResource> getGeoMatchConstraint() {
        if (geoMatchConstraint == null) {
            geoMatchConstraint = new ArrayList<>();
        }

        return geoMatchConstraint;
    }

    public void setGeoMatchConstraint(List<GeoMatchConstraintResource> geoMatchConstraint) {
        this.geoMatchConstraint = geoMatchConstraint;
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
