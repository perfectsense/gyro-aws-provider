package gyro.aws.waf.global;

import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceUpdatable;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateRuleResponse;
import software.amazon.awssdk.services.waf.model.Predicate;
import software.amazon.awssdk.services.waf.model.Rule;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a global rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::rule rule-example
 *         name: "rule-example"
 *         metric-name: "ruleExample"
 *     end
 */
@ResourceType("rule")
public class RuleResource extends gyro.aws.waf.common.RuleResource {
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
    protected Rule getRule() {
        return getGlobalClient().getRule(
            r -> r.ruleId(getRuleId())
        ).rule();
    }

    @Override
    public void create() {
        WafClient client = getGlobalClient();

        CreateRuleResponse response = client.createRule(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
                .metricName(getMetricName())
        );

        setRuleId(response.rule().ruleId());
    }

    @Override
    public void delete() {
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
            PredicateResource predicateResource = new PredicateResource(predicate);
            getPredicate().add(predicateResource);
        }
    }
}
