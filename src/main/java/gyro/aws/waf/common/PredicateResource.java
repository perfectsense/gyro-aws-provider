package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.Predicate;
import software.amazon.awssdk.services.waf.model.RuleUpdate;
import software.amazon.awssdk.services.waf.model.UpdateRateBasedRuleRequest;
import software.amazon.awssdk.services.waf.model.UpdateRuleRequest;

import java.util.Set;

public abstract class PredicateResource extends AbstractWafResource implements Copyable<Predicate> {
    private ConditionResource condition;
    private Boolean negated;
    private String type;

    /**
     * The condition to be attached with the rule. (Required)
     */
    public ConditionResource getCondition() {
        return condition;
    }

    public void setCondition(ConditionResource condition) {
        this.condition = condition;
    }

    /**
     * Set if the condition is checked to be false. Defaults to false.
     */
    @Updatable
    public Boolean getNegated() {
        if (negated == null) {
            negated = false;
        }

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

    @Override
    public void copyFrom(Predicate predicate) {
        setCondition(findById(ConditionResource.class, predicate.dataId()));
        setNegated(predicate.negated());
        setType(predicate.typeAsString());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {
        savePredicate(toPredicate(), false);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        //Remove old predicate
        savePredicate(((PredicateResource) current).toPredicate(), true);

        //Add updates predicate
        savePredicate(toPredicate(), false);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        savePredicate(toPredicate(), true);
    }

    @Override
    public String primaryKey() {
        return String.format("%s", (getCondition() != null ? (ObjectUtils.isBlank(getCondition().name()) ? getCondition().getId() : getCondition().name()) : null));
    }

    protected abstract void savePredicate(Predicate predicate, boolean isDelete);

    protected Predicate toPredicate() {
        return Predicate.builder()
            .dataId(getCondition().getId())
            .negated(getNegated())
            .type(!ObjectUtils.isBlank(getType()) ? getType() : (getCondition() != null ? getCondition().getType() : null))
            .build();
    }

    private RuleUpdate toRuleUpdate(Predicate predicate, boolean isDelete) {
        return RuleUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .predicate(predicate)
            .build();
    }

    protected UpdateRuleRequest.Builder toUpdateRuleRequest(Predicate predicate, boolean isDelete) {
        RuleResource parent = (RuleResource) parent();

        return UpdateRuleRequest.builder()
            .ruleId(parent.getRuleId())
            .updates(toRuleUpdate(predicate, isDelete));
    }

    protected UpdateRateBasedRuleRequest.Builder toUpdateRateBasedRuleRequest(Predicate predicate, boolean isDelete) {
        RateRuleResource parent = (RateRuleResource) parent();

        return UpdateRateBasedRuleRequest.builder()
            .ruleId(parent.getRuleId())
            .rateLimit(parent.getRateLimit())
            .updates(toRuleUpdate(predicate, isDelete));
    }
}
