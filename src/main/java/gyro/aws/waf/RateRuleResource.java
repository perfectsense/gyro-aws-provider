package gyro.aws.waf;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.ResourceDiffProperty;
import software.amazon.awssdk.services.waf.model.RateBasedRule;

public abstract class RateRuleResource extends AbstractRuleResource {
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

    protected abstract RateBasedRule getRule();

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getRuleId())) {
            return false;
        }

        /*GetRateBasedRuleResponse response;

        if (getRegionalWaf()) {
            response = getRegionalClient().getRateBasedRule(
                r -> r.ruleId(getRuleId())
            );
        } else {
            response = getGlobalClient().getRateBasedRule(
                r -> r.ruleId(getRuleId())
            );
        }

        RateBasedRule rule = response.rule();*/
        RateBasedRule rule = getRule();
        setName(rule.name());
        setMetricName(rule.metricName());
        setRateKey(rule.rateKeyAsString());
        setRateLimit(rule.rateLimit());
        loadPredicates(rule.matchPredicates());

        return true;
    }

    /*@Override
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
    }*/

    @Override
    boolean isRateRule() {
        return true;
    }
}
