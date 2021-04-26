package gyro.aws.ec2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Min;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

public class LaunchTemplateElasticInferenceAccelerator extends Diffable
    implements Copyable<software.amazon.awssdk.services.ec2.model.LaunchTemplateElasticInferenceAccelerator> {

    private String type;
    private Integer count;

    /**
     * The type of elastic inference accelerator. Valid values are ``eia1.medium`` or ``eia1.large``, ``eia1.xlarge``.
     */
    @ValidStrings({ "eia1.medium", "eia1.large", "eia1.xlarge" })
    @Required
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The number of elastic inference accelerators to attach to the instance. Defaults to ``1``.
     */
    @Updatable
    @Min(1)
    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.ec2.model.LaunchTemplateElasticInferenceAccelerator model) {
        setCount(model.count());
        setType(model.type());
    }

    @Override
    public String primaryKey() {
        return getType();
    }

    software.amazon.awssdk.services.ec2.model.LaunchTemplateElasticInferenceAccelerator toLaunchTemplateElasticInferenceAccelerator() {
        return software.amazon.awssdk.services.ec2.model.LaunchTemplateElasticInferenceAccelerator.builder()
            .count(getCount()).type(getType()).build();
    }
}
