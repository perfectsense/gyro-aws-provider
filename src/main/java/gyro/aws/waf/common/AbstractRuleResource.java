package gyro.aws.waf.common;

import gyro.core.resource.Id;
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
     * The metric name of the rule. Can only contain letters and numbers. (Required)
     */
    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    @Id
    @Output
    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public abstract boolean isRateRule();

    protected abstract void loadPredicates(List<Predicate> predicates);

}

