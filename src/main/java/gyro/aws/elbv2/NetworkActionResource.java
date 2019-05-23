package gyro.aws.elbv2;

import gyro.aws.AwsResource;
import gyro.core.resource.Create;
import gyro.core.resource.Updatable;
import gyro.core.resource.Update;
import gyro.core.resource.Resource;
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
 *        target-group-arn: $(aws::target-group target-group-example | target-group-arn)
 *        type: "forward"
 *     end
 */
public class NetworkActionResource extends AwsResource {

    private String targetGroupArn;
    private String type;

    /**
     *  The target group arn that this action is associated with  (Optional)
     */
    public String getTargetGroupArn() {
        return targetGroupArn;
    }

    public void setTargetGroupArn(String targetGroupArn) {
        this.targetGroupArn = targetGroupArn;
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
        return String.format("%s %s", getTargetGroupArn(), getType());
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

        NetworkLoadBalancerListenerResource parent = (NetworkLoadBalancerListenerResource) parentResource();
        parent.updateDefaultAction();
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        if (parentResource().change() instanceof Update) {
            return;
        }

        NetworkLoadBalancerListenerResource parent = (NetworkLoadBalancerListenerResource) parentResource();
        parent.updateDefaultAction();
    }

    @Override
    public void delete() {}

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append("nlb listener default action");
        return sb.toString();
    }

    public Action toAction() {
        return Action.builder()
                .targetGroupArn(getTargetGroupArn())
                .type(getType())
                .build();
    }
}
