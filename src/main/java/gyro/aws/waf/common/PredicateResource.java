/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.waf.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.DiffableInternals;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.Predicate;
import software.amazon.awssdk.services.waf.model.RuleUpdate;
import software.amazon.awssdk.services.waf.model.UpdateRateBasedRuleRequest;
import software.amazon.awssdk.services.waf.model.UpdateRuleRequest;

public abstract class PredicateResource extends AbstractWafResource implements Copyable<Predicate> {
    private ConditionResource condition;
    private Boolean negated;
    private String type;

    /**
     * The condition to be attached with the rule.
     */
    @Required
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
     * The type of condition being attached.
     */
    @ValidStrings({"XssMatch", "GeoMatch", "SqlInjectionMatch", "ByteMatch", "RegexMatch", "SizeConstraint", "IPMatch"})
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
        return String.format("%s", (getCondition() != null ? (ObjectUtils.isBlank(DiffableInternals.getName(getCondition())) ? getCondition().getId() : DiffableInternals.getName(getCondition())) : null));
    }

    protected abstract void savePredicate(Predicate predicate, boolean isDelete);

    protected Predicate toPredicate() {
        return Predicate.builder()
            .dataId(getCondition().getId())
            .negated(getNegated())
            .type(!ObjectUtils.isBlank(getType()) ? getType() : getCondition().getType())
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

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        ArrayList<ValidationError> errors = new ArrayList<>();

        if (getType() == null && getCondition().getType() == null) {
            errors.add(new ValidationError(this, "type",
                "The 'type' needs to be set if the 'condition' does not have a type."));
        }

        return errors;
    }
}
