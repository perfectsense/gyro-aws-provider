/*
 * Copyright 2021, Brightspot.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.elbv2;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.QueryStringConditionConfig;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.QueryStringKeyValuePair;

public class QueryStringConditionConfiguration extends Diffable implements Copyable<QueryStringConditionConfig>, ConditionValue {

    private Map<String, String> keyValuePairs;

    /**
     * The key/value pairs or values to find in the query string.
     */
    @Required
    @Updatable
    public Map<String, String> getKeyValuePairs() {
        if (keyValuePairs == null) {
            keyValuePairs = new HashMap<>();
        }

        return keyValuePairs;
    }

    public void setKeyValuePairs(Map<String, String> keyValuePairs) {
        this.keyValuePairs = keyValuePairs;
    }

    @Override
    public void copyFrom(QueryStringConditionConfig model) {
        getKeyValuePairs().clear();
        if (model.hasValues()) {
            for (QueryStringKeyValuePair pair : model.values()) {
                getKeyValuePairs().put(pair.key(), pair.value());
            }
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    QueryStringConditionConfig toQueryStringConditionConfig() {
        return QueryStringConditionConfig.builder().values(getKeyValuePairs().entrySet().stream()
            .map(r -> QueryStringKeyValuePair.builder().key(r.getKey()).value(r.getValue()).build())
            .collect(Collectors.toList())).build();
    }

    @Override
    public String getField() {
        return "query-string";
    }
}
