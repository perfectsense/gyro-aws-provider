package gyro.aws.elbv2;

import gyro.aws.AwsResource;
import gyro.core.resource.ResourceType;
import gyro.core.resource.Resource;
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
 *         aws::target target
 *             id: "i-5543455454435"
 *             port: 81
 *             target-group-arn: $(aws::target-group webserver-rta-1 | arn)
 *         end
 */

@ResourceType("target")
public class TargetResource extends AwsResource {

    private String availabilityZone;
    private String id;
    private Integer port;
    private String targetGroupArn;

    public TargetResource() {

    }

    /**
     *   The availability zone from where the target receives traffic (Optional)
     */
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     *  The ID of the target (Required)
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     *  The port on which the target is listening (Required)
     */
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     *  The arn of the target group that the target is associated with (Required)
     */
    public String getTargetGroupArn() {
        return targetGroupArn;
    }

    public void setTargetGroupArn(String targetGroupArn) {
        this.targetGroupArn = targetGroupArn;
    }

    @Override
    public String primaryKey() {
        return String.format("%s %d", getId(), getPort());
    }

    @Override
    public boolean refresh() {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);

        try {
            DescribeTargetHealthResponse response = client.describeTargetHealth(r -> r.targets(toTarget())
                    .targetGroupArn(getTargetGroupArn()));

            for (TargetHealthDescription targetHealthDescription : response.targetHealthDescriptions()) {
                TargetHealth health = targetHealthDescription.targetHealth();
                if (health.state() != TargetHealthStateEnum.DRAINING) {
                    TargetDescription description = targetHealthDescription.target();
                    setAvailabilityZone(description.availabilityZone());
                    setPort(description.port());
                    setId(description.id());
                }
            }

            return true;
        } catch (InvalidTargetException ex) {
            return false;
        }
    }

    @Override
    public void create() {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.registerTargets(r -> r.targets(toTarget())
                                    .targetGroupArn(getTargetGroupArn()));
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {}

    @Override
    public void delete() {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.deregisterTargets(r -> r.targets(toTarget())
                                        .targetGroupArn(getTargetGroupArn()));
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
