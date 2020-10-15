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

package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.codebuild.model.EnvironmentVariable;
import software.amazon.awssdk.services.codebuild.model.EnvironmentVariableType;

public class CodebuildProjectEnvironmentVariable extends Diffable implements Copyable<EnvironmentVariable> {

    private String name;
    private String value;
    private EnvironmentVariableType type;

    /**
     * The name or key of the environment variable.
     */
    @Updatable
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The value of the environment variable.
     */
    @Updatable
    @Required
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * The type of the environment variable.
     */
    @Updatable
    public EnvironmentVariableType getType() {
        return type;
    }

    public void setType(EnvironmentVariableType type) {
        this.type = type;
    }

    @Override
    public void copyFrom(EnvironmentVariable model) {
        setName(model.name());
        setValue(model.value());
        setType(model.type());
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    public EnvironmentVariable toEnvironmentVariable() {
        return EnvironmentVariable.builder()
            .name(getName())
            .type(getType())
            .value(getValue())
            .build();
    }
}
