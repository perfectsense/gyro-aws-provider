package gyro.aws.waf.common;

import gyro.aws.Copyable;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.Predicate;
import software.amazon.awssdk.services.waf.model.PredicateType;
import software.amazon.awssdk.services.waf.model.RuleUpdate;
import software.amazon.awssdk.services.waf.model.UpdateRateBasedRuleRequest;
import software.amazon.awssdk.services.waf.model.UpdateRuleRequest;

import java.util.Set;

public abstract class PredicateResource extends AbstractWafResource implements Copyable<Predicate> {
    private ConditionResource condition;
    private Boolean negated;

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
     * Set if the condition is checked to be false. (Required)
     */
    @Updatable
    public Boolean getNegated() {
        return negated;
    }

    public void setNegated(Boolean negated) {
        this.negated = negated;
    }

    @Override
    public void copyFrom(Predicate predicate) {
        setCondition(findById(ConditionResource.class, predicate.dataId()));
        setNegated(predicate.negated());
        //setType(predicate.typeAsString());
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

        if (getCondition() != null) {
            sb.append(" - ").append(getCondition().getId());
        }

        if (getType() != null) {
            sb.append(" - ").append(getType().toString());
        }

        return sb.toString();
    }

    @Override
    public String primaryKey() {
        return String.format("%s", (getCondition() != null ? getCondition().getId() : null));
    }

    protected abstract void savePredicate(Predicate predicate, boolean isDelete);

    protected Predicate getPredicate() {
        return Predicate.builder()
            .dataId(getCondition().getId())
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

    private PredicateType getType() {
        PredicateType predicateType = null;
        if (getCondition() instanceof ByteMatchSetResource) {
            predicateType = PredicateType.BYTE_MATCH;
        } else if (getCondition() instanceof GeoMatchSetResource) {
            predicateType = PredicateType.GEO_MATCH;
        } else if (getCondition() instanceof RegexMatchSetResource) {
            predicateType = PredicateType.REGEX_MATCH;
        } else if (getCondition() instanceof IpSetResource) {
            predicateType = PredicateType.IP_MATCH;
        } else if (getCondition() instanceof SizeConstraintSetResource) {
            predicateType = PredicateType.SIZE_CONSTRAINT;
        } else if (getCondition() instanceof XssMatchSetResource) {
            predicateType = PredicateType.XSS_MATCH;
        } else if (getCondition() instanceof SqlInjectionMatchSetResource) {
            predicateType = PredicateType.SQL_INJECTION_MATCH;
        }

        return predicateType;
    }
}
