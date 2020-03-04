/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.neptune;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.neptune.model.Parameter;

public class NeptuneParameter extends Diffable implements Copyable<Parameter> {

    private String name;
    private String value;
    private String description;
    private String dataType;
    private String allowedValues;
    private String applyMethod;
    private String applyType;
    private String minimumEngineVersion;
    private String source;
    private Boolean isModifiable;

    /**
     * The name of the Neptune parameter. (Required)
     */
    @Required
    @Output
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The value of the Neptune parameter. (Required)
     */
    @Required
    @Updatable
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * The description of the Neptune parameter.
     */
    @Output
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The valid data type for the Neptune parameter's value.
     */
    @Output
    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * The valid range of values for the Neptune parameter.
     */
    @Output
    public String getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(String allowedValues) {
        this.allowedValues = allowedValues;
    }

    /**
     * Indicates when to apply parameter updates.
     */
    @Output
    public String getApplyMethod() {
        return applyMethod;
    }

    public void setApplyMethod(String applyMethod) {
        this.applyMethod = applyMethod;
    }

    /**
     * Specifies the the engine specific parameters type.
     */
    @Output
    public String getApplyType() {
        return applyType;
    }

    public void setApplyType(String applyType) {
        this.applyType = applyType;
    }

    /**
     * Specifies the earliest engine version to which the Neptune parameter can apply.
     */
    @Output
    public String getMinimumEngineVersion() {
        return minimumEngineVersion;
    }

    public void setMinimumEngineVersion(String minimumEngineVersion) {
        this.minimumEngineVersion = minimumEngineVersion;
    }

    /**
     * Indicates the source of the Neptune parameter's value.
     */
    @Output
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Indicates whether the Neptune parameter can be modified.
     */
    @Output
    public Boolean isModifiable() {
        return isModifiable;
    }

    public void setModifiable(Boolean modifiable) {
        isModifiable = modifiable;
    }

    @Override
    public void copyFrom(Parameter model) {
        setAllowedValues(model.allowedValues());
        setApplyMethod(model.applyMethodAsString());
        setApplyType(model.applyType());
        setDataType(model.dataType());
        setDescription(model.description());
        setMinimumEngineVersion(model.minimumEngineVersion());
        setModifiable(model.isModifiable());
        setName(model.parameterName());
        setSource(model.source());
        setValue(model.parameterValue());
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    public Parameter toParameter() {
        return Parameter.builder().allowedValues(getAllowedValues()).applyMethod(getApplyMethod())
                .applyType(getApplyType()).dataType(getDataType()).description(getDescription())
                .isModifiable(isModifiable()).minimumEngineVersion(getMinimumEngineVersion())
                .parameterName(getName()).parameterValue(getValue()).source(getSource()).build();
    }
}
