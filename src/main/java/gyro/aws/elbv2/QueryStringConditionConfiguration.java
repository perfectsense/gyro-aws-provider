package gyro.aws.elbv2;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.QueryStringConditionConfig;

public class QueryStringConditionConfiguration extends Diffable implements Copyable<QueryStringConditionConfig> {

    private List<ConditionQueryStringKeyValuePair> keyValuePairs;

    /**
     * The key/value pairs or values to find in the query string.
     */
    @Required
    @Updatable
    public List<ConditionQueryStringKeyValuePair> getKeyValuePairs() {
        if (keyValuePairs == null) {
            keyValuePairs = new ArrayList<>();
        }

        return keyValuePairs;
    }

    public void setKeyValuePairs(List<ConditionQueryStringKeyValuePair> keyValuePairs) {
        this.keyValuePairs = keyValuePairs;
    }

    @Override
    public void copyFrom(QueryStringConditionConfig model) {
        getKeyValuePairs().clear();
        if (model.hasValues()) {
            setKeyValuePairs(model.values().stream().map(r -> {
                ConditionQueryStringKeyValuePair pair = newSubresource(ConditionQueryStringKeyValuePair.class);
                pair.copyFrom(r);
                return pair;
            }).collect(Collectors.toList()));
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    QueryStringConditionConfig toQueryStringConditionConfig() {
        return QueryStringConditionConfig.builder().values(getKeyValuePairs().stream()
            .map(ConditionQueryStringKeyValuePair::toQueryStringKeyValuePair).collect(Collectors.toList())).build();
    }
}
