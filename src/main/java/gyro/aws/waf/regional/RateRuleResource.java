package gyro.aws.waf.regional;

import gyro.core.resource.Resource;
import gyro.core.resource.ResourceType;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.waf.model.CreateRateBasedRuleResponse;
import software.amazon.awssdk.services.waf.model.Predicate;
import software.amazon.awssdk.services.waf.model.RateBasedRule;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
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
 *    aws::rate-rule-regional rate-rule-example
 *         name: "rate-rule-example"
 *         metric-name: "rateRuleExample"
 *         rate-key: "IP"
 *         rate-limit: 2000
 *    end
 */
@ResourceType("rate-rule-regional")
public class RateRuleResource extends gyro.aws.waf.common.RateRuleResource {
    private List<PredicateResource> predicate;

    /**
     * A list of predicates specifying the connection between rule and conditions.
     *
     * @subresource gyro.aws.waf.PredicateResource
     */
    @Updatable
    public List<PredicateResource> getPredicate() {
        if (predicate == null) {
            predicate = new ArrayList<>();
        }

        return predicate;
    }

    public void setPredicate(List<PredicateResource> predicate) {
        this.predicate = predicate;
    }

    @Override
    protected RateBasedRule getRule() {
        return getRegionalClient().getRateBasedRule(r -> r.ruleId(getRuleId())).rule();
    }

    @Override
    public void create() {
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
    public void update(Resource current, Set<String> changedProperties) {
        WafRegionalClient client = getRegionalClient();

        client.updateRateBasedRule(
            r -> r.ruleId(getRuleId())
                .changeToken(client.getChangeToken().changeToken())
                .rateLimit(getRateLimit())
                .updates(new ArrayList<>())
        );
    }

    @Override
    public void delete() {
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
            PredicateResource predicateResource = new PredicateResource(predicate);
            getPredicate().add(predicateResource);
        }
    }
}
