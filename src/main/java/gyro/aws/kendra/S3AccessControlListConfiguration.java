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

package gyro.aws.kendra;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.kendra.model.AccessControlListConfiguration;

public class S3AccessControlListConfiguration extends Diffable implements Copyable<AccessControlListConfiguration> {

    private String keyPath;

    /**
     * The path to the AWS S3 bucket that contains the ACL files. (Required)
     */
    @Updatable
    @Required
    public String getKeyPath() {
        return keyPath;
    }

    public void setKeyPath(String keyPath) {
        this.keyPath = keyPath;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(AccessControlListConfiguration model) {
        setKeyPath(model.keyPath());
    }

    public AccessControlListConfiguration toAccessControlListConfiguration() {
        return AccessControlListConfiguration.builder().keyPath(getKeyPath()).build();
    }
}
