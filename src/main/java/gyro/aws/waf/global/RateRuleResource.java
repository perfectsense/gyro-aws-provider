package gyro.aws.waf.global;

import gyro.core.resource.Resource;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceUpdatable;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateRateBasedRuleResponse;
import software.amazon.awssdk.services.waf.model.Predicate;
import software.amazon.awssdk.services.waf.model.RateBasedRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Creates a global rate-rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::rate-rule rate-rule-example
 *          name: "rate-rule-example"
 *          metric-name: "rateRuleExample"
 *          rate-key: "IP"
 *          rate-limit: 2000
 *      end
 */
@ResourceType("rate-rule")
public class RateRuleResource extends gyro.aws.waf.common.RateRuleResource {
    private List<PredicateResource> predicate;

    /**
     * A list of predicates specifying the connection between rule and conditions.
     *
     * @subresource gyro.aws.waf.PredicateResource
     */
    @ResourceUpdatable
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
        return getGlobalClient().getRateBasedRule(r -> r.ruleId(getRuleId())).rule();
    }

    @Override
    public void create() {
        WafClient client = getGlobalClient();

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
        WafClient client = getGlobalClient();

        client.updateRateBasedRule(
            r -> r.ruleId(getRuleId())
                .changeToken(client.getChangeToken().changeToken())
                .rateLimit(getRateLimit())
                .updates(new ArrayList<>())
        );
    }

    @Override
    public void delete() {
        WafClient client = getGlobalClient();

        client.deleteRateBasedRule(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .ruleId(getRuleId())
        );
    }

    @Override
    protected void loadPredicates(List<Predicate> predicates, boolean isRateRule) {
        getPredicate().clear();

        for (Predicate predicate: predicates) {
            PredicateResource predicateResource = new PredicateResource(predicate, isRateRule);
            getPredicate().add(predicateResource);
        }
    }
}
