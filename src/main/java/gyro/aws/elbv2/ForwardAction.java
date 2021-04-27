/*
 * Copyright 2021, Brightspot.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.ForwardActionConfig;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroupStickinessConfig;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroupTuple;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     forward-action
 *         target-group-weight
 *             target-group: $(aws::load-balancer-target-group target-group-example)
 *             weight: 1
 *         end
 *         target-group-weight
 *             target-group: $(aws::load-balancer-target-group target-group-example-2)
 *             weight: 1
 *         end
 *         target-group-stickiness
 *             enabled: true
 *             duration: 60
 *         end
 *     end
 */
public class ForwardAction extends Diffable implements Copyable<ForwardActionConfig> {

    private List<TargetGroupTupleResource> targetGroupWeight;
    private TargetGroupStickiness targetGroupStickiness;

    /**
     * The list of target groups and their associated weight for forwarding.
     *
     * @subresource gyro.aws.elbv2.TargetGroupTupleResource
     */
    @Updatable
    public List<TargetGroupTupleResource> getTargetGroupWeight() {
        return targetGroupWeight == null ?
            targetGroupWeight = new ArrayList<>() :
            targetGroupWeight;
    }

    public void setTargetGroupWeight(List<TargetGroupTupleResource> targetGroupWeight) {
        this.targetGroupWeight = targetGroupWeight;
    }

    /**
     * The configuration to determine if subsequent requests should stay with the same target group.
     *
     * @subresource gyro.aws.elbv2.TargetGroupStickiness
     */
    @Updatable
    public TargetGroupStickiness getTargetGroupStickiness() {
        return targetGroupStickiness;
    }

    public void setTargetGroupStickiness(TargetGroupStickiness targetGroupStickiness) {
        this.targetGroupStickiness = targetGroupStickiness;
    }

    public void addTargets (String... targetGroupArns) {
        Set<String> existingTargetGroups = getTargetGroupWeight().stream()
            .map(TargetGroupTupleResource::getTargetGroupArn)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        for (String targetGroupArn : targetGroupArns) {
            TargetGroupResource targetGroupResource = findById(TargetGroupResource.class, targetGroupArn);
            if (!existingTargetGroups.contains(targetGroupArn) && targetGroupResource != null) {
                TargetGroupTupleResource tupleResource = newSubresource(TargetGroupTupleResource.class);
                tupleResource.setTargetGroup(targetGroupResource);
                getTargetGroupWeight().add(tupleResource);
            }
        }
    }

    @Override
    public void copyFrom(ForwardActionConfig forwardActionConfig) {

        getTargetGroupWeight().clear();
        if (forwardActionConfig.hasTargetGroups()) {
            for (TargetGroupTuple targetGroupTuple : forwardActionConfig.targetGroups()) {
                TargetGroupTupleResource targetGroupWeight = newSubresource(TargetGroupTupleResource.class);
                targetGroupWeight.copyFrom(targetGroupTuple);
                getTargetGroupWeight().add(targetGroupWeight);
            }
        }

        TargetGroupStickinessConfig targetGroupStickinessConfig = forwardActionConfig.targetGroupStickinessConfig();
        if (targetGroupStickinessConfig != null) {
            TargetGroupStickiness targetGroupStickiness = newSubresource(TargetGroupStickiness.class);
            targetGroupStickiness.copyFrom(targetGroupStickinessConfig);
        }
    }

    public List<TargetGroupTuple> toTargetGroupTuples() {
        return getTargetGroupWeight().stream()
            .map(TargetGroupTupleResource::toTargetGroupTuple)
            .collect(Collectors.toList());
    }

    public TargetGroupStickinessConfig toTargetGroupStickinessConfig() {
        if (getTargetGroupStickiness() != null) {
            return TargetGroupStickinessConfig.builder()
                .durationSeconds(getTargetGroupStickiness().getDuration())
                .enabled(getTargetGroupStickiness().getEnabled())
                .build();
        }

        return null;
    }

    public ForwardActionConfig toForwardActionConfig() {
        return ForwardActionConfig.builder()
            .targetGroups(toTargetGroupTuples())
            .targetGroupStickinessConfig(toTargetGroupStickinessConfig())
            .build();
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
