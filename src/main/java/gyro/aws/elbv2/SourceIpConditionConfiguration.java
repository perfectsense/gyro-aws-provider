package gyro.aws.elbv2;

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.SourceIpConditionConfig;

public class SourceIpConditionConfiguration extends Diffable implements Copyable<SourceIpConditionConfig> {

    private List<String> values;

    /**
     * The source IP addresses, in CIDR format.
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
    public void copyFrom(SourceIpConditionConfig model) {
        setValues(model.values());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    SourceIpConditionConfig toSourceIpConditionConfig() {
        return SourceIpConditionConfig.builder().values(getValues()).build();
    }
}

