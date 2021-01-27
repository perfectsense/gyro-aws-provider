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
