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

package gyro.aws.elasticsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.aws.Copyable;
import gyro.aws.kms.KmsKeyResource;
import gyro.core.resource.Diffable;
import gyro.core.validation.DependsOn;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.elasticsearch.model.EncryptionAtRestOptions;

public class ElasticsearchEncryptionAtRestOptions extends Diffable implements Copyable<EncryptionAtRestOptions> {

    private Boolean enableEncryptionAtRest;
    private KmsKeyResource kmsKeyResource;

    /**
     * Enable encryption at rest to prevent unauthorized to the data. Defaults to ``false``. (Required)
     */
    @Required
    public Boolean getEnableEncryptionAtRest() {
        return enableEncryptionAtRest;
    }

    public void setEnableEncryptionAtRest(Boolean enableEncryptionAtRest) {
        this.enableEncryptionAtRest = enableEncryptionAtRest;
    }

    /**
     * The KMS key resource for encryption options.
     */
    @DependsOn("enable-encryption-at-rest")
    public KmsKeyResource getKmsKeyResource() {
        return kmsKeyResource;
    }

    public void setKmsKeyResource(KmsKeyResource kmsKeyResource) {
        this.kmsKeyResource = kmsKeyResource;
    }

    @Override
    public void copyFrom(EncryptionAtRestOptions model) {
        setEnableEncryptionAtRest(model.enabled());
        setKmsKeyResource(findById(KmsKeyResource.class, model.kmsKeyId()));
    }

    @Override
    public String primaryKey() {
        return "";
    }

    EncryptionAtRestOptions toEncryptionAtRestOptions() {
        EncryptionAtRestOptions.Builder builder = EncryptionAtRestOptions.builder()
            .enabled(getEnableEncryptionAtRest());

        if (getKmsKeyResource() != null) {
            builder.kmsKeyId(getKmsKeyResource().getId());
        }

        return builder.build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (configuredFields.contains("enable-encryption-at-rest") && getEnableEncryptionAtRest().equals(Boolean.FALSE)
            && configuredFields.contains("kms-key-resource")) {
            errors.add(new ValidationError(
                this,
                null,
                "The 'kms-key-resource' can only be set if 'enable-encryption-at-rest' is set to 'true'."));
        }

        return errors;
    }
}
