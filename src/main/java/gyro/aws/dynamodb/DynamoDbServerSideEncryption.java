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

package gyro.aws.dynamodb;

import gyro.aws.Copyable;
import gyro.aws.kms.KmsKeyResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.dynamodb.model.SSEDescription;
import software.amazon.awssdk.services.dynamodb.model.SSESpecification;
import software.amazon.awssdk.services.dynamodb.model.SSEStatus;
import software.amazon.awssdk.services.dynamodb.model.SSEType;

public class DynamoDbServerSideEncryption extends Diffable implements Copyable<SSEDescription> {

    private Boolean enabled;
    private KmsKeyResource kmsKey;

    /**
     * Whether or not to enable server-side encryption using an AWS managed KMS customer master key. (Required)
     */
    @Required
    @Updatable
    public Boolean getEnabled() {
        if (enabled == null) {
            enabled = false;
        }

        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * The AWS managed KMS customer master key to encrypt the DynamoDb table. Only provide this if the key is different from the default DynamoDB customer master key ``alias/aws/dynamodb``.
     */
    @Updatable
    public KmsKeyResource getKmsKey() {
        return kmsKey;
    }

    public void setKmsKey(KmsKeyResource kmsKey) {
        this.kmsKey = kmsKey;
    }

    @Override
    public void copyFrom(SSEDescription model) {
        if (model == null) {
            setEnabled(false);
            setKmsKey(null);
            return;
        }

        setEnabled(SSEStatus.ENABLED.equals(model.status()));
        setKmsKey(findById(KmsKeyResource.class, model.kmsMasterKeyArn()));
    }

    @Override
    public String primaryKey() {
        return "";
    }

    SSESpecification toSSESpecification() {
        SSESpecification.Builder builder = SSESpecification.builder()
            .enabled(getEnabled());

        if (getKmsKey() != null && getEnabled()) {
            builder.kmsMasterKeyId(getKmsKey().getArn());
            builder.sseType(getKmsKey() != null ? SSEType.KMS : null);
        }

        return builder.build();
    }
}
