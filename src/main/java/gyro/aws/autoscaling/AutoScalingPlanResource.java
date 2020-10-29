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
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.autoscalingplans.AutoScalingPlansClient;
import software.amazon.awssdk.services.autoscalingplans.model.CreateScalingPlanResponse;
import software.amazon.awssdk.services.autoscalingplans.model.DescribeScalingPlansResponse;
import software.amazon.awssdk.services.autoscalingplans.model.InternalServiceException;
import software.amazon.awssdk.services.autoscalingplans.model.ScalingPlan;
import software.amazon.awssdk.services.autoscalingplans.model.ScalingPlanStatusCode;
import software.amazon.awssdk.services.autoscalingplans.model.ValidationException;

/**
 * Creates a scaling plan with the specified Application Source, Scaling Instructions, Scaling Plan Name, and Scaling
 * Plan Version.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::autoscaling-plan autoscaling-plan-example
 *        scaling-plan-name: "scaling-plan-example"
 *        scaling-plan-version: 1.0
 *
 *        application-source
 *            cloud-formation-stack-arn: "arn:aws:cloudformation:us-east-2:242040583208:stack/example-stack/e95a0400-192f-11eb-8eb9-0a36006b772c"
 *        end
 *
 *        scaling-instructions
 *            max-capacity: 100
 *            min-capacity: 0
 *            resource-id: "autoScalingGroup/test-autoscaling-group"
 *            scalable-dimension: "autoscaling:autoScalingGroup:DesiredCapacity"
 *        end
 *
 */
@Type("autoscaling-plan")
public class AutoScalingPlanResource extends AwsResource implements Copyable<ScalingPlan> {

    private AutoScalingApplicationSource applicationSource;
    private String creationTime;
    private List<AutoScalingScalingInstruction> scalingIntructions;
    private String scalingPlanName;
    private Long scalingPlanVersion;
    private ScalingPlanStatusCode statusCode;
    private String statusMessage;
    private String statusStartTime;

    /**
     * The CloudFormation stack or a set of tag filters.
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
     * The scaling instructions.
     */
    @Updatable
    @Required
    public List<AutoScalingScalingInstruction> getScalingIntructions() {
        if (scalingIntructions == null) {
            scalingIntructions = new ArrayList<>();
        }
        return scalingIntructions;
    }

    public void setScalingIntructions(List<AutoScalingScalingInstruction> scalingIntructions) {
        this.scalingIntructions = scalingIntructions;
    }

    /**
     * The name of the scaling plan.
     */
    @Id
    @Required
    public String getScalingPlanName() {
        return scalingPlanName;
    }

    public void setScalingPlanName(String scalingPlanName) {
        this.scalingPlanName = scalingPlanName;
    }

    /**
     * The version of the scaling plan.
     */
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
    @Required
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
    @Regex(value = "[\\u0020-\\uD7FF\\uE000-\\uFFFD\\uD800\\uDC00-\\uDBFF\\uDFFF\\r\\n\\t]*", message = "Alphanumeric characters and symbols excluding basic ASCII control characters.")
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
        setScalingPlanName(model.scalingPlanName());
        setScalingPlanVersion(model.scalingPlanVersion());
        setStatusCode(model.statusCode());
        setStatusMessage(model.statusMessage());
        setStatusStartTime(model.statusStartTime().toString());

        if (model.applicationSource() != null) {
            AutoScalingApplicationSource applicationSource = newSubresource(AutoScalingApplicationSource.class);
            applicationSource.copyFrom(model.applicationSource());
            setApplicationSource(applicationSource);
        } else {
            setApplicationSource(null);
        }

        if (model.scalingInstructions() != null) {
            List<AutoScalingScalingInstruction> instructions = new ArrayList<>();
            model.scalingInstructions().forEach(instruction -> {
                AutoScalingScalingInstruction scalingInstruction = newSubresource(
                    AutoScalingScalingInstruction.class);
                scalingInstruction.copyFrom(instruction);
                instructions.add(scalingInstruction);
            });
            setScalingIntructions(instructions);
        } else {
            setScalingIntructions(null);
        }

    }

    @Override
    public boolean refresh() {
        AutoScalingPlansClient client = createClient(AutoScalingPlansClient.class);
        DescribeScalingPlansResponse response = null;

        try {
            response = client.describeScalingPlans(r -> r.scalingPlanNames(getScalingPlanName()));
        } catch (InternalServiceException ex) {
            // Service encountered an internal error
        } catch (ValidationException ex) {
            // Service encountered a validation issue
        }

        if (response == null) {
            return false;
        }

        copyFrom(response.scalingPlans().get(0));
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        AutoScalingPlansClient client = createClient(AutoScalingPlansClient.class);

        CreateScalingPlanResponse response = client.createScalingPlan(r -> r
            .scalingPlanName(getScalingPlanName())
            .applicationSource(getApplicationSource() != null ? getApplicationSource().toApplicationSource() : null)
            .scalingInstructions(getScalingIntructions().stream()
                .map(AutoScalingScalingInstruction::toScalingInstruction).collect(Collectors.toList()))
        );

        setScalingPlanVersion(response.scalingPlanVersion());
        refresh();
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

        AutoScalingPlansClient client = createClient(AutoScalingPlansClient.class);

        client.updateScalingPlan(r -> r
            .scalingPlanName(getScalingPlanName())
            .applicationSource(getApplicationSource() != null ? getApplicationSource().toApplicationSource() : null)
            .scalingInstructions(getScalingIntructions().stream()
                .map(AutoScalingScalingInstruction::toScalingInstruction).collect(Collectors.toList()))
        );
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        AutoScalingPlansClient client = createClient(AutoScalingPlansClient.class);
        client.deleteScalingPlan(r -> r
            .scalingPlanName(getScalingPlanName())
            .scalingPlanVersion(getScalingPlanVersion())
        );
    }
}
