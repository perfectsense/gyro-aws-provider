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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.aws.Copyable;
import gyro.aws.kms.KmsKeyResource;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
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
    public KmsKeyResource getKmsKey() {
        return kmsKey;
    }

    public void setKmsKey(KmsKeyResource kmsKey) {
        this.kmsKey = kmsKey;
    }

    @Override
    public void copyFrom(SSEDescription model) {
        setEnabled(SSEStatus.ENABLED.equals(model.status()));
        setKmsKey(findById(KmsKeyResource.class, model.kmsMasterKeyArn()));
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (!getEnabled()) {
            errors.add(new ValidationError(
                this,
                "enabled",
                "Do not set 'enabled' to 'false' as this is the default value!"));
        }

        return errors;
    }

    SSESpecification toSSESpecification() {
        return SSESpecification.builder()
            .enabled(getEnabled())
            .kmsMasterKeyId(getKmsKey() != null ? getKmsKey().getArn() : null)
            .sseType(getKmsKey() != null ? SSEType.KMS : null)
            .build();
    }
}
