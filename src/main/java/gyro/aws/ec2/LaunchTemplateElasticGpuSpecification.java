package gyro.aws.ec2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.model.ElasticGpuSpecification;

public class LaunchTemplateElasticGpuSpecification extends Diffable implements Copyable<ElasticGpuSpecification> {

    private String type;

    /**
     * The type of Elastic Graphics accelerator. See `Elastic Graphics Basics <https://docs.aws.amazon.com/AWSEC2/latest/WindowsGuide/elastic-graphics.html#elastic-graphics-basics/>`_.
     */
    @Required
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void copyFrom(ElasticGpuSpecification model) {
        setType(model.type());
    }

    @Override
    public String primaryKey() {
        return getType();
    }

    ElasticGpuSpecification toElasticGpuSpecification() {
        return ElasticGpuSpecification.builder().type(getType()).build();
    }
}
