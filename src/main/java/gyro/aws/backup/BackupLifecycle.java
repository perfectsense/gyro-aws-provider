/*
 * Copyright 2021, Brightspot.
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

package gyro.aws.backup;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.backup.model.Lifecycle;

public class BackupLifecycle extends Diffable implements Copyable<Lifecycle> {

    private Long deleteAfterDays;
    private Long moveToColdStorageAfterDays;

    /**
     * The number of days after creation that a recovery point is deleted.
     */
    @Updatable
    public Long getDeleteAfterDays() {
        return deleteAfterDays;
    }

    public void setDeleteAfterDays(Long deleteAfterDays) {
        this.deleteAfterDays = deleteAfterDays;
    }

    /**
     * The number of days after creation that a recovery point is moved to cold storage.
     */
    @Updatable
    public Long getMoveToColdStorageAfterDays() {
        return moveToColdStorageAfterDays;
    }

    public void setMoveToColdStorageAfterDays(Long moveToColdStorageAfterDays) {
        this.moveToColdStorageAfterDays = moveToColdStorageAfterDays;
    }

    @Override
    public void copyFrom(Lifecycle model) {
        setDeleteAfterDays(model.deleteAfterDays());
        setMoveToColdStorageAfterDays(model.moveToColdStorageAfterDays());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    Lifecycle toLifecycle() {
        return Lifecycle.builder().deleteAfterDays(getDeleteAfterDays())
            .moveToColdStorageAfterDays(getMoveToColdStorageAfterDays()).build();
    }
}
