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
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.autoscalingplans.AutoScalingPlansClient;
import software.amazon.awssdk.services.autoscalingplans.model.CreateScalingPlanResponse;
import software.amazon.awssdk.services.autoscalingplans.model.DescribeScalingPlansResponse;
import software.amazon.awssdk.services.autoscalingplans.model.InternalServiceException;
import software.amazon.awssdk.services.autoscalingplans.model.ScalingPlan;
import software.amazon.awssdk.services.autoscalingplans.model.ScalingPlanStatusCode;
import software.amazon.awssdk.services.autoscalingplans.model.ValidationException;

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

    @Updatable
    @ConflictsWith("scaling-plan-name")
    @Required
    public AutoScalingApplicationSource getApplicationSource() {
        return applicationSource;
    }

    public void setApplicationSource(AutoScalingApplicationSource applicationSource) {
        this.applicationSource = applicationSource;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }

    @Updatable
    @Required
    public List<AutoScalingScalingInstruction> getScalingIntructions() {
        return scalingIntructions;
    }

    public void setScalingIntructions(List<AutoScalingScalingInstruction> scalingIntructions) {
        this.scalingIntructions = scalingIntructions;
    }

    @Id
    @ConflictsWith("application-source")
    @Required
    public String getScalingPlanName() {
        return scalingPlanName;
    }

    public void setScalingPlanName(String scalingPlanName) {
        this.scalingPlanName = scalingPlanName;
    }

    public Long getScalingPlanVersion() {
        return scalingPlanVersion;
    }

    public void setScalingPlanVersion(Long scalingPlanVersion) {
        this.scalingPlanVersion = scalingPlanVersion;
    }

    @Required
    public ScalingPlanStatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(ScalingPlanStatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

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
