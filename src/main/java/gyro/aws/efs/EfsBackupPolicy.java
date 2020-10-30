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

package gyro.aws.efs;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.efs.model.BackupPolicy;
import software.amazon.awssdk.services.efs.model.Status;

public class EfsBackupPolicy extends Diffable implements Copyable<BackupPolicy> {

    private Status status;

    /**
     * The status of the file system's backup policy.
     */
    @Required
    @ValidStrings({"ENABLED", "DISABLED"})
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public void copyFrom(BackupPolicy model) {
        setStatus(model.status());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public BackupPolicy toBackupPolicy() {
        return BackupPolicy.builder().status(getStatus()).build();
    }
}
