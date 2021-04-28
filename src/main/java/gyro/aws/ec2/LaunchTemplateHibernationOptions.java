package gyro.aws.ec2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateHibernationOptionsRequest;

public class LaunchTemplateHibernationOptions extends Diffable
    implements Copyable<software.amazon.awssdk.services.ec2.model.LaunchTemplateHibernationOptions> {

    private Boolean configured;

    /**
     * When set to ``true``, the instance in enabled for hibernation.
     */
    @Required
    @Updatable
    public Boolean getConfigured() {
        return configured;
    }

    public void setConfigured(Boolean configured) {
        this.configured = configured;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.ec2.model.LaunchTemplateHibernationOptions model) {
        setConfigured(model.configured());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    LaunchTemplateHibernationOptionsRequest toLaunchTemplateHibernationOptionsRequest() {
        return LaunchTemplateHibernationOptionsRequest.builder().configured(getConfigured()).build();
    }
}
