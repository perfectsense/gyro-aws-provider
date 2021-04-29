package gyro.aws.ec2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.model.LaunchTemplatesMonitoring;
import software.amazon.awssdk.services.ec2.model.LaunchTemplatesMonitoringRequest;

public class LaunchTemplateMonitoring extends Diffable implements Copyable<LaunchTemplatesMonitoring> {

    private Boolean enabled;

    /**
     * When set to ``true``, monitoring for your instance is enabled.
     */
    @Required
    @Updatable
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void copyFrom(LaunchTemplatesMonitoring model) {
        setEnabled(model.enabled());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    LaunchTemplatesMonitoringRequest toLaunchTemplatesMonitoringRequest() {
        return LaunchTemplatesMonitoringRequest.builder().enabled(getEnabled()).build();
    }
}
