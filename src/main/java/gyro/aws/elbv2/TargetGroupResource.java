package gyro.aws.elbv2;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.ec2.VpcResource;

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
    private Boolean healthCheckEnabled;
    private Integer port;
    private String protocol;
    private Map<String, String> tags;
    private String arn;
    private String name;
    private String targetType;
    private VpcResource vpc;

    /**
     *  The health check associated with the target group (Optional)
     */
    public HealthCheck getHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(HealthCheck healthCheck) {
        this.healthCheck = healthCheck;
    }

    /**
     *  Indicates if health checks are enabled. Required if target type is instance. (Required)
     */
    @Updatable
    public Boolean getHealthCheckEnabled() {
        return healthCheckEnabled;
    }

    public void setHealthCheckEnabled(Boolean healthCheckEnabled) {
        this.healthCheckEnabled = healthCheckEnabled;
    }

    /**
     *  Port on which traffic is received by targets (Optional)
     */
    @Updatable
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     *  Protocol used to route traffic to targets (Optional)
     */
    @Updatable
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     *  List of tags associated with the target group (Optional)
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
     *  The name of the target group (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *  The type of the target. Options include instance, ip, and lambda (Optional)
     */
    @Updatable
    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    @Override
    public void copyFrom(TargetGroup targetGroup) {
        HealthCheck healthCheck = new HealthCheck();
        healthCheck.copyFrom(targetGroup);
        setHealthCheck(healthCheck);

        setHealthCheckEnabled(targetGroup.healthCheckEnabled());
        setPort(targetGroup.port());
        setProtocol(targetGroup.healthCheckProtocolAsString());
        setArn(targetGroup.targetGroupArn());
        setName(targetGroup.targetGroupName());
        setTargetType(targetGroup.targetTypeAsString());
        setVpc(findById(VpcResource.class, targetGroup.vpcId()));

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

        CreateTargetGroupResponse response;

        if (getHealthCheck() != null && getHealthCheckEnabled() == true) {
            response = client.createTargetGroup(r -> r.healthCheckEnabled(getHealthCheckEnabled())
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
                    .vpcId(getVpc().getVpcId())
            );
        } else {
            response = client.createTargetGroup(r -> r.healthCheckEnabled(getHealthCheckEnabled())
                    .port(getPort())
                    .protocol(getProtocol())
                    .name(getName())
                    .targetType(getTargetType())
                    .vpcId(getVpc().getVpcId()));
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

        if (getHealthCheck() != null && getHealthCheckEnabled() == true) {
            client.modifyTargetGroup(r -> r.healthCheckEnabled(getHealthCheckEnabled())
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
