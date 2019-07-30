package gyro.aws.ec2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.ec2.model.LaunchTemplateSpecification;

public class LaunchTemplateSpecificationResource extends Diffable implements Copyable<LaunchTemplateSpecification> {
    private LaunchTemplateResource launchTemplate;
    private String version;

    /**
     * The Launch Template to use for creating the instance.
     */
    public LaunchTemplateResource getLaunchTemplate() {
        return launchTemplate;
    }

    public void setLaunchTemplate(LaunchTemplateResource launchTemplate) {
        this.launchTemplate = launchTemplate;
    }

    /**
     * The version of the launch template to use.
     */
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public void copyFrom(LaunchTemplateSpecification launchTemplateSpecification) {
        setLaunchTemplate(findById(LaunchTemplateResource.class,launchTemplateSpecification.launchTemplateId()));
        setVersion(launchTemplateSpecification.version());
    }

    @Override
    public String primaryKey() {
        return "launch template specification";
    }

    LaunchTemplateSpecification toLaunchTemplateSpecification() {
        return LaunchTemplateSpecification.builder()
            .version(getVersion())
            .launchTemplateId(getLaunchTemplate().getId())
            .build();
    }
}
