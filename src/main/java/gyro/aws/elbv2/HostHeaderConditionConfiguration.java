package gyro.aws.elbv2;

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.HostHeaderConditionConfig;

public class HostHeaderConditionConfiguration extends Diffable implements Copyable<HostHeaderConditionConfig> {

    private List<String> values;

    /**
     * A list of host names.
     */
    @Required
    @Updatable
    public List<String> getValues() {
        if (values == null) {
            values = new ArrayList<>();
        }

        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public void copyFrom(HostHeaderConditionConfig model) {
        setValues(model.values());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    HostHeaderConditionConfig toHostHeaderConditionConfig() {
        return HostHeaderConditionConfig.builder().values(getValues()).build();
    }
}
