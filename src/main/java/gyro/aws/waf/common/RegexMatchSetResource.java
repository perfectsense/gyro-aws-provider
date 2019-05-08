package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class RegexMatchSetResource extends AbstractWafResource {
    private String name;
    private String regexMatchSetId;
    private List<RegexMatchTupleResource> regexMatchTuple;

    /**
     * The name of the regex match condition. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ResourceOutput
    public String getRegexMatchSetId() {
        return regexMatchSetId;
    }

    public void setRegexMatchSetId(String regexMatchSetId) {
        this.regexMatchSetId = regexMatchSetId;
    }

    /**
     * List of regex match tuple data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.RegexMatchTupleResource
     */
    @ResourceDiffProperty(updatable = true, subresource = true)
    public List<RegexMatchTupleResource> getRegexMatchTuple() {
        if (regexMatchTuple == null) {
            regexMatchTuple = new ArrayList<>();
        }

        return regexMatchTuple;
    }

    public void setRegexMatchTuple(List<RegexMatchTupleResource> regexMatchTuple) {
        this.regexMatchTuple = regexMatchTuple;
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("regex match set");

        if (!ObjectUtils.isBlank(getName())) {
            sb.append(" - ").append(getName());
        }

        if (!ObjectUtils.isBlank(getRegexMatchSetId())) {
            sb.append(" - ").append(getRegexMatchSetId());
        }

        return sb.toString();
    }
}
