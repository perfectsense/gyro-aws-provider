/*
 * Copyright 2024, Brightspot.
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

package gyro.aws.opensearch;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ConflictsWith;
import software.amazon.awssdk.services.opensearch.model.MasterUserOptions;

public class OpenSearchMasterUserOptions extends Diffable implements Copyable<MasterUserOptions> {

    private String masterUsername;
    private String masterPassword;
    private String masterUserArn;

    /**
     * The master username stored in the domain's internal database.
     */
    @Updatable
    @ConflictsWith("master-user-arn")
    public String getMasterUsername() {
        return masterUsername;
    }

    public void setMasterUsername(String masterUsername) {
        this.masterUsername = masterUsername;
    }

    /**
     * The master password stored in the domain's internal database.
     */
    @Updatable
    @ConflictsWith("master-user-arn")
    public String getMasterPassword() {
        return masterPassword;
    }

    public void setMasterPassword(String masterPassword) {
        this.masterPassword = masterPassword;
    }

    /**
     * The master user's Amazon Resource Number. The ARN can point to an IAM user or role.
     */
    @Updatable
    @ConflictsWith({ "master-username", "master-password" })
    public String getMasterUserArn() {
        return masterUserArn;
    }

    public void setMasterUserArn(String masterUserArn) {
        this.masterUserArn = masterUserArn;
    }

    @Override
    public void copyFrom(MasterUserOptions model) {
        setMasterUsername(model.masterUserName());
        setMasterPassword(model.masterUserPassword());
        setMasterUserArn(model.masterUserARN());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    MasterUserOptions toMasterUserOptions() {
        return MasterUserOptions.builder()
            .masterUserName(getMasterUsername())
            .masterUserPassword(getMasterPassword())
            .masterUserARN(getMasterUserArn())
            .build();
    }
}
