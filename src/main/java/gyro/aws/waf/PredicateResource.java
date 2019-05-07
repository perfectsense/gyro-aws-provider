package gyro.aws.waf;

import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.Predicate;
import software.amazon.awssdk.services.waf.model.RuleUpdate;
import software.amazon.awssdk.services.waf.model.UpdateRateBasedRuleRequest;
import software.amazon.awssdk.services.waf.model.UpdateRuleRequest;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.Set;

@ResourceName(parent = "rule", value = "predicate-regular")
@ResourceName(parent = "rate-rule", value = "predicate-rate-based")
public class PredicateResource extends AbstractWafResource {
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
    @ResourceDiffProperty(updatable = true)
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
        if (getRegionalWaf()) {
            savePredicate(getRegionalClient(), getPredicate(), false);
        } else {
            savePredicate(getGlobalClient(), getPredicate(), false);
        }
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        if (getRegionalWaf()) {
            //Remove old predicate
            savePredicate(getRegionalClient(),((PredicateResource) current).getPredicate(), true);

            //Add updates predicate
            savePredicate(getRegionalClient(), getPredicate(), false);
        } else {
            //Remove old predicate
            savePredicate(getGlobalClient(),((PredicateResource) current).getPredicate(), true);

            //Add updates predicate
            savePredicate(getGlobalClient(), getPredicate(), false);
        }
    }

    @Override
    public void delete() {
        if (getRegionalWaf()) {
            savePredicate(getRegionalClient(), getPredicate(), true);
        } else {
            savePredicate(getGlobalClient(), getPredicate(), true);
        }
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
        if (!getRateRule()) {
            client.updateRule(getUpdateRuleRequest(predicate, isDelete)
                .changeToken(client.getChangeToken().changeToken())
                .build()
            );
        } else {
            client.updateRateBasedRule(getUpdateRateBasedRuleRequest(predicate, isDelete)
                .changeToken(client.getChangeToken().changeToken())
                .build()
            );
        }
    }

    private void savePredicate(WafRegionalClient client, Predicate predicate, boolean isDelete) {
        if (!getRateRule()) {
            client.updateRule(getUpdateRuleRequest(predicate, isDelete)
                .changeToken(client.getChangeToken().changeToken())
                .build()
            );
        } else {
            client.updateRateBasedRule(getUpdateRateBasedRuleRequest(predicate, isDelete)
                .changeToken(client.getChangeToken().changeToken())
                .build()
            );
        }
    }

    private RuleUpdate getRuleUpdate(Predicate predicate, boolean isDelete) {
        return RuleUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .predicate(predicate)
            .build();
    }

    private UpdateRuleRequest.Builder getUpdateRuleRequest(Predicate predicate, boolean isDelete) {
        RuleResource parent = (RuleResource) parent();

        return UpdateRuleRequest.builder()
            .ruleId(parent.getRuleId())
            .updates(getRuleUpdate(predicate, isDelete));
    }

    private UpdateRateBasedRuleRequest.Builder getUpdateRateBasedRuleRequest(Predicate predicate, boolean isDelete) {
        RateRuleResource parent = (RateRuleResource) parent();

        return UpdateRateBasedRuleRequest.builder()
            .ruleId(parent.getRuleId())
            .rateLimit(parent.getRateLimit())
            .updates(getRuleUpdate(predicate, isDelete));
    }
}
