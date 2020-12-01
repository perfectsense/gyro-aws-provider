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
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.autoscalingplans.AutoScalingPlansClient;
import software.amazon.awssdk.services.autoscalingplans.model.DescribeScalingPlansResponse;
import software.amazon.awssdk.services.autoscalingplans.model.ScalingPlan;
import software.amazon.awssdk.services.autoscalingplans.model.ScalingPlanStatusCode;

/**
 * Creates a Scaling plan.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::autoscaling-plan autoscaling-plan-example
 *        name: "scaling-plan-example"
 *        scaling-plan-version: 1.0
 *        status-code: "Active"
 *
 *        application-source
 *            tag-filter
 *                key: "auto-scaling-group-gyro-1-tag"
 *                values: ["value"]
 *            end
 *        end
 *
 *        scaling-instructions
 *            max-capacity: 100
 *            min-capacity: 0
 *            service-namespace: "autoscaling"
 *            resource-id: "autoScalingGroup/auto-scaling-group-gyro-1"
 *            scalable-dimension: "autoscaling:autoScalingGroup:DesiredCapacity"
 *
 *            target-tracking-configurations
 *                target-value: 2.0
 *
 *                customized-scaling-metric-specification
 *                    dimensions
 *                        name: "dimension-test"
 *                        value: "dimension-test-value"
 *                    end
 *
 *                    metric-name: "metric-name-test"
 *                    namespace: "namespace"
 *                    statistic: "Sum"
 *                end
 *            end
 *        end
 *    end
 *
 */
@Type("autoscaling-plan")
public class AutoScalingPlanResource extends AwsResource implements Copyable<ScalingPlan> {

    private AutoScalingApplicationSource applicationSource;
    private String creationTime;
    private List<AutoScalingScalingInstruction> scalingInstructions;
    private String name;
    private Long scalingPlanVersion;
    private ScalingPlanStatusCode statusCode;
    private String statusMessage;
    private String statusStartTime;

    /**
     * The CloudFormation stack or a set of tag filters.
     *
     * @subresource gyro.aws.autoscaling.AutoScalingApplicationSource
     */
    @Updatable
    @Required
    public AutoScalingApplicationSource getApplicationSource() {
        return applicationSource;
    }

    public void setApplicationSource(AutoScalingApplicationSource applicationSource) {
        this.applicationSource = applicationSource;
    }

    /**
     * The time and date when the scaling plan was created.
     */
    @Output
    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * A list of scaling instruction configuration.
     *
     * @subresource gyro.aws.autoscaling.AutoScalingScalingInstruction
     */
    @Updatable
    @Required
    public List<AutoScalingScalingInstruction> getScalingInstructions() {
        if (scalingInstructions == null) {
            scalingInstructions = new ArrayList<>();
        }

        return scalingInstructions;
    }

    public void setScalingInstructions(List<AutoScalingScalingInstruction> scalingInstructions) {
        this.scalingInstructions = scalingInstructions;
    }

    /**
     * The name of the scaling plan.
     */
    @Id
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The version of the scaling plan.
     */

    @Updatable
    public Long getScalingPlanVersion() {
        return scalingPlanVersion;
    }

    public void setScalingPlanVersion(Long scalingPlanVersion) {
        this.scalingPlanVersion = scalingPlanVersion;
    }

    /**
     * The status of the scaling plan.
     */
    @Output
    public ScalingPlanStatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(ScalingPlanStatusCode statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * The current status of the scaling plan.
     */
    @Output
    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    /**
     * The time and date when the scaling plan entered the current status.
     */
    @Output
    public String getStatusStartTime() {
        return statusStartTime;
    }

    public void setStatusStartTime(String statusStartTime) {
        this.statusStartTime = statusStartTime;
    }

    @Override
    public void copyFrom(ScalingPlan model) {
        setCreationTime(model.creationTime().toString());
        setName(model.scalingPlanName());
        setScalingPlanVersion(model.scalingPlanVersion());
        setStatusCode(model.statusCode());
        setStatusMessage(model.statusMessage());
        setStatusStartTime(model.statusStartTime().toString());

        setApplicationSource(null);
        if (model.applicationSource() != null) {
            AutoScalingApplicationSource applicationSource = newSubresource(AutoScalingApplicationSource.class);
            applicationSource.copyFrom(model.applicationSource());
            setApplicationSource(applicationSource);
        }

        getScalingInstructions().clear();
        if (model.scalingInstructions() != null) {
            model.scalingInstructions().forEach(instruction -> {
                AutoScalingScalingInstruction scalingInstruction = newSubresource(
                    AutoScalingScalingInstruction.class);
                scalingInstruction.copyFrom(instruction);
                getScalingInstructions().add(scalingInstruction);
            });
        }

    }

    @Override
    public boolean refresh() {
        AutoScalingPlansClient client = createClient(AutoScalingPlansClient.class);
        DescribeScalingPlansResponse response;

        response = client.describeScalingPlans(r -> r.scalingPlanNames(getName()));

        if (response == null || response.scalingPlans().isEmpty()) {
            return false;
        }

        copyFrom(response.scalingPlans().get(0));
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        AutoScalingPlansClient client = createClient(AutoScalingPlansClient.class);

        client.createScalingPlan(r -> r
            .scalingPlanName(getName())
            .applicationSource(getApplicationSource() != null ? getApplicationSource().toApplicationSource() : null)
            .scalingInstructions(getScalingInstructions().stream()
                .map(AutoScalingScalingInstruction::toScalingInstruction).collect(Collectors.toList()))
        );

        refresh();
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        AutoScalingPlansClient client = createClient(AutoScalingPlansClient.class);

        client.updateScalingPlan(r -> r
            .scalingPlanName(getName())
            .applicationSource(getApplicationSource() != null ? getApplicationSource().toApplicationSource() : null)
            .scalingInstructions(getScalingInstructions().stream()
                .map(AutoScalingScalingInstruction::toScalingInstruction).collect(Collectors.toList()))
            .scalingPlanVersion(getScalingPlanVersion())
        );
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        AutoScalingPlansClient client = createClient(AutoScalingPlansClient.class);

        client.deleteScalingPlan(r -> r
            .scalingPlanName(getName())
            .scalingPlanVersion(getScalingPlanVersion())
        );
    }
}
