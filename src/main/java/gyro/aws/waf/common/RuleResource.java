package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.resource.Resource;
import software.amazon.awssdk.services.waf.model.Rule;

import java.util.Set;

public abstract class RuleResource extends CommonRuleResource implements Copyable<Rule> {
    protected abstract Rule getRule();

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getRuleId())) {
            return false;
        }

        this.copyFrom(getRule());

        return true;
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public boolean isRateRule() {
        return false;
    }

    @Override
    public void copyFrom(Rule rule) {
        setMetricName(rule.metricName());
        setName(rule.name());
        loadPredicates(rule.predicates());
    }
}
