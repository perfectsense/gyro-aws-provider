package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.lang.Resource;
import software.amazon.awssdk.services.waf.WafClient;

import java.util.Set;
import java.util.UUID;

public class RateRuleResource extends AwsResource {
    private String name;
    private String metricName;
    private String rateKey;
    private Long rateLimit;
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

    public String getRateKey() {
        return rateKey;
    }

    public void setRateKey(String rateKey) {
        this.rateKey = rateKey;
    }

    public Long getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(Long rateLimit) {
        this.rateLimit = rateLimit;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        WafClient client = createClient(WafClient.class);

        client.createRateBasedRule(
            r -> r.name(getName())
                .metricName(getMetricName())
                .changeToken(UUID.randomUUID().toString())
                .rateKey(getRateKey())
                .rateLimit(getRateLimit())
        );
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        WafClient client = createClient(WafClient.class);

        client.updateRateBasedRule(
            r -> r.ruleId(getRuleId())
                .changeToken(UUID.randomUUID().toString())
                .rateLimit(getRateLimit())
        );
    }

    @Override
    public void delete() {
        WafClient client = createClient(WafClient.class);

        client.deleteRateBasedRule(
            r -> r.changeToken(UUID.randomUUID().toString())
                .ruleId(getRuleId())
        );
    }

    @Override
    public String toDisplayString() {
        return null;
    }
}
