package gyro.aws.elbv2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.QueryStringKeyValuePair;

public class ConditionQueryStringKeyValuePair extends Diffable implements Copyable<QueryStringKeyValuePair> {

    private String key;
    private String value;

    /**
     * The key of the query.
     */
    @Updatable
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * The value of the query.
     */
    @Required
    @Updatable
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void copyFrom(QueryStringKeyValuePair model) {
        setKey(model.key());
        setValue(model.value());
    }

    @Override
    public String primaryKey() {
        return String.format("Key: %s, Value: %s", getKey(), getValue());
    }

    QueryStringKeyValuePair toQueryStringKeyValuePair() {
        return QueryStringKeyValuePair.builder().key(getKey()).value(getValue()).build();
    }
}
