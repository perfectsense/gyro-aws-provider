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

package gyro.aws.rds;

import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

public class DbParameter extends Diffable {

    private String name;
    private String value;
    private String applyMethod;

    /**
     * The name of the DB parameter. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The value of the DB parameter. (Required)
     */
    @Updatable
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * The timing to apply parameter updates. Valid values are ``immediate`` (default) or ``pending-reboot``.
     */
    public String getApplyMethod() {
        if (applyMethod == null) {
            applyMethod = "immediate";
        }

        return applyMethod;
    }

    public void setApplyMethod(String applyMethod) {
        this.applyMethod = applyMethod;
    }

    @Override
    public String primaryKey() {
        return getName();
    }

}
