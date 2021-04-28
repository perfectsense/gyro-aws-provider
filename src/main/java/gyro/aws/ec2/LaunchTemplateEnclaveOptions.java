package gyro.aws.ec2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateEnclaveOptionsRequest;

public class LaunchTemplateEnclaveOptions extends Diffable
    implements Copyable<software.amazon.awssdk.services.ec2.model.LaunchTemplateEnclaveOptions> {

    private Boolean enabled;

    /**
     * When set to ``true`` the instance for AWS Nitro Enclaves is enabled.
     */
    @Required
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.ec2.model.LaunchTemplateEnclaveOptions model) {
        setEnabled(model.enabled());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    LaunchTemplateEnclaveOptionsRequest toLaunchTemplateEnclaveOptionsRequest() {
        return LaunchTemplateEnclaveOptionsRequest.builder().enabled(getEnabled()).build();
    }
}
