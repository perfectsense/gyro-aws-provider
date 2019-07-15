package gyro.aws.waf.global;

import gyro.core.GyroException;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateRuleResponse;
import software.amazon.awssdk.services.waf.model.Predicate;
import software.amazon.awssdk.services.waf.model.Rule;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Creates a global rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::waf-rule rule-example
 *         name: "rule-example"
 *         metric-name: "ruleExample"
 *
 *         predicate
 *             condition: $(aws::waf-regex-match-set regex-match-set-example-waf)
 *         end
 *     end
 */
@Type("waf-rule")
public class RuleResource extends gyro.aws.waf.common.RuleResource {
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

        return predicate;
    }

    public void setPredicate(Set<PredicateResource> predicate) {
        this.predicate = predicate;

        if (predicate.size() > 10) {
            throw new GyroException("Predicate limit exception. Max 10.");
        }
    }

    @Override
    protected Rule getRule() {
        return getGlobalClient().getRule(
            r -> r.ruleId(getRuleId())
        ).rule();
    }

    @Override
    public void create(State state) {
        WafClient client = getGlobalClient();

        CreateRuleResponse response = client.createRule(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
                .metricName(getMetricName())
        );

        setRuleId(response.rule().ruleId());
    }

    @Override
    public void delete(State state) {
        WafClient client = getGlobalClient();

        client.deleteRule(
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
