package gyro.aws.elbv2;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Resource;

import gyro.core.scope.State;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeTargetHealthResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.InvalidTargetException;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetDescription;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealth;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealthDescription;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealthStateEnum;

import java.util.Set;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *      aws::load-balancer-target target
 *          id: "i-5543455454435"
 *          port: 81
 *          target-group: $(aws::load-balancer-target-group tg-example)
 *      end
 */
@Type("load-balancer-target")
public class TargetResource extends AwsResource implements Copyable<TargetDescription> {

    private String availabilityZone;
    private String id;
    private Integer port;
    private TargetGroupResource targetGroup;

    /**
     *  The availability zone from where the target receives traffic. (Optional)
     */
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     *  The ID of the target. (Required)
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     *  The port on which the target is listening. (Required)
     */
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     *  The target group that the target is associated with. (Required)
     */
    public TargetGroupResource getTargetGroup() {
        return targetGroup;
    }

    public void setTargetGroup(TargetGroupResource targetGroup) {
        this.targetGroup = targetGroup;
    }

    @Override
    public void copyFrom(TargetDescription description) {
        setAvailabilityZone(description.availabilityZone());
        setPort(description.port());
        setId(description.id());
    }

    @Override
    public boolean refresh() {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);

        try {
            DescribeTargetHealthResponse response = client.describeTargetHealth(r -> r.targets(toTarget())
                    .targetGroupArn(getTargetGroup().getArn()));

            for (TargetHealthDescription targetHealthDescription : response.targetHealthDescriptions()) {
                TargetHealth health = targetHealthDescription.targetHealth();
                if (health.state() != TargetHealthStateEnum.DRAINING) {
                    TargetDescription description = targetHealthDescription.target();
                    this.copyFrom(description);
                }
            }

            return true;
        } catch (InvalidTargetException ex) {
            return false;
        }
    }

    @Override
    public void create(GyroUI ui, State state) {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.registerTargets(r -> r.targets(toTarget())
                                    .targetGroupArn(getTargetGroup().getArn()));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {}

    @Override
    public void delete(GyroUI ui, State state) {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.deregisterTargets(r -> r.targets(toTarget())
                                        .targetGroupArn(getTargetGroup().getArn()));
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        if (getId() != null) {
            sb.append("target " + getId());
        } else {
            sb.append("target - port: ");
            sb.append(getPort());
        }

        return sb.toString();
    }

    public TargetDescription toTarget() {
        return TargetDescription.builder()
                .availabilityZone(getAvailabilityZone())
                .id(getId())
                .port(getPort())
                .build();
    }
}
