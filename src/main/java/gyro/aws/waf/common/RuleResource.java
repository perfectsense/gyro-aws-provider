package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Resource;
import software.amazon.awssdk.services.waf.model.Rule;

import java.util.Set;

public abstract class RuleResource extends AbstractRuleResource {
    protected abstract Rule getRule();

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getRuleId())) {
            return false;
        }

        Rule rule = getRule();
        setMetricName(rule.metricName());
        setName(rule.name());
        loadPredicates(rule.predicates());

        return true;
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public boolean isRateRule() {
        return false;
    }
}
