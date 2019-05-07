package gyro.aws.waf;

import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateRateBasedRuleResponse;
import software.amazon.awssdk.services.waf.model.GetRateBasedRuleResponse;
import software.amazon.awssdk.services.waf.model.RateBasedRule;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

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
@ResourceName("rate-rule")
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
    @ResourceDiffProperty(updatable = true, nullable = true)
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

        GetRateBasedRuleResponse response;

        if (getRegionalWaf()) {
            response = getRegionalClient().getRateBasedRule(
                r -> r.ruleId(getRuleId())
            );
        } else {
            response = getGlobalClient().getRateBasedRule(
                r -> r.ruleId(getRuleId())
            );
        }

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
        CreateRateBasedRuleResponse response;

        if (getRegionalWaf()) {
            WafRegionalClient client = getRegionalClient();

            response = client.createRateBasedRule(
                r -> r.name(getName())
                    .metricName(getMetricName())
                    .changeToken(client.getChangeToken().changeToken())
                    .rateKey(getRateKey())
                    .rateLimit(getRateLimit())
            );
        } else {
            WafClient client = getGlobalClient();

            response = client.createRateBasedRule(
                r -> r.name(getName())
                    .metricName(getMetricName())
                    .changeToken(client.getChangeToken().changeToken())
                    .rateKey(getRateKey())
                    .rateLimit(getRateLimit())
            );
        }

        RateBasedRule rule = response.rule();
        setRuleId(rule.ruleId());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        if (getRegionalWaf()) {
            WafRegionalClient client = getRegionalClient();

            client.updateRateBasedRule(
                r -> r.ruleId(getRuleId())
                    .changeToken(client.getChangeToken().changeToken())
                    .rateLimit(getRateLimit())
                    .updates(new ArrayList<>())
            );
        } else {
            WafClient client = getGlobalClient();

            client.updateRateBasedRule(
                r -> r.ruleId(getRuleId())
                    .changeToken(client.getChangeToken().changeToken())
                    .rateLimit(getRateLimit())
                    .updates(new ArrayList<>())
            );
        }
    }

    @Override
    public void delete() {
        if (getRegionalWaf()) {
            WafRegionalClient client = getRegionalClient();

            client.deleteRateBasedRule(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .ruleId(getRuleId())
            );
        } else {
            WafClient client = getGlobalClient();

            client.deleteRateBasedRule(
                r -> r.changeToken(client.getChangeToken().changeToken())
                    .ruleId(getRuleId())
            );
        }
    }

    @Override
    boolean isRateRule() {
        return true;
    }
}
