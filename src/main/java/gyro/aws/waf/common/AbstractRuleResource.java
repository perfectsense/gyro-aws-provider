package gyro.aws.waf.common;


import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Output;
import software.amazon.awssdk.services.waf.model.Predicate;

import java.util.List;

public abstract class AbstractRuleResource extends AbstractWafResource {
    private String name;
    private String metricName;
    private String ruleId;

    /**
     * The name of the rule. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The name of the rule. Can only contain letters and numbers. (Required)
     */
    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    @Output
    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public abstract boolean isRateRule();

    protected abstract void loadPredicates(List<Predicate> predicates);

    @Override
    public String toDisplayString() {

        StringBuilder sb = new StringBuilder();

        sb.append("Rule");

        if (isRateRule()) {
            sb.append(" [ Rate Based] ");
        } else {
            sb.append(" [ Regular] ");
        }

        if (!ObjectUtils.isBlank(getName())) {
            sb.append(" - ").append(getName());
        }

        if (!ObjectUtils.isBlank(getRuleId())) {
            sb.append(" - ").append(getRuleId());
        }

        return sb.toString();
    }
}

