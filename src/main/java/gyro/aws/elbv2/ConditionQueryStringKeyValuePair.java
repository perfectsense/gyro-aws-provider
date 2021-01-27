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
