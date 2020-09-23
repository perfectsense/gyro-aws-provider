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

package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.aws.kms.KmsKeyResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;
import software.amazon.awssdk.services.s3.model.ServerSideEncryptionByDefault;

public class S3ServerSideEncryptionByDefault extends Diffable implements Copyable<ServerSideEncryptionByDefault> {

    private KmsKeyResource key;
    private ServerSideEncryption encryptionType;

    /**
     * The KMS master key to use for the default encryption.
     */
    @Updatable
    public KmsKeyResource getKey() {
        return key;
    }

    public void setKey(KmsKeyResource key) {
        this.key = key;
    }

    /**
     * The server-side encryption algorithm to use for the default encryption.
     */
    @Required
    @Updatable
    public ServerSideEncryption getEncryptionType() {
        return encryptionType;
    }

    public void setEncryptionType(ServerSideEncryption encryptionType) {
        this.encryptionType = encryptionType;
    }

    @Override
    public void copyFrom(ServerSideEncryptionByDefault model) {
        setKey(findById(KmsKeyResource.class, model.kmsMasterKeyID()));
        setEncryptionType(model.sseAlgorithm());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    ServerSideEncryptionByDefault toServerSideEncryptionByDefault() {
        ServerSideEncryptionByDefault.Builder builder = ServerSideEncryptionByDefault.builder().sseAlgorithm(getEncryptionType());

        if (getKey() != null) {
            builder = builder.kmsMasterKeyID(getKey().getArn());
        }

        return builder.build();
    }
}
