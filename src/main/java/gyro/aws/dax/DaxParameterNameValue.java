/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.dax;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.dax.model.ParameterNameValue;

public class DaxParameterNameValue extends Diffable implements Copyable<ParameterNameValue> {

    private String name;
    private String value;

    /**
     * The name of the parameter.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The value of the parameter.
     */
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void copyFrom(ParameterNameValue model) {
        setName(model.parameterName());
        setValue(model.parameterValue());
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    public static ParameterNameValue toParameterNameValues(DaxParameterNameValue daxParameterNameValue) {
        return ParameterNameValue.builder()
            .parameterName(daxParameterNameValue.getName())
            .parameterValue(daxParameterNameValue.getValue())
            .build();
    }
}
