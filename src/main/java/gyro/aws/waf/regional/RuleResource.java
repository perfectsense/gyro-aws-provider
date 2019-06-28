package gyro.aws.waf.regional;

import gyro.core.GyroException;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.waf.model.CreateRuleResponse;
import software.amazon.awssdk.services.waf.model.Predicate;
import software.amazon.awssdk.services.waf.model.Rule;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Creates a regional rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::waf-rule-regional rule-example
 *         name: "rule-example"
 *         metric-name: "ruleExample"
 *
 *         predicate
 *             condition: $(aws::regex-match-set-regional regex-match-set-example-waf)
 *             negated: false
 *         end
 *     end
 */
@Type("waf-rule-regional")
public class RuleResource extends gyro.aws.waf.common.RuleResource {
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

        if (predicate.size() > 10) {
            throw new GyroException("Predicate limit exception. Max 10.");
        }

        return predicate;
    }

    public void setPredicate(Set<PredicateResource> predicate) {
        this.predicate = predicate;
    }

    @Override
    protected Rule getRule() {
        return getRegionalClient().getRule(
            r -> r.ruleId(getRuleId())
        ).rule();
    }

    @Override
    public void create() {
        WafRegionalClient client = getRegionalClient();

        CreateRuleResponse response = client.createRule(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
                .metricName(getMetricName())
        );

        setRuleId(response.rule().ruleId());
    }

    @Override
    public void delete() {
        WafRegionalClient client = getRegionalClient();

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
