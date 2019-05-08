package gyro.aws.waf;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class SizeConstraintSetResource extends AbstractWafResource {
    private String name;
    private String sizeConstraintSetId;
    private List<SizeConstraintResource> sizeConstraint;

    /**
     * The name of the size constraint condition. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ResourceOutput
    public String getSizeConstraintSetId() {
        return sizeConstraintSetId;
    }

    public void setSizeConstraintSetId(String sizeConstraintSetId) {
        this.sizeConstraintSetId = sizeConstraintSetId;
    }

    /**
     * List of size constraint data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.SizeConstraintResource
     */
    @ResourceDiffProperty(updatable = true, subresource = true)
    public List<SizeConstraintResource> getSizeConstraint() {
        if (sizeConstraint == null) {
            sizeConstraint = new ArrayList<>();
        }
        return sizeConstraint;
    }

    public void setSizeConstraint(List<SizeConstraintResource> sizeConstraint) {
        this.sizeConstraint = sizeConstraint;
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public String toDisplayString() {

        StringBuilder sb = new StringBuilder();

        sb.append("size constraint set");

        if (!ObjectUtils.isBlank(getName())) {
            sb.append(" - ").append(getName());
        }

        if (!ObjectUtils.isBlank(getSizeConstraintSetId())) {
            sb.append(" - ").append(getSizeConstraintSetId());
        }

        return sb.toString();
    }
}
