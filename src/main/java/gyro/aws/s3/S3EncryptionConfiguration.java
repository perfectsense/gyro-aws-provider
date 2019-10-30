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
import gyro.aws.kms.KmsKeyResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.s3.model.EncryptionConfiguration;

public class S3EncryptionConfiguration extends Diffable implements Copyable<EncryptionConfiguration> {
    private KmsKeyResource kmsKey;

    /**
     * KMS Key ID or ALIAS ARN to encrypt objects with when replicated to destination. Required if SSE is enabled.
     */
    @Updatable
    public KmsKeyResource getKmsKey() {
        return kmsKey;
    }

    public void setKmsKey(KmsKeyResource kmsKey) {
        this.kmsKey = kmsKey;
    }

    @Override
    public String primaryKey() {
        return "encryption configuration";
    }

    @Override
    public void copyFrom(EncryptionConfiguration encryptionConfiguration) {
        setKmsKey(findById(KmsKeyResource.class, encryptionConfiguration.replicaKmsKeyID()));
    }

    EncryptionConfiguration toEncryptionConfiguration() {
        return EncryptionConfiguration.builder()
                .replicaKmsKeyID(getKmsKey().getArn())
                .build();
    }
}
