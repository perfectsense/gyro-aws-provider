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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.autoscalingplans.AutoScalingPlansClient;
import software.amazon.awssdk.services.autoscalingplans.model.DescribeScalingPlansRequest;
import software.amazon.awssdk.services.autoscalingplans.model.DescribeScalingPlansResponse;
import software.amazon.awssdk.services.autoscalingplans.model.ScalingPlan;

/**
 * Query auto scaling plan.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    project: $(external-query aws::autoscaling-plan { name: 'scaling-plan-example'}
 */
@Type("autoscaling-plan")
public class AutoScalingPlanFinder extends AwsFinder<AutoScalingPlansClient, ScalingPlan, AutoScalingPlanResource> {

    private String name;

    /**
     * The name of the scaling plan.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<ScalingPlan> findAllAws(AutoScalingPlansClient client) {
        List<ScalingPlan> scalingPlans = new ArrayList<>();
        DescribeScalingPlansResponse response;
        String token = null;

        do {
            if (ObjectUtils.isBlank(token)) {
                response = client.describeScalingPlans();
            } else {
                response = client.describeScalingPlans(DescribeScalingPlansRequest.builder()
                    .nextToken(token)
                    .build());
            }

            if (response.hasScalingPlans()) {
                scalingPlans.addAll(response.scalingPlans());
            }

            token = response.nextToken();
        } while (!ObjectUtils.isBlank(token));

        return scalingPlans;
    }

    @Override
    protected List<ScalingPlan> findAws(
        AutoScalingPlansClient client, Map<String, String> filters) {
        return client.describeScalingPlans(r -> r.scalingPlanNames(filters.get("name"))).scalingPlans();
    }
}
