package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.lang.Resource;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.GetRuleResponse;
import software.amazon.awssdk.services.waf.model.Predicate;
import software.amazon.awssdk.services.waf.model.Rule;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RuleResource extends AwsResource {
    private String name;
    private String metricName;
    private String ruleId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    @Override
    public boolean refresh() {
        WafClient client = createClient(WafClient.class);

        GetRuleResponse response = client.getRule(
            r -> r.ruleId(getRuleId())
        );

        Rule rule = response.rule();
        setMetricName(rule.metricName());
        setName(rule.name());
        List<Predicate> predicates = rule.predicates();

        return true;
    }

    @Override
    public void create() {
        WafClient client = createClient(WafClient.class);

        client.createRule(
            r -> r.changeToken(UUID.randomUUID().toString())
                .name(getName())
                .metricName(getMetricName())
        );
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        WafClient client = createClient(WafClient.class);

        client.deleteRule(
            r -> r.changeToken(UUID.randomUUID().toString())
                .ruleId(getRuleId())
        );
    }

    @Override
    public String toDisplayString() {
        return null;
    }
}
