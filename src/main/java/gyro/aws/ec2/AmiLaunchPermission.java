package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.model.LaunchPermission;

public class AmiLaunchPermission extends Diffable implements Copyable<LaunchPermission> {
    private String userId;

    /**
     * The AWS Account ID for the permission.
     */
    @Required
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String primaryKey() {
        return getUserId();
    }

    @Override
    public void copyFrom(LaunchPermission permission) {
        setUserId(permission.userId());
    }

    LaunchPermission toLaunchPermission() {
        return LaunchPermission.builder()
            .userId(getUserId())
            .build();
    }
}
