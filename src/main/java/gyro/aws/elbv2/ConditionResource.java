package gyro.aws.elbv2;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.resource.Create;
import gyro.core.resource.Delete;
import gyro.core.resource.Resource;
import gyro.core.resource.Update;
import gyro.core.resource.Updatable;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.RuleCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     condition
 *         field: "host-header"
 *         value: ["www.example.net"]
 *     end
 */

public class ConditionResource extends AwsResource implements Copyable<RuleCondition> {

    private String field;
    private List<String> value;

    /**
     *  Condition field name  (Required)
     */
    @Updatable
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    /**
     *  Condition value (Required)
     */
    @Updatable
    public List<String> getValue() {
        if (value == null) {
            value = new ArrayList<>();
        }

        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }

    @Override
    public String primaryKey() {
        return String.format("%s", getField(), getValue());
    }

    @Override
    public void copyFrom(RuleCondition ruleCondition) {
        setField(ruleCondition.field());
        setValue(ruleCondition.values());
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void create() {
        if (parentResource().change() instanceof Create) {
            return;
        }

        ApplicationLoadBalancerListenerRuleResource parent = (ApplicationLoadBalancerListenerRuleResource) parentResource();
        parent.createCondition(this);
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        if (parentResource().change() instanceof Update) {
            return;
        }

        ApplicationLoadBalancerListenerRuleResource parent = (ApplicationLoadBalancerListenerRuleResource) parentResource();
        parent.updateCondition();
    }

    @Override
    public void delete() {
        if (parentResource().change() instanceof Delete) {
            return;
        }

        ApplicationLoadBalancerListenerRuleResource parent = (ApplicationLoadBalancerListenerRuleResource) parentResource();
        parent.deleteCondition(this);
    }

    @Override
    public String toDisplayString() {
        return "rule condition - field: " + getField();
    }

    public RuleCondition toCondition() {
        return RuleCondition.builder()
                .field(getField())
                .values(getValue())
                .build();
    }
}
