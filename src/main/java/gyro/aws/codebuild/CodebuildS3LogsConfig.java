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
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.codebuild.model.S3LogsConfig;

public class CodebuildS3LogsConfig extends Diffable implements Copyable<S3LogsConfig> {

    private String status;
    private Boolean encryptionDisabled;
    private String location;

    /**
     * The current status of the S3 build logs.
     */
    @Updatable
    @Required
    @ValidStrings({ "ENABLED", "DISABLED" })
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * When set to ``true`` the S3 build log output is not encrypted. When set to ``false`` the S3 build log output is
     * encrypted.
     */
    @Updatable
    public Boolean getEncryptionDisabled() {
        return encryptionDisabled;
    }

    public void setEncryptionDisabled(Boolean encryptionDisabled) {
        this.encryptionDisabled = encryptionDisabled;
    }

    /**
     * The ARN of the S3 bucket and the path prefix for the S3 logs.
     */
    @Updatable
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public void copyFrom(S3LogsConfig model) {
        setStatus(model.statusAsString());
        setEncryptionDisabled(model.encryptionDisabled());
        setLocation(model.location());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public S3LogsConfig toS3LogsConfig() {
        return S3LogsConfig.builder()
            .encryptionDisabled(getEncryptionDisabled())
            .location(getLocation())
            .status(getStatus())
            .build();
    }
}
