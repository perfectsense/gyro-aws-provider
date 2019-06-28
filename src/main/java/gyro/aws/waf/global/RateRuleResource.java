package gyro.aws.waf.global;

import gyro.core.GyroException;
import gyro.core.resource.Resource;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateRateBasedRuleResponse;
import software.amazon.awssdk.services.waf.model.Predicate;
import software.amazon.awssdk.services.waf.model.RateBasedRule;

import java.util.ArrayList;
import java.util.HashSet;
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
 *     aws::waf-rate-rule rate-rule-example
 *         name: "rate-rule-example"
 *         metric-name: "rateRuleExample"
 *         rate-key: "IP"
 *         rate-limit: 2000
 *
 *         predicate
 *             condition: $(aws::waf-regex-match-set regex-match-set-example-waf)
 *             negated: false
 *         end
 *      end
 */
@Type("waf-rate-rule")
public class RateRuleResource extends gyro.aws.waf.common.RateRuleResource {
    private Set<PredicateResource> predicate;

    /**
     * A set of predicates specifying the connection between rule and conditions.
     *
     * @subresource gyro.aws.waf.global.PredicateResource
     */
    @Updatable
    public Set<PredicateResource> getPredicate() {
        if (predicate == null) {
            predicate = new HashSet<>();
        }

        if (predicate.size() > 10) {
            throw new GyroException("Predicate limit exception. Max 10.");
        }

        return predicate;
    }

    public void setPredicate(Set<PredicateResource> predicate) {
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
    protected void loadPredicates(List<Predicate> predicates) {
        getPredicate().clear();

        for (Predicate predicate: predicates) {
            PredicateResource predicateResource = newSubresource(PredicateResource.class);
            predicateResource.copyFrom(predicate);
            getPredicate().add(predicateResource);
        }
    }
}
