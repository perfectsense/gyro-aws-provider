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

import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ecs.model.SystemControl;

public class EcsSystemControl extends Diffable {

    private String namespace;
    private String value;

    /**
     * The namespaced kernel parameter for which to set a ``value``. (Required)
     */
    @Required
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * The value for the namespaced kernel parameter specified in ``namespace``.
     */
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String primaryKey() {
        return getNamespace();
    }

    public void copyFrom(SystemControl model) {
        setNamespace(model.namespace());
        setValue(model.value());
    }

    public SystemControl copyTo() {
        return SystemControl.builder()
            .namespace(getNamespace())
            .value(getValue())
            .build();
    }
}
