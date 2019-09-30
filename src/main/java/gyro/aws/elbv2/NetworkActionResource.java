package gyro.aws.elbv2;

import gyro.aws.AwsResource;
import gyro.core.GyroUI;
import gyro.core.diff.Create;
import gyro.core.resource.DiffableInternals;
import gyro.core.resource.Resource;
import gyro.core.diff.Update;
import gyro.core.resource.Updatable;

import gyro.core.scope.State;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Action;

import java.util.Set;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     default-action
 *        target-group: $(aws::load-balancer-target-group target-group-example)
 *        type: "forward"
 *     end
 */
public class NetworkActionResource extends AwsResource {

    private TargetGroupResource targetGroup;
    private String type;

    /**
     *  The target group that this action is associated with  (Optional)
     */
    public TargetGroupResource getTargetGroup() {
        return targetGroup;
    }

    public void setTargetGroup(TargetGroupResource targetGroup) {
        this.targetGroup = targetGroup;
    }

    /**
     *  The type of action to perform  (Required)
     */
    @Updatable
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String primaryKey() {
        return getType();
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        if (DiffableInternals.hasChange(parentResource(), Create.class)) {
            return;
        }

        NetworkLoadBalancerListenerResource parent = (NetworkLoadBalancerListenerResource) parentResource();
        parent.updateDefaultAction();
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        if (DiffableInternals.hasChange(parentResource(), Update.class)) {
            return;
        }

        NetworkLoadBalancerListenerResource parent = (NetworkLoadBalancerListenerResource) parentResource();
        parent.updateDefaultAction();
    }

    @Override
    public void delete(GyroUI ui, State state) {}

    public Action toAction() {
        return Action.builder()
                .targetGroupArn(getTargetGroup().getArn())
                .type(getType())
                .build();
    }
}
