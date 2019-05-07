package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.core.resource.ResourceUpdatable;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.Predicate;
import software.amazon.awssdk.services.waf.model.RuleUpdate;

import java.util.Set;

public class PredicateResource extends AwsResource {
    private String dataId;
    private Boolean negated;
    private String type;
    private Boolean rateRule;

    /**
     * The id of the condition to be attached with the rule. (Required)
     */
    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    /**
     * Set if the condition is checked to be false. (Required)
     */
    @ResourceUpdatable
    public Boolean getNegated() {
        return negated;
    }

    public void setNegated(Boolean negated) {
        this.negated = negated;
    }

    /**
     * The type of condition being attached. Valid values are ``XssMatch`` or ``GeoMatch`` or ``SqlInjectionMatch`` or ``ByteMatch`` or ``RegexMatch`` or ``SizeConstraint`` or ``IPMatch``. (Required)
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Flag to mark the rule as a regular or a rate based.
     */
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
        return String.format("%s %s %s", getDataId(), getType(), (getRateRule() ? "Rate based" : "Regular"));
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
                    .changeToken(client.getChangeToken().changeToken())
            );
        } else {
            RateRuleResource parent = (RateRuleResource) parent();

            client.updateRateBasedRule(
                r -> r.ruleId(parent.getRuleId())
                    .rateLimit(parent.getRateLimit())
                    .updates(ruleUpdate)
                    .changeToken(client.getChangeToken().changeToken())
            );
        }
    }
}
