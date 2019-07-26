package gyro.aws.waf.regional;

import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import gyro.core.diff.Context;
import software.amazon.awssdk.services.waf.model.CreateRateBasedRuleResponse;
import software.amazon.awssdk.services.waf.model.Predicate;
import software.amazon.awssdk.services.waf.model.RateBasedRule;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Creates a regional rate-rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::waf-rate-rule-regional rule-example
 *         name: "rate-rule-example"
 *         metric-name: "rateRuleExample"
 *         rate-key: "IP"
 *         rate-limit: 2000
 *
 *         predicate
 *             condition: $(aws::waf-regex-match-set-regional regex-match-set-example-waf)
 *         end
 *    end
 */
@Type("waf-rate-rule-regional")
public class RateRuleResource extends gyro.aws.waf.common.RateRuleResource {
    private Set<PredicateResource> predicate;

    /**
     * A set of predicates specifying the connection between rule and conditions.
     *
     * @subresource gyro.aws.waf.regional.PredicateResource
     */
    @Updatable
    public Set<PredicateResource> getPredicate() {
        if (predicate == null) {
            predicate = new HashSet<>();
        }

        return predicate;
    }

    public void setPredicate(Set<PredicateResource> predicate) {
        this.predicate = predicate;

        if (predicate.size() > 10) {
            throw new GyroException("Predicate limit exception. Max 10.");
        }
    }

    @Override
    protected RateBasedRule getRule() {
        return getRegionalClient().getRateBasedRule(r -> r.ruleId(getRuleId())).rule();
    }

    @Override
    public void create(GyroUI ui, Context context) {
        WafRegionalClient client = getRegionalClient();

        CreateRateBasedRuleResponse response = client.createRateBasedRule(
            r -> r.name(getName())
                .metricName(getMetricName())
                .changeToken(client.getChangeToken().changeToken())
                .rateKey(getRateKey())
                .rateLimit(getRateLimit())
        );

        setRuleId(response.rule().ruleId());
    }

    @Override
    public void update(GyroUI ui, Context context, Resource current, Set<String> changedProperties) {
        WafRegionalClient client = getRegionalClient();

        client.updateRateBasedRule(
            r -> r.ruleId(getRuleId())
                .changeToken(client.getChangeToken().changeToken())
                .rateLimit(getRateLimit())
                .updates(new ArrayList<>())
        );
    }

    @Override
    public void delete(GyroUI ui, Context context) {
        WafRegionalClient client = getRegionalClient();

        client.deleteRateBasedRule(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .ruleId(getRuleId())
        );
    }

    @Override
    protected void loadPredicates(List<Predicate> predicates) {
        getPredicate().clear();

        for (Predicate predicate: predicates) {
            PredicateResource predicateResource = newSubresource(PredicateResource.class);
            predicateResource.copyFrom(predicate);
            getPredicate().add(predicateResource);
        }
    }
}
