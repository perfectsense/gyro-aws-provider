/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.autoscaling;

import java.util.List;
import java.util.Map;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.autoscalingplans.AutoScalingPlansClient;
import software.amazon.awssdk.services.autoscalingplans.model.ScalingPlan;

/**
 * Query auto scaling plan.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    project: $(external-query aws::autoscaling-plan { scaling-plan-names: ['scaling-plan-example']}
 */
@Type("autoscaling-plan")
public class AutoScalingPlanFinder extends AwsFinder<AutoScalingPlansClient, ScalingPlan, AutoScalingPlanResource> {

    private List<String> scalingPlanNames;

    /**
     * The set of scaling plan names.
     */
    public List<String> getScalingPlanNames() {
        return scalingPlanNames;
    }

    public void setScalingPlanNames(List<String> scalingPlanNames) {
        this.scalingPlanNames = scalingPlanNames;
    }

    @Override
    protected List<ScalingPlan> findAllAws(AutoScalingPlansClient client) {
        return client.describeScalingPlans().scalingPlans();
    }

    @Override
    protected List<ScalingPlan> findAws(
        AutoScalingPlansClient client, Map<String, String> filters) {
        return client.describeScalingPlans(r -> r.scalingPlanNames(filters.get("scaling-plan-names"))).scalingPlans();
    }
}
