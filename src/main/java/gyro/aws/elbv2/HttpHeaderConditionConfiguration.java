package gyro.aws.elbv2;

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.HttpHeaderConditionConfig;

public class HttpHeaderConditionConfiguration extends Diffable implements Copyable<HttpHeaderConditionConfig> {

    private String headerName;
    private List<String> values;

    /**
     * The name of the HTTP header field.
     */
    @Required
    @Updatable
    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    /**
     * The strings to compare against the value of the HTTP header.
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
    public void copyFrom(HttpHeaderConditionConfig model) {
        setHeaderName(model.httpHeaderName());
        setValues(model.values());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    HttpHeaderConditionConfig toHttpHeaderConditionConfig() {
        return HttpHeaderConditionConfig.builder().values(getValues()).build();
    }
}
