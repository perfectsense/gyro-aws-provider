package gyro.aws.elbv2;

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.PathPatternConditionConfig;

public class PathPatternConditionConfiguration extends Diffable implements Copyable<PathPatternConditionConfig> {

    private List<String> values;

    /**
     * The path patterns to compare against the request URL.
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
    public void copyFrom(PathPatternConditionConfig model) {
        setValues(model.values());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    PathPatternConditionConfig toPathPatternConditionConfig() {
        return PathPatternConditionConfig.builder().values(getValues()).build();
    }
}
