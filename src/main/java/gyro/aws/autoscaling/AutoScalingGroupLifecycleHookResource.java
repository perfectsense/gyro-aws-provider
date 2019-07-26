package gyro.aws.autoscaling;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.RoleResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.diff.Context;
import gyro.core.validation.Range;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.LifecycleHook;

import java.util.Set;

public class AutoScalingGroupLifecycleHookResource extends AwsResource implements Copyable<LifecycleHook> {

    private String lifecycleHookName;
    private String defaultResult;
    private Integer heartbeatTimeout;
    private String lifecycleTransition;
    private String notificationMetadata;
    private String notificationTargetArn;
    private RoleResource role;
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
     * The action the Auto Scaling group should take when the lifecycle hook timeout elapses. Defaults to ABANDON. Valid values are ``ABANDON`` or ``CONTINUE``.
     */
    @Updatable
    @ValidStrings({ "CONTINUE", "ABANDON" })
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
     * The max time in seconds after which the lifecycle hook times out. Defaults to 3600. Valid values are Integer between ``30`` and ``7200``.
     */
    @Range(min = 30, max = 7200)
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
     * The instance state to which this lifecycle hook is being attached. Defaults to 'autoscaling:EC2_INSTANCE_LAUNCHING'. Valid values are ``autoscaling:EC2_INSTANCE_LAUNCHING`` or ``autoscaling:EC2_INSTANCE_TERMINATING``.
     */
    @Updatable
    @ValidStrings({ "autoscaling:EC2_INSTANCE_LAUNCHING", "autoscaling:EC2_INSTANCE_TERMINATING" })
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
     * An IAM role that allows publication to the specified notification target.
     */
    @Updatable
    public RoleResource getRole() {
        return role;
    }

    public void setRole(RoleResource role) {
        this.role = role;
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
        setDefaultResult(lifecycleHook.defaultResult());
        setHeartbeatTimeout(lifecycleHook.heartbeatTimeout());
        setLifecycleTransition(lifecycleHook.lifecycleTransition());
        setNotificationMetadata(lifecycleHook.notificationMetadata());
        setNotificationTargetArn(lifecycleHook.notificationTargetARN());
        setRole(!ObjectUtils.isBlank(lifecycleHook.roleARN()) ? findById(RoleResource.class, lifecycleHook.roleARN()) : null);
        setGlobalTimeout(lifecycleHook.globalTimeout());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, Context context) {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        validate();
        saveLifecycleHook(client);
    }

    @Override
    public void update(GyroUI ui, Context context, Resource current, Set<String> changedFieldNames) {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        validate();
        saveLifecycleHook(client);
    }

    @Override
    public void delete(GyroUI ui, Context context) {
        AutoScalingClient client = createClient(AutoScalingClient.class);

        client.deleteLifecycleHook(
            r -> r.autoScalingGroupName(getParentId())
            .lifecycleHookName(getLifecycleHookName())
        );
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
                .autoScalingGroupName(getParentId())
                .defaultResult(getDefaultResult())
                .heartbeatTimeout(getHeartbeatTimeout())
                .lifecycleTransition(getLifecycleTransition())
                .notificationMetadata(getNotificationMetadata())
                .notificationTargetARN(getNotificationTargetArn())
                .roleARN(getRole() != null ? getRole().getArn() : null)
        );
    }

}
