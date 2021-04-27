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

package gyro.aws.elbv2;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.ec2.VpcResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;

import com.psddev.dari.util.CompactMap;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateTargetGroupResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeTagsResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeTargetGroupsResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeTargetHealthRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Matcher;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Tag;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetHealthDescription;

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
 *     aws::load-balancer-target-group target-group-example
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
 *             id: $(aws::instance instance-us-east-2a).id
 *             port: "80"
 *         end
 *
 *         target
 *             id: $(aws::instance instance-us-east-2b).id
 *             port: "443"
 *         end
 *
 *         tags: {
 *                 Name: "alb-example-target-group"
 *             }
 *     end
 */
@Type("load-balancer-target-group")
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
     *  The health check associated with the target group. Required for use with ``instance`` and ``ip`` target types.
     *
     *  @subresource gyro.aws.elbv2.HealthCheck
     */
    @Updatable
    public HealthCheck getHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(HealthCheck healthCheck) {
        this.healthCheck = healthCheck;
    }

    /**
     *  Port on which traffic is received by targets. Required for use ``instance`` and ``ip`` target types.
     */
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     *  Protocol used to route traffic to targets. Required for use with ``instance`` and ``ip`` target types.
     */
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     *  List of tags associated with the target group.
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

    /**
     *  The arn of the target group.
     */
    @Id
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     *  The name of the target group.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *  The type of the target. Will default to ``instance``.
     */
    @Updatable
    @ValidStrings({"instance", "ip", "lambda"})
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
     *  The vpc where the target group resides. Required for use with ``instance`` and ``ip`` target types.
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
        setProtocol(targetGroup.protocolAsString());
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
    public void create(GyroUI ui, State state) {
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
                    .vpcId(getVpc() != null ? getVpc().getId() : null)
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
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
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
    public void delete(GyroUI ui, State state) {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.deleteTargetGroup(r -> r.targetGroupArn(getArn()));
    }

}
