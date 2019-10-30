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

package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.s3.model.SourceSelectionCriteria;
import software.amazon.awssdk.services.s3.model.SseKmsEncryptedObjectsStatus;

public class S3SourceSelectionCriteria extends Diffable implements Copyable<SourceSelectionCriteria> {
    private SseKmsEncryptedObjectsStatus sseKmsEncryptedObjectsStatus;

    /**
     * Status of Server Side Encryption. Valid values are ``ENABLED`` or ``DISABLED``
     */
    @Updatable
    public SseKmsEncryptedObjectsStatus getSseKmsEncryptedObjectsStatus() {
        if(sseKmsEncryptedObjectsStatus == null){
            this.sseKmsEncryptedObjectsStatus = SseKmsEncryptedObjectsStatus.ENABLED;
        }
        return sseKmsEncryptedObjectsStatus;
    }

    public void setSseKmsEncryptedObjectsStatus(SseKmsEncryptedObjectsStatus sseKmsEncryptedObjectsStatus) {
        this.sseKmsEncryptedObjectsStatus = sseKmsEncryptedObjectsStatus;
    }

    @Override
    public String primaryKey() {
        return "source selection criteria";
    }

    @Override
    public void copyFrom(SourceSelectionCriteria sourceSelectionCriteria) {
        setSseKmsEncryptedObjectsStatus(sourceSelectionCriteria.sseKmsEncryptedObjects().status());
    }

    SourceSelectionCriteria toSourceSelectionCriteria() {
        return SourceSelectionCriteria.builder()
                .sseKmsEncryptedObjects(
                        k -> k.status(getSseKmsEncryptedObjectsStatus())
                ).build();
    }
}
