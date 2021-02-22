package gyro.aws.autoscaling;

import gyro.aws.Copyable;
import gyro.aws.ec2.LaunchTemplateResource;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.autoscaling.model.LaunchTemplate;
import software.amazon.awssdk.services.autoscaling.model.LaunchTemplateSpecification;
import software.amazon.awssdk.services.autoscaling.model.MixedInstancesPolicy;

public class AutoScalingGroupMixedInstancesPolicy extends Diffable implements Copyable<MixedInstancesPolicy> {

    private AutoScalingGroupInstancesDistribution instancesDistribution;
    private LaunchTemplateResource launchTemplate;

    /**
     * The instances distribution of the mixed instances policy.
     *
     * @subresource gyro.aws.autoscaling.AutoScalingGroupInstancesDistribution
     */
    public AutoScalingGroupInstancesDistribution getInstancesDistribution() {
        return instancesDistribution;
    }

    public void setInstancesDistribution(AutoScalingGroupInstancesDistribution instancesDistribution) {
        this.instancesDistribution = instancesDistribution;
    }

    /**
     * The launch template that would be used as a skeleton to create the Auto Scaling group.
     */
    public LaunchTemplateResource getLaunchTemplate() {
        return launchTemplate;
    }

    public void setLaunchTemplate(LaunchTemplateResource launchTemplate) {
        this.launchTemplate = launchTemplate;
    }

    @Override
    public void copyFrom(MixedInstancesPolicy model) {
        setInstancesDistribution(null);
        if (model.instancesDistribution() != null) {
            AutoScalingGroupInstancesDistribution distribution = newSubresource(
                AutoScalingGroupInstancesDistribution.class);
            distribution.copyFrom(model.instancesDistribution());
            setInstancesDistribution(distribution);
        }

        setLaunchTemplate(launchTemplate == null
            ? null
            : findById(LaunchTemplateResource.class, launchTemplate.getId()));
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public MixedInstancesPolicy toMixedInstancesPolicy() {
        return MixedInstancesPolicy.builder()
            .instancesDistribution(getInstancesDistribution().toInstancesDistribution())
            .launchTemplate(LaunchTemplate.builder().launchTemplateSpecification(
                LaunchTemplateSpecification.builder()
                    .launchTemplateId(getLaunchTemplate() != null ? getLaunchTemplate().getId() : null)
                    .build())
                .build())
            .build();
    }
}
