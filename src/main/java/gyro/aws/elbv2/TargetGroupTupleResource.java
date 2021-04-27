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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroupTuple;

public class TargetGroupTupleResource extends Diffable implements Copyable<TargetGroupTuple> {

    private TargetGroupResource targetGroup;
    private Integer weight;

    /**
     * The target group to which requests are forwarded
     */
    @Updatable
    public TargetGroupResource getTargetGroup() {
        return targetGroup;
    }

    public void setTargetGroup(TargetGroupResource targetGroup) {
        this.targetGroup = targetGroup;
    }

    /**
     * The weight of the ratio of requests forwarded to the given target group.
     * The ratio is this weight / total weights from all target groups configured
     */
    @Updatable
    public Integer getWeight() {
        return weight == null ?
            weight = 1 :
            weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    @Override
    public String primaryKey() {
        return String.format("%s/%s", getTargetGroup().getArn(), getWeight());
    }

    public String getTargetGroupArn() {
        return getTargetGroup() != null ?
            getTargetGroup().getArn() :
            null;
    }

    @Override
    public void copyFrom(TargetGroupTuple targetGroupTuple) {
        setTargetGroup(targetGroupTuple.targetGroupArn() != null ? findById(TargetGroupResource.class, targetGroupTuple.targetGroupArn()) : null);
        setWeight(targetGroupTuple.weight());
    }

    public TargetGroupTuple toTargetGroupTuple() {
        return TargetGroupTuple.builder()
            .targetGroupArn(getTargetGroup().getArn())
            .weight(getWeight())
            .build();
    }
}
