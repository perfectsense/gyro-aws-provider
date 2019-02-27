package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.core.diff.ResourceDiffProperty;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.waf.model.Predicate;

import java.util.ArrayList;
import java.util.List;

public abstract class RuleBaseResource extends AwsResource {
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

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    abstract boolean isRateRule();

    abstract List<PredicateResource> getPredicate();

    protected void loadPredicates(List<Predicate> predicates) {
        getPredicate().clear();

        for (Predicate predicate: predicates) {
            PredicateResource predicateResource = new PredicateResource(predicate, isRateRule());
            predicateResource.parent(this);
            getPredicate().add(predicateResource);
        }
    }

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
