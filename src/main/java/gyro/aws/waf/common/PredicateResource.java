package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceUpdatable;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.Predicate;
import software.amazon.awssdk.services.waf.model.RuleUpdate;
import software.amazon.awssdk.services.waf.model.UpdateRateBasedRuleRequest;
import software.amazon.awssdk.services.waf.model.UpdateRuleRequest;

import java.util.Set;

public abstract class PredicateResource extends AbstractWafResource {
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
        savePredicate(getPredicate(), false);
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        //Remove old predicate
        savePredicate(((PredicateResource) current).getPredicate(), true);

        //Add updates predicate
        savePredicate(getPredicate(), false);
    }

    @Override
    public void delete() {
        savePredicate(getPredicate(), true);
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

    protected abstract void savePredicate(Predicate predicate, boolean isDelete);

    protected Predicate getPredicate() {
        return Predicate.builder()
            .dataId(getDataId())
            .negated(getNegated())
            .type(getType())
            .build();
    }

    private RuleUpdate getRuleUpdate(Predicate predicate, boolean isDelete) {
        return RuleUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .predicate(predicate)
            .build();
    }

    protected UpdateRuleRequest.Builder getUpdateRuleRequest(Predicate predicate, boolean isDelete) {
        RuleResource parent = (RuleResource) parent();

        return UpdateRuleRequest.builder()
            .ruleId(parent.getRuleId())
            .updates(getRuleUpdate(predicate, isDelete));
    }

    protected UpdateRateBasedRuleRequest.Builder getUpdateRateBasedRuleRequest(Predicate predicate, boolean isDelete) {
        RateRuleResource parent = (RateRuleResource) parent();

        return UpdateRateBasedRuleRequest.builder()
            .ruleId(parent.getRuleId())
            .rateLimit(parent.getRateLimit())
            .updates(getRuleUpdate(predicate, isDelete));
    }
}
