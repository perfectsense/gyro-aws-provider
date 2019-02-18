package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.core.diff.ResourceName;
import gyro.lang.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.Predicate;
import software.amazon.awssdk.services.waf.model.RuleUpdate;

import java.util.Set;

@ResourceName(parent = "rule", value = "predicate")
@ResourceName(parent = "rate-rule", value = "predicate")
public class PredicateResource extends AwsResource {
    private String dataId;
    private Boolean negated;
    private String type;
    private Boolean rateRule;

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public Boolean getNegated() {
        return negated;
    }

    public void setNegated(Boolean negated) {
        this.negated = negated;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getRateRule() {
        return rateRule;
    }

    public void setRateRule(Boolean rateRule) {
        this.rateRule = rateRule;
    }

    public PredicateResource() {

    }

    public PredicateResource(Predicate predicate, boolean isRateRule) {
        setDataId(predicate.dataId());
        setNegated(predicate.negated());
        setType(predicate.typeAsString());
        setRateRule(isRateRule);
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        savePredicate(client, getPredicate(), false);
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        //Remove old predicate
        savePredicate(client,((PredicateResource) current).getPredicate(), true);

        //Add updates predicate
        savePredicate(client, getPredicate(), false);
    }

    @Override
    public void delete() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        savePredicate(client, getPredicate(), true);
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("predicate");

        if (!ObjectUtils.isBlank(getDataId())) {
            sb.append(" - ").append(getDataId());
        }

        if (!ObjectUtils.isBlank(getType())) {
            sb.append(" - ").append(getType());
        }

        if (getRateRule()) {
            sb.append(" - Rate Based");
        } else {
            sb.append(" - Regular");
        }

        return sb.toString();
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s %s", getDataId(), getType(), getRateRule() ? "Rate based" : "Regular");
    }

    @Override
    public String resourceIdentifier() {
        return null;
    }

    private Predicate getPredicate() {
        return Predicate.builder()
            .dataId(getDataId())
            .negated(getNegated())
            .type(getType())
            .build();
    }

    private void savePredicate(WafClient client, Predicate predicate, boolean isDelete) {
        RuleUpdate ruleUpdate = RuleUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .predicate(predicate)
            .build();

        if (!getRateRule()) {
            RuleResource parent = (RuleResource) parent();

            client.updateRule(
                r -> r.ruleId(parent.getRuleId())
                    .updates(ruleUpdate)
            );
        } else {
            RateRuleResource parent = (RateRuleResource) parent();

            client.updateRateBasedRule(
                r -> r.ruleId(parent.getRuleId())
                    .rateLimit(parent.getRateLimit())
                    .updates(ruleUpdate)
            );
        }
    }
}
