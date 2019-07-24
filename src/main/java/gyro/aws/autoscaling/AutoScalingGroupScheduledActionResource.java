package gyro.aws.autoscaling;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Min;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.DescribeScheduledActionsResponse;
import software.amazon.awssdk.services.autoscaling.model.ScheduledUpdateGroupAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class AutoScalingGroupScheduledActionResource extends AwsResource implements Copyable<ScheduledUpdateGroupAction> {

    private String scheduledActionName;
    private Integer desiredCapacity;
    private Integer maxSize;
    private Integer minSize;
    private String recurrence;
    private Date startTime;
    private Date endTime;
    private String arn;

    /**
     * The scheduled action name. (Required)
     */
    public String getScheduledActionName() {
        return scheduledActionName;
    }

    public void setScheduledActionName(String scheduledActionName) {
        this.scheduledActionName = scheduledActionName;
    }

    /**
     * The desired capacity of instances this scheduled action to scale to.
     */
    @Min(0)
    @Updatable
    public Integer getDesiredCapacity() {
        return desiredCapacity;
    }

    public void setDesiredCapacity(Integer desiredCapacity) {
        this.desiredCapacity = desiredCapacity;
    }

    /**
     * The maximum number of instances this scheduled action to scale to.
     */
    @Min(0)
    @Updatable
    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    /**
     * The minimum number of instances this scheduled action to scale to.
     */
    @Min(0)
    @Updatable
    public Integer getMinSize() {
        return minSize;
    }

    public void setMinSize(Integer minSize) {
        this.minSize = minSize;
    }

    /**
     * The recurring schedule for this action, in Unix cron syntax format
     */
    @Updatable
    public String getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(String recurrence) {
        this.recurrence = recurrence;
    }

    /**
     * The time for this action to start.
     */
    @Updatable
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * The time for this action to stop.
     */
    @Updatable
    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * The arn of the scheduled action resource.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public void copyFrom(ScheduledUpdateGroupAction scheduledUpdateGroupAction) {
        setScheduledActionName(scheduledUpdateGroupAction.scheduledActionName());
        setDesiredCapacity(scheduledUpdateGroupAction.desiredCapacity());
        setMaxSize(scheduledUpdateGroupAction.maxSize());
        setMinSize(scheduledUpdateGroupAction.minSize());
        setRecurrence(scheduledUpdateGroupAction.recurrence());
        setStartTime(scheduledUpdateGroupAction.startTime() != null ? Date.from(scheduledUpdateGroupAction.startTime()) : null);
        setEndTime(scheduledUpdateGroupAction.endTime() != null ? Date.from(scheduledUpdateGroupAction.endTime()) : null);
        setArn(scheduledUpdateGroupAction.scheduledActionARN());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        validate();
        saveScheduledAction(client);

        //set arn
        DescribeScheduledActionsResponse response = client.describeScheduledActions(
            r -> r.autoScalingGroupName(getParentId())
            .scheduledActionNames(Collections.singleton(getScheduledActionName()))
        );

        if (!response.scheduledUpdateGroupActions().isEmpty()) {
            setArn(response.scheduledUpdateGroupActions().get(0).scheduledActionARN());
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        validate();
        saveScheduledAction(client);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        client.deleteScheduledAction(
            r -> r.autoScalingGroupName(getParentId())
            .scheduledActionName(getScheduledActionName())
        );
    }

    @Override
    public String primaryKey() {
        return String.format("%s", getScheduledActionName());
    }

    private String getParentId() {
        AutoScalingGroupResource parent = (AutoScalingGroupResource) parentResource();
        if (parent == null) {
            throw new GyroException("Parent Auto Scale Group resource not found.");
        }
        return parent.getAutoScalingGroupName();
    }

    private void saveScheduledAction(AutoScalingClient client) {
        client.putScheduledUpdateGroupAction(
            r -> r.scheduledActionName(getScheduledActionName())
                .autoScalingGroupName(getParentId())
                .desiredCapacity(getDesiredCapacity())
                .maxSize(getMaxSize())
                .minSize(getMinSize())
                .recurrence(getRecurrence())
                .startTime(getStartTime() != null ? getStartTime().toInstant() : null)
                .endTime(getEndTime() != null ? getEndTime().toInstant() : null)
        );
    }

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (getMaxSize() == null && getMinSize() == null && getDesiredCapacity() == null) {
            errors.add(new ValidationError(this, null, "At least one of the params 'max-size' or 'min-size' or 'desired-capacity' needs to be provided."));
        }

        if (getMinSize() != null && getMaxSize() != null && (getMaxSize() < getMinSize())) {
            errors.add(new ValidationError(this, null, "The value - (" + getMinSize()
                + ") for parameter 'min-size' needs to be equal or smaller than the value - (" + getMaxSize()
                + ") for parameter 'max-size'."));
        }

        return errors;
    }
}
