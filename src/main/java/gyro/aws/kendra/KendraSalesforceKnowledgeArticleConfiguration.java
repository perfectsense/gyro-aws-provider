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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.kendra.model.SalesforceKnowledgeArticleConfiguration;

public class KendraSalesforceKnowledgeArticleConfiguration extends Diffable
    implements Copyable<SalesforceKnowledgeArticleConfiguration> {

    private List<KendraSalesforceCustomKnowledgeArticleTypeConfiguration> customTypeConfiguration;
    private KendraSalesforceStandardKnowledgeArticleTypeConfiguration standardTypeConfiguration;
    private List<String> states;

    /**
     * The list of configurations for custom Salesforce knowledge articles.
     */
    @Updatable
    @ConflictsWith("standard-type-configuration")
    public List<KendraSalesforceCustomKnowledgeArticleTypeConfiguration> getCustomTypeConfiguration() {
        if (customTypeConfiguration == null) {
            customTypeConfiguration = new ArrayList<>();
        }

        return customTypeConfiguration;
    }

    public void setCustomTypeConfiguration(List<KendraSalesforceCustomKnowledgeArticleTypeConfiguration> customTypeConfiguration) {
        this.customTypeConfiguration = customTypeConfiguration;
    }

    /**
     * The configuration for standard Salesforce knowledge articles.
     */
    @Updatable
    @ConflictsWith("custom-type-configuration")
    public KendraSalesforceStandardKnowledgeArticleTypeConfiguration getStandardTypeConfiguration() {
        return standardTypeConfiguration;
    }

    public void setStandardTypeConfiguration(KendraSalesforceStandardKnowledgeArticleTypeConfiguration standardTypeConfiguration) {
        this.standardTypeConfiguration = standardTypeConfiguration;
    }

    /**
     * The document states that should be included when Amazon Kendra indexes knowledge articles.
     */
    @Updatable
    @Required
    public List<String> getStates() {
        return states;
    }

    public void setStates(List<String> states) {
        this.states = states;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(SalesforceKnowledgeArticleConfiguration model) {
        setStates(model.includedStatesAsStrings());

        if (model.hasCustomKnowledgeArticleTypeConfigurations()) {
            setCustomTypeConfiguration(model.customKnowledgeArticleTypeConfigurations().stream().map(c -> {
                KendraSalesforceCustomKnowledgeArticleTypeConfiguration config = newSubresource(
                    KendraSalesforceCustomKnowledgeArticleTypeConfiguration.class);
                config.copyFrom(c);

                return config;
            }).collect(Collectors.toList()));

        } else {
            KendraSalesforceStandardKnowledgeArticleTypeConfiguration config = newSubresource(
                KendraSalesforceStandardKnowledgeArticleTypeConfiguration.class);
            config.copyFrom(model.standardKnowledgeArticleTypeConfiguration());
            setStandardTypeConfiguration(config);
        }
    }

    public SalesforceKnowledgeArticleConfiguration toSalesforceKnowledgeArticleConfiguration() {
        SalesforceKnowledgeArticleConfiguration.Builder builder = SalesforceKnowledgeArticleConfiguration.builder()
            .includedStatesWithStrings(getStates());

        if (getCustomTypeConfiguration().isEmpty()) {
            builder = builder.standardKnowledgeArticleTypeConfiguration(getStandardTypeConfiguration()
                .toSalesforceStandardKnowledgeArticleTypeConfiguration());

        } else {
            builder = builder.customKnowledgeArticleTypeConfigurations(getCustomTypeConfiguration().stream()
                .map(KendraSalesforceCustomKnowledgeArticleTypeConfiguration::
                    toSalesforceCustomKnowledgeArticleTypeConfiguration)
                .collect(Collectors.toList()));
        }

        return builder.build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (!configuredFields.contains("standard-type-configuration") && !configuredFields.contains(
            "custom-type-configuration")) {
            errors.add(new ValidationError(
                this,
                null,
                "Either 'custom-type-configuration' or 'standard-type-configuration' is required."));
        }

        return errors;
    }
}
