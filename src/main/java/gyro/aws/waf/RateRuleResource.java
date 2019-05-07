package gyro.aws.waf;

import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceType;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateRateBasedRuleResponse;
import software.amazon.awssdk.services.waf.model.GetRateBasedRuleResponse;
import software.amazon.awssdk.services.waf.model.RateBasedRule;

import java.util.ArrayList;
import java.util.Set;

/**
 * Creates a rate rule.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::rate-rule rate-rule-example
 *         name: "rate-rule-example"
 *         metric-name: "rateRuleExample"
 *         rate-key: "IP"
 *         rate-limit: 10
 *     end
 */
@ResourceType("rate-rule")
public class RateRuleResource extends RuleBaseResource {
    private String rateKey;
    private Long rateLimit;

    /**
     * The rate key based on which the rate would be checked. (Required)
     */
    public String getRateKey() {
        return rateKey != null ? rateKey.toUpperCase() : null;
    }

    public void setRateKey(String rateKey) {
        this.rateKey = rateKey;
    }

    /**
     * The rate limit at which the action would be taken. Valid values integer 2000 and above. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public Long getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(Long rateLimit) {
        this.rateLimit = rateLimit;
    }

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getRuleId())) {
            return false;
        }

        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        GetRateBasedRuleResponse response = client.getRateBasedRule(
            r -> r.ruleId(getRuleId())
        );

        RateBasedRule rule = response.rule();
        setName(rule.name());
        setMetricName(rule.metricName());
        setRateKey(rule.rateKeyAsString());
        setRateLimit(rule.rateLimit());
        loadPredicates(rule.matchPredicates());

        return true;
    }

    @Override
    public void create() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        CreateRateBasedRuleResponse response = client.createRateBasedRule(
            r -> r.name(getName())
                .metricName(getMetricName())
                .changeToken(client.getChangeToken().changeToken())
                .rateKey(getRateKey())
                .rateLimit(getRateLimit())
        );

        RateBasedRule rule = response.rule();
        setRuleId(rule.ruleId());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        client.updateRateBasedRule(
            r -> r.ruleId(getRuleId())
                .changeToken(client.getChangeToken().changeToken())
                .rateLimit(getRateLimit())
                .updates(new ArrayList<>())
        );
    }

    @Override
    public void delete() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        client.deleteRateBasedRule(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .ruleId(getRuleId())
        );
    }

    @Override
    boolean isRateRule() {
        return true;
    }
}
