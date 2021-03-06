/*
 * Copyright 2019, Perfect Sense, Inc.
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

package gyro.aws.elasticache;

import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.elasticache.model.ParameterNameValue;

public class CacheParameter extends Diffable {
    private String name;
    private String value;

    public CacheParameter() {

    }

    public CacheParameter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * The name of the cache parameter variable.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The value of the cache parameter variable.
     */
    @Updatable
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    ParameterNameValue getParameterNameValue() {
        return ParameterNameValue.builder()
            .parameterName(getName())
            .parameterValue(getValue())
            .build();
    }
}
