package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceOutput;

import java.util.Set;

public abstract class SqlInjectionMatchSetResource extends AbstractWafResource {
    private String name;
    private String sqlInjectionMatchSetId;

    /**
     * The name of the sql injection match condition. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ResourceOutput
    public String getSqlInjectionMatchSetId() {
        return sqlInjectionMatchSetId;
    }

    public void setSqlInjectionMatchSetId(String sqlInjectionMatchSetId) {
        this.sqlInjectionMatchSetId = sqlInjectionMatchSetId;
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("sql injection match set");

        if (!ObjectUtils.isBlank(getName())) {
            sb.append(" - ").append(getName());
        }

        if (!ObjectUtils.isBlank(getSqlInjectionMatchSetId())) {
            sb.append(" - ").append(getSqlInjectionMatchSetId());
        }

        return sb.toString();
    }
}
