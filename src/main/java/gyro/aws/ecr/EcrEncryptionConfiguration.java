/*
 * Copyright 2021, Brightspot, Inc.
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

package gyro.aws.ecr;

import gyro.aws.Copyable;
import gyro.aws.kms.KmsKeyResource;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.ecr.model.EncryptionConfiguration;
import software.amazon.awssdk.services.ecr.model.EncryptionType;

public class EcrEncryptionConfiguration extends Diffable implements Copyable<EncryptionConfiguration> {

    private EncryptionType encryptionType;
    private KmsKeyResource kmsKey;

    /**
     * The encryption type to use.
     */
    @Required
    @ValidStrings({ "AES256", "KMS" })
    public EncryptionType getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(EncryptionType encryptionType) {
        this.encryptionType = encryptionType;
    }

    /**
     * The KMS key to use for encryption.
     */
    public KmsKeyResource getKmsKey() {
        return kmsKey;
    }

    public void setKmsKey(KmsKeyResource kmsKey) {
        this.kmsKey = kmsKey;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(EncryptionConfiguration model) {
        setEncryptionType(model.encryptionType());
        setKmsKey(findById(KmsKeyResource.class, model.kmsKey()));
    }

    EncryptionConfiguration toEncryptionConfiguration() {
        return EncryptionConfiguration.builder().encryptionType(getEncryptionType())
            .kmsKey(getKmsKey() != null ? getKmsKey().getArn() : null).build();
    }
}
