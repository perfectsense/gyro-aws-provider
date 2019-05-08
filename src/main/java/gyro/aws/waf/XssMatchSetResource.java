package gyro.aws.waf;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class XssMatchSetResource extends AbstractWafResource {
    private String name;
    private String xssMatchSetId;
    private List<XssMatchTupleResource> xssMatchTuple;

    /**
     * The name of the xss match condition. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ResourceOutput
    public String getXssMatchSetId() {
        return xssMatchSetId;
    }

    public void setXssMatchSetId(String xssMatchSetId) {
        this.xssMatchSetId = xssMatchSetId;
    }

    /**
     * List of xss match tuple data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.XssMatchTupleResource
     */
    @ResourceDiffProperty(updatable = true, subresource = true)
    public List<XssMatchTupleResource> getXssMatchTuple() {
        if (xssMatchTuple == null) {
            xssMatchTuple = new ArrayList<>();
        }

        return xssMatchTuple;
    }

    public void setXssMatchTuple(List<XssMatchTupleResource> xssMatchTuple) {
        this.xssMatchTuple = xssMatchTuple;
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("xss match set");

        if (!ObjectUtils.isBlank(getName())) {
            sb.append(" - ").append(getName());
        }

        if (!ObjectUtils.isBlank(getXssMatchSetId())) {
            sb.append(" - ").append(getXssMatchSetId());
        }

        return sb.toString();
    }
}
