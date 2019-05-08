package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceOutput;
import gyro.core.resource.ResourceUpdatable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class RegexMatchSetResource extends AbstractWafResource {
    private String name;
    private String regexMatchSetId;

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
