package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;

import java.util.Set;

public abstract class SizeConstraintSetResource extends AbstractWafResource {
    private String name;
    private String sizeConstraintSetId;

    /**
     * The name of the size constraint condition. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Output
    public String getSizeConstraintSetId() {
        return sizeConstraintSetId;
    }

    public void setSizeConstraintSetId(String sizeConstraintSetId) {
        this.sizeConstraintSetId = sizeConstraintSetId;
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
