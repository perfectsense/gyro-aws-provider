package gyro.aws.elbv2;

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.HttpRequestMethodConditionConfig;

public class HttpRequestMethodConditionConfiguration extends Diffable implements Copyable<HttpRequestMethodConditionConfig> {

    private List<String> values;

    /**
     * The name of the request method.
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
    public void copyFrom(HttpRequestMethodConditionConfig model) {
        setValues(model.values());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    HttpRequestMethodConditionConfig toHttpRequestMethodConditionConfig() {
        return HttpRequestMethodConditionConfig.builder().values(getValues()).build();
    }
}


