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

package gyro.aws.ecs;

import java.util.HashMap;
import java.util.Map;

import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ecs.model.FirelensConfiguration;
import software.amazon.awssdk.services.ecs.model.FirelensConfigurationType;

public class EcsFirelensConfiguration extends Diffable {

    private FirelensConfigurationType type;
    private Map<String, String> options;

    /**
     * The log router to use. (Required)
     * Valid values are ``fluentd`` or ``fluentbit``.
     */
    @Required
    public FirelensConfigurationType getType() {
        return type;
    }

    public void setType(FirelensConfigurationType type) {
        this.type = type;
    }

    /**
     * The options to use when configuring the log router.
     * This field can be used to specify a custom configuration file or to add additional metadata, such as the task, task definition, cluster, and container instance details to the log event.
     */
    public Map<String, String> getOptions() {
        if (options == null) {
            options = new HashMap<>();
        }

        return options;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public void copyFrom(FirelensConfiguration model) {
        setType(model.type());
        setOptions(model.options());
    }

    public FirelensConfiguration copyTo() {
        return FirelensConfiguration.builder()
            .type(getType())
            .options(getOptions())
            .build();
    }
}
