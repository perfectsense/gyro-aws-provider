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
import gyro.aws.kms.KmsKeyResource;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.kendra.model.ServerSideEncryptionConfiguration;

public class KendraServerSideEncryptionConfiguration extends Diffable
    implements Copyable<ServerSideEncryptionConfiguration> {

    private KmsKeyResource kmsKey;

    /**
     * The AWS KMS customer master key. (Required)
     */
    @Required
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
    public void copyFrom(ServerSideEncryptionConfiguration model) {
        setKmsKey(findById(KmsKeyResource.class, model.kmsKeyId()));
    }

    public ServerSideEncryptionConfiguration toServerSideEncryptionConfiguration() {
        return ServerSideEncryptionConfiguration.builder().kmsKeyId(getKmsKey().getId()).build();
    }
}
