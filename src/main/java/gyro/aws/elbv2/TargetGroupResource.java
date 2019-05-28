package gyro.aws.elbv2;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.ec2.VpcResource;
import gyro.core.GyroException;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;

import com.psddev.dari.util.CompactMap;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateTargetGroupResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeTagsResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeTargetGroupsResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Matcher;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Tag;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::target-group target-group-example
 *         name: "test-target-group"
 *         port: "80"
 *         protocol: "HTTP"
 *         target-type: "instance"
 *         vpc: $(aws::vpc vpc)
 *         enabled: "true"
 *
 *         health-check
 *             interval: "90"
 *             path: "/"
 *             port: "traffic-port"
 *             protocol: "HTTP"
 *             timeout: "30"
 *             healthy-threshold: "2"
 *             matcher: "200"
 *             unhealthy-threshold: "2"
 *         end
 *
 *         target
 *             id: $(aws::instance instance-us-east-2a | instance-id)
 *             port: "80"
 *         end
 *
 *         target
 *             id: $(aws::instance instance-us-east-2b | instance-id)
 *             port: "443"
 *         end
 *
 *         tags: {
 *                 Name: "alb-example-target-group"
 *             }
 *     end
 */

@Type("target-group")
public class TargetGroupResource extends AwsResource implements Copyable<TargetGroup> {

    private HealthCheck healthCheck;
    private Integer port;
    private String protocol;
    private Map<String, String> tags;
    private String arn;
    private String name;
    private String targetType;
    private VpcResource vpc;

    /**
     *  The health check associated with the target group. Required for use with ``instance`` and ``ip`` target types. (Optional)
     */
    @Updatable
    public HealthCheck getHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(HealthCheck healthCheck) {
        this.healthCheck = healthCheck;
    }

    /**
     *  Port on which traffic is received by targets. Required for use ``instance`` and ``ip`` target types. (Optional)
     */
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     *  Protocol used to route traffic to targets. Valid values are ``HTTP`` and ``HTTPS`` for ALBs and ``TCP`` and ``TLS`` for NLBs.
     *  Required for use with ``instance`` and ``ip`` target types. (Optional)
     */
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     *  List of tags associated with the target group. (Optional)
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new CompactMap<>();
        }
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        if (this.tags != null && tags != null) {
            this.tags.putAll(tags);

        } else {
            this.tags = tags;
        }
    }

    @Output
    @Id
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     *  The name of the target group. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *  The type of the target. Valid values are ``instance``, ``ip``, and ``lambda``. Will default to ``instance``. (Optional)
     */
    @Updatable
    public String getTargetType() {
        if (targetType == null) {
            targetType = "instance";
        }

        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    /**
     *  The vpc where the target group resides. Required for use with ``instance`` and ``ip`` target types. (Optional)
     */
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    @Override
    public void copyFrom(TargetGroup targetGroup) {
        if (getHealthCheck() != null) {
            HealthCheck healthCheck = new HealthCheck();
            healthCheck.copyFrom(targetGroup);
            setHealthCheck(healthCheck);
        }

        setPort(targetGroup.port());
        setProtocol(targetGroup.healthCheckProtocolAsString());
        setArn(targetGroup.targetGroupArn());
        setName(targetGroup.targetGroupName());
        setTargetType(targetGroup.targetTypeAsString());
        setVpc(targetGroup.vpcId() != null ? findById(VpcResource.class, targetGroup.vpcId()) : null);

        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);

        getTags().clear();
        DescribeTagsResponse tagResponse = client.describeTags(r -> r.resourceArns(getArn()));
        if (tagResponse != null) {
            List<Tag> tags = tagResponse.tagDescriptions().get(0).tags();
            for (Tag tag : tags) {
                getTags().put(tag.key(), tag.value());
            }
        }
    }

    @Override
    public boolean refresh() {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);

        DescribeTargetGroupsResponse tgResponse = client.describeTargetGroups(r -> r.targetGroupArns(getArn()));

        if (tgResponse != null) {
            TargetGroup tg = tgResponse.targetGroups().get(0);

            this.copyFrom(tg);

            return true;
        }

        return false;
    }

    @Override
    public void create() {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);

        CreateTargetGroupResponse response = null;

        if (!getTargetType().equals("lambda") && getHealthCheck() == null) {
            throw new GyroException("A health check must be provided for instance and ip target types.");
        }

        if (getHealthCheck() != null) {
            response = client.createTargetGroup(r -> r.healthCheckEnabled(true)
                    .healthCheckIntervalSeconds(getHealthCheck().getInterval())
                    .healthCheckPath(getHealthCheck().getPath())
                    .healthCheckPort(getHealthCheck().getPort())
                    .healthCheckProtocol(getHealthCheck().getProtocol())
                    .healthCheckTimeoutSeconds(getHealthCheck().getTimeout())
                    .healthyThresholdCount(getHealthCheck().getHealthyThreshold())
                    .matcher(Matcher.builder().httpCode(getHealthCheck().getMatcher()).build())
                    .port(getPort())
                    .protocol(getProtocol())
                    .name(getName())
                    .targetType(getTargetType())
                    .unhealthyThresholdCount(getHealthCheck().getUnhealthyThreshold())
                    .vpcId(getVpc() != null ? getVpc().getVpcId() : null)
            );
        } else if (getTargetType().equals("lambda") && getHealthCheck() == null) {
            response = client.createTargetGroup(r -> r.healthCheckEnabled(false)
                    .name(getName())
                    .targetType(getTargetType()));
        }

        setArn(response.targetGroups().get(0).targetGroupArn());

        if (!getTags().isEmpty()) {
            List<Tag> tag = new ArrayList<>();
            getTags().forEach((key, value) -> tag.add(Tag.builder().key(key).value(value).build()));
            client.addTags(r -> r.tags(tag)
                    .resourceArns(getArn()));
        }
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);

        if (getHealthCheck() != null) {
            client.modifyTargetGroup(r -> r.healthCheckEnabled(true)
                    .healthCheckIntervalSeconds(getHealthCheck().getInterval())
                    .healthCheckPath(getHealthCheck().getPath())
                    .healthCheckPort(getHealthCheck().getPort())
                    .healthCheckProtocol(getHealthCheck().getProtocol())
                    .healthCheckTimeoutSeconds(getHealthCheck().getTimeout())
                    .healthyThresholdCount(getHealthCheck().getHealthyThreshold())
                    .matcher(Matcher.builder().httpCode(getHealthCheck().getMatcher()).build())
                    .targetGroupArn(getArn())
                    .unhealthyThresholdCount(getHealthCheck().getUnhealthyThreshold())
            );
        } else if (getTargetType().equals("lambda") && getHealthCheck() == null) {
            client.modifyTargetGroup(r -> r.healthCheckEnabled(false)
                    .targetGroupArn(getArn()));
        }

        TargetGroupResource currentResource = (TargetGroupResource) current;

        Map<String, String> tagAdditions = new HashMap<>(getTags());
        currentResource.getTags().forEach((key, value) -> tagAdditions.remove(key, value));

        Map<String, String> tagSubtractions = new HashMap<>(currentResource.getTags());
        getTags().forEach((key, value) -> tagSubtractions.remove(key, value));

        if (!tagAdditions.isEmpty()) {
            List<Tag> tag = new ArrayList<>();
            tagAdditions.forEach((key, value) -> tag.add(Tag.builder().key(key).value(value).build()));
            client.addTags(r -> r.tags(tag)
                    .resourceArns(getArn()));
        }

        if (!tagSubtractions.isEmpty()) {
            List<String> tag = new ArrayList<>();
            tagSubtractions.forEach((key, value) -> tag.add(key));
            client.removeTags(r -> r.tagKeys(tag)
                    .resourceArns(getArn()));
        }
    }

    @Override
    public void delete() {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.deleteTargetGroup(r -> r.targetGroupArn(getArn()));
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        if (getName() != null) {
            sb.append("target group - " + getName());

        } else {
            sb.append("target group ");
        }

        return sb.toString();
    }
}
