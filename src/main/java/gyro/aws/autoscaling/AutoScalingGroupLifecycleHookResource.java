package gyro.aws.autoscaling;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.LifecycleHook;

import java.util.Set;

public class AutoScalingGroupLifecycleHookResource extends AwsResource implements Copyable<LifecycleHook> {

    private String lifecycleHookName;
    private String autoScalingGroupName;
    private String defaultResult;
    private Integer heartbeatTimeout;
    private String lifecycleTransition;
    private String notificationMetadata;
    private String notificationTargetArn;
    private String roleArn;
    private Integer globalTimeout;

    /**
     * The name of the lifecycle hook. (Required)
     */
    public String getLifecycleHookName() {
        return lifecycleHookName;
    }

    public void setLifecycleHookName(String lifecycleHookName) {
        this.lifecycleHookName = lifecycleHookName;
    }

    /**
     * The name of the parent auto scaling group. (Auto populated)
     */
    public String getAutoScalingGroupName() {
        return autoScalingGroupName;
    }

    public void setAutoScalingGroupName(String autoScalingGroupName) {
        this.autoScalingGroupName = autoScalingGroupName;
    }

    /**
     * The action the Auto Scaling group should take when the lifecycle hook timeout elapses. Defaults to ABANDON.
     * Valid values [ 'ABANDON', 'CONTINUE' ].
     */
    @Updatable
    public String getDefaultResult() {
        if (defaultResult == null) {
            defaultResult = "ABANDON";
        }

        return defaultResult;
    }

    public void setDefaultResult(String defaultResult) {
        this.defaultResult = defaultResult;
    }

    /**
     * The max time in seconds after which the lifecycle hook times out. Defaults to 3600. Valid values [ Integer between 30 and 7200 ].
     */
    @Updatable
    public Integer getHeartbeatTimeout() {
        if (heartbeatTimeout == null) {
            heartbeatTimeout = 3600;
        }

        return heartbeatTimeout;
    }

    public void setHeartbeatTimeout(Integer heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }

    /**
     * The instance state to which this lifecycle hook is being attached. Defaults to 'autoscaling:EC2_INSTANCE_LAUNCHING'.
     * Valid values [ 'autoscaling:EC2_INSTANCE_LAUNCHING', 'autoscaling:EC2_INSTANCE_TERMINATING' ].
     */
    @Updatable
    public String getLifecycleTransition() {
        if (lifecycleTransition == null) {
            lifecycleTransition = "autoscaling:EC2_INSTANCE_LAUNCHING";
        }

        return lifecycleTransition;
    }

    public void setLifecycleTransition(String lifecycleTransition) {
        this.lifecycleTransition = lifecycleTransition;
    }

    /**
     * Additional information to be included in the notification to the notification target.
     */
    @Updatable
    public String getNotificationMetadata() {
        return notificationMetadata;
    }

    public void setNotificationMetadata(String notificationMetadata) {
        this.notificationMetadata = notificationMetadata;
    }

    /**
     * The ARN of the notification target. Can be SQS or SNS.
     */
    @Updatable
    public String getNotificationTargetArn() {
        return notificationTargetArn;
    }

    public void setNotificationTargetArn(String notificationTargetArn) {
        this.notificationTargetArn = notificationTargetArn;
    }

    /**
     * The ARN of an IAM role that allows publication to the specified notification target.
     */
    @Updatable
    public String getRoleArn() {
        return roleArn;
    }

    public void setRoleArn(String roleArn) {
        this.roleArn = roleArn;
    }

    /**
     * The value set as the global timeout.
     */
    @Output
    public Integer getGlobalTimeout() {
        return globalTimeout;
    }

    public void setGlobalTimeout(Integer globalTimeout) {
        this.globalTimeout = globalTimeout;
    }

    @Override
    public void copyFrom(LifecycleHook lifecycleHook) {
        setLifecycleHookName(lifecycleHook.lifecycleHookName());
        setAutoScalingGroupName(lifecycleHook.autoScalingGroupName());
        setDefaultResult(lifecycleHook.defaultResult());
        setHeartbeatTimeout(lifecycleHook.heartbeatTimeout());
        setLifecycleTransition(lifecycleHook.lifecycleTransition());
        setNotificationMetadata(lifecycleHook.notificationMetadata());
        setNotificationTargetArn(lifecycleHook.notificationTargetARN());
        setRoleArn(lifecycleHook.roleARN());
        setGlobalTimeout(lifecycleHook.globalTimeout());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        validate();
        setAutoScalingGroupName(getParentId());
        saveLifecycleHook(client);
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        validate();
        saveLifecycleHook(client);
    }

    @Override
    public void delete() {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        client.deleteLifecycleHook(
            r -> r.autoScalingGroupName(getAutoScalingGroupName())
            .lifecycleHookName(getLifecycleHookName())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("lifecycle hook");

        if (!ObjectUtils.isBlank(getLifecycleHookName())) {
            sb.append(" - ").append(getLifecycleHookName());
        }

        return sb.toString();
    }

    @Override
    public String primaryKey() {
        return String.format("%s", getLifecycleHookName());
    }

    private String getParentId() {
        AutoScalingGroupResource parent = (AutoScalingGroupResource) parentResource();
        if (parent == null) {
            throw new GyroException("Parent Auto Scale Group resource not found.");
        }
        return parent.getAutoScalingGroupName();
    }

    private void saveLifecycleHook(AutoScalingClient client) {
        client.putLifecycleHook(
            r -> r.lifecycleHookName(getLifecycleHookName())
                .autoScalingGroupName(getAutoScalingGroupName())
                .defaultResult(getDefaultResult())
                .heartbeatTimeout(getHeartbeatTimeout())
                .lifecycleTransition(getLifecycleTransition())
                .notificationMetadata(getNotificationMetadata())
                .notificationTargetARN(getNotificationTargetArn())
                .roleARN(getRoleArn())
        );
    }

    private void validate() {
        if (!getLifecycleTransition().equals("autoscaling:EC2_INSTANCE_LAUNCHING")
            && !getLifecycleTransition().equals("autoscaling:EC2_INSTANCE_TERMINATING")) {
            throw new GyroException("Invalid value '" + getLifecycleTransition() + "' for the param 'lifecycle-transition'."
                + " Valid options ['autoscaling:EC2_INSTANCE_LAUNCHING', 'autoscaling:EC2_INSTANCE_TERMINATING'].");
        }

        if (getHeartbeatTimeout() < 30 || getHeartbeatTimeout() > 7200) {
            throw new GyroException("The value - (" + getHeartbeatTimeout()
                + ") is invalid for param 'heartbeat-timeout'. Valid values [ Integer value between 30 and 7200 ].");
        }

        if (!getDefaultResult().equals("CONTINUE") && !getDefaultResult().equals("ABANDON")) {
            throw new GyroException("Invalid value '" + getDefaultResult() + "' for the param 'default-result'."
                + " Valid options ['CONTINUE', 'ABANDON'].");
        }
    }
}
