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
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.elasticsearch.model.AdvancedSecurityOptions;
import software.amazon.awssdk.services.elasticsearch.model.AdvancedSecurityOptionsInput;

public class ElasticsearchAdvancedSecurityOptions extends Diffable implements Copyable<AdvancedSecurityOptions> {

    private Boolean enableAdvancedSecurityOptions;
    private Boolean enableInternalUserDatabase;
    private ElasticsearchMasterUserOptions masterUserOptions;

    /**
     * Enable advanced security for the domain. (Required)
     */
    @Updatable
    @Required
    public Boolean getEnableAdvancedSecurityOptions() {
        return enableAdvancedSecurityOptions;
    }

    public void setEnableAdvancedSecurityOptions(Boolean enableAdvancedSecurityOptions) {
        this.enableAdvancedSecurityOptions = enableAdvancedSecurityOptions;
    }

    /**
     * Enable the Internal User Database.
     */
    @Updatable
    public Boolean getEnableInternalUserDatabase() {
        return enableInternalUserDatabase;
    }

    public void setEnableInternalUserDatabase(Boolean enableInternalUserDatabase) {
        this.enableInternalUserDatabase = enableInternalUserDatabase;
    }

    /**
     * The master user options configuration. Can only be set if ``enable-advanced-security-options`` is set to ``true``.
     *
     * @subresource gyro.aws.elasticsearch.ElasticsearchMasterUserOptions
     */
    @Updatable
    public ElasticsearchMasterUserOptions getMasterUserOptions() {
        return masterUserOptions;
    }

    public void setMasterUserOptions(ElasticsearchMasterUserOptions masterUserOptions) {
        this.masterUserOptions = masterUserOptions;
    }

    @Override
    public void copyFrom(AdvancedSecurityOptions model) {
        setEnableAdvancedSecurityOptions(model.enabled());
        setEnableInternalUserDatabase(model.internalUserDatabaseEnabled());

        // Not resetting the masterUserOptions since the api doesn't return the credentials for the master user.
    }

    @Override
    public String primaryKey() {
        return "";
    }

    AdvancedSecurityOptionsInput toAdvancedSecurityOptionsInput() {
        AdvancedSecurityOptionsInput.Builder builder = AdvancedSecurityOptionsInput.builder()
            .enabled(getEnableAdvancedSecurityOptions());

        if (getEnableInternalUserDatabase() != null) {
            builder.internalUserDatabaseEnabled(getEnableInternalUserDatabase());
        }

        if (getMasterUserOptions() != null) {
            builder.masterUserOptions(getMasterUserOptions().toMasterUserOptions());
        }

        return builder.build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getEnableAdvancedSecurityOptions().equals(Boolean.FALSE) && (
            configuredFields.contains("enable-internal-user-database") || configuredFields.contains(
                "master-user-options"))) {
            errors.add(new ValidationError(
                this,
                null,
                "The 'enable-internal-user-database' or 'master-user-options' can only be set if 'enable-advanced-security-options' is set to 'true'."));
        }

        if (getEnableAdvancedSecurityOptions().equals(Boolean.TRUE)
            && !configuredFields.contains("master-user-options")) {
            errors.add(new ValidationError(
                this,
                "master-user-options",
                "The 'master-user-options' is required if 'enable-advanced-security-options' is set to 'true'."));
        }

        return errors;
    }
}
