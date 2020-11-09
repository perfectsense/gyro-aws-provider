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
import gyro.core.validation.DependsOn;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.kendra.model.SalesforceConfiguration;

public class KendraSalesforceConfiguration extends Diffable implements Copyable<SalesforceConfiguration> {

    private KendraSalesforceChatterFeedConfiguration chatterFeedConfiguration;
    private Boolean crawlAttachments;
    private List<String> excludeAttachmentFilePatterns;
    private List<String> includeAttachmentFilePatterns;
    private KendraSalesforceKnowledgeArticleConfiguration knowledgeArticleConfiguration;
    private String secretArn;
    private String serverUrl;
    private KendraSalesforceStandardObjectAttachmentConfiguration objectAttachmentConfiguration;
    private List<KendraSalesforceStandardObjectConfiguration> objectConfiguration;

    /**
     * The configuration information for Salesforce chatter feeds.
     */
    @Updatable
    public KendraSalesforceChatterFeedConfiguration getChatterFeedConfiguration() {
        return chatterFeedConfiguration;
    }

    public void setChatterFeedConfiguration(KendraSalesforceChatterFeedConfiguration chatterFeedConfiguration) {
        this.chatterFeedConfiguration = chatterFeedConfiguration;
    }

    /**
     * Indicates whether Amazon Kendra should index attachments to Salesforce objects.
     */
    @Updatable
    public Boolean getCrawlAttachments() {
        return crawlAttachments;
    }

    public void setCrawlAttachments(Boolean crawlAttachments) {
        this.crawlAttachments = crawlAttachments;
    }

    /**
     * The list of regular expression patterns to exclude.
     */
    @DependsOn("crawl-attachments")
    public List<String> getExcludeAttachmentFilePatterns() {
        if (excludeAttachmentFilePatterns == null) {
            excludeAttachmentFilePatterns = new ArrayList<>();
        }

        return excludeAttachmentFilePatterns;
    }

    public void setExcludeAttachmentFilePatterns(List<String> excludeAttachmentFilePatterns) {
        this.excludeAttachmentFilePatterns = excludeAttachmentFilePatterns;
    }

    /**
     * The list of regular expression patterns to include.
     */
    @DependsOn("crawl-attachments")
    public List<String> getIncludeAttachmentFilePatterns() {
        if (includeAttachmentFilePatterns == null) {
            includeAttachmentFilePatterns = new ArrayList<>();
        }
        return includeAttachmentFilePatterns;
    }

    public void setIncludeAttachmentFilePatterns(List<String> includeAttachmentFilePatterns) {
        this.includeAttachmentFilePatterns = includeAttachmentFilePatterns;
    }

    /**
     * The configuration information for the knowlege article types that Amazon Kendra indexes.
     */
    @Updatable
    public KendraSalesforceKnowledgeArticleConfiguration getKnowledgeArticleConfiguration() {
        return knowledgeArticleConfiguration;
    }

    public void setKnowledgeArticleConfiguration(KendraSalesforceKnowledgeArticleConfiguration knowledgeArticleConfiguration) {
        this.knowledgeArticleConfiguration = knowledgeArticleConfiguration;
    }

    /**
     * The Amazon Resource Name (ARN) of an AWS Secrets Manager secret that contains the key/value pairs required to connect to the Salesforce instance.
     */
    @Updatable
    @Required
    public String getSecretArn() {
        return secretArn;
    }

    public void setSecretArn(String secretArn) {
        this.secretArn = secretArn;
    }

    /**
     * The instance URL for the Salesforce site that you want to index.
     */
    @Updatable
    @Required
    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    /**
     * The configuration information for processing attachments to Salesforce standard objects.
     */
    @Updatable
    @DependsOn("crawl-attachments")
    public KendraSalesforceStandardObjectAttachmentConfiguration getObjectAttachmentConfiguration() {
        return objectAttachmentConfiguration;
    }

    public void setObjectAttachmentConfiguration(KendraSalesforceStandardObjectAttachmentConfiguration objectAttachmentConfiguration) {
        this.objectAttachmentConfiguration = objectAttachmentConfiguration;
    }

    /**
     * The list of Salesforce standard objects that Amazon Kendra indexes.
     */
    @Updatable
    public List<KendraSalesforceStandardObjectConfiguration> getObjectConfiguration() {
        if (objectConfiguration != null) {
            objectConfiguration = new ArrayList<>();
        }

        return objectConfiguration;
    }

    public void setObjectConfiguration(List<KendraSalesforceStandardObjectConfiguration> objectConfiguration) {
        this.objectConfiguration = objectConfiguration;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(SalesforceConfiguration model) {
        setSecretArn(model.secretArn());
        setServerUrl(model.serverUrl());
        setCrawlAttachments(model.crawlAttachments());

        if (model.chatterFeedConfiguration() != null) {
            KendraSalesforceChatterFeedConfiguration config = newSubresource(
                KendraSalesforceChatterFeedConfiguration.class);
            config.copyFrom(model.chatterFeedConfiguration());
            setChatterFeedConfiguration(config);
        }

        if (model.knowledgeArticleConfiguration() != null) {
            KendraSalesforceKnowledgeArticleConfiguration config = newSubresource(
                KendraSalesforceKnowledgeArticleConfiguration.class);
            config.copyFrom(model.knowledgeArticleConfiguration());
            setKnowledgeArticleConfiguration(config);
        }

        if (model.hasStandardObjectConfigurations()) {
            setObjectConfiguration(model.standardObjectConfigurations().stream().map(c -> {
                KendraSalesforceStandardObjectConfiguration config = newSubresource(
                    KendraSalesforceStandardObjectConfiguration.class);
                config.copyFrom(c);

                return config;
            }).collect(Collectors.toList()));
        }

        if (model.standardObjectAttachmentConfiguration() != null) {
            KendraSalesforceStandardObjectAttachmentConfiguration config = newSubresource(
                KendraSalesforceStandardObjectAttachmentConfiguration.class);
            config.copyFrom(model.standardObjectAttachmentConfiguration());
            setObjectAttachmentConfiguration(config);
        }

        if (model.hasExcludeAttachmentFilePatterns()) {
            setExcludeAttachmentFilePatterns(model.excludeAttachmentFilePatterns());
        }

        if (model.hasIncludeAttachmentFilePatterns()) {
            setIncludeAttachmentFilePatterns(model.includeAttachmentFilePatterns());
        }
    }

    public SalesforceConfiguration toSalesforceConfiguration() {
        SalesforceConfiguration.Builder builder = SalesforceConfiguration.builder()
            .serverUrl(getServerUrl()).secretArn(getSecretArn()).crawlAttachments(getCrawlAttachments());

        if (getChatterFeedConfiguration() != null) {
            builder = builder.chatterFeedConfiguration(getChatterFeedConfiguration()
                .toSalesforceChatterFeedConfiguration());
        }

        if (!getExcludeAttachmentFilePatterns().isEmpty()) {
            builder = builder.excludeAttachmentFilePatterns(getExcludeAttachmentFilePatterns());
        }

        if (getIncludeAttachmentFilePatterns() != null) {
            builder = builder.includeAttachmentFilePatterns(getIncludeAttachmentFilePatterns());
        }

        if (getKnowledgeArticleConfiguration() != null) {
            builder = builder.knowledgeArticleConfiguration(getKnowledgeArticleConfiguration()
                .toSalesforceKnowledgeArticleConfiguration());
        }

        if (getObjectAttachmentConfiguration() != null) {
            builder = builder.standardObjectAttachmentConfiguration(getObjectAttachmentConfiguration()
                .toSalesforceStandardObjectAttachmentConfiguration());
        }

        if (!getObjectConfiguration().isEmpty()) {
            builder = builder.standardObjectConfigurations(getObjectConfiguration().stream()
                .map(KendraSalesforceStandardObjectConfiguration::toSalesforceStandardObjectConfiguration)
                .collect(Collectors.toList()));
        }

        return builder.build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if ((getCrawlAttachments() == null || getCrawlAttachments().equals(Boolean.FALSE)) && (
            getObjectAttachmentConfiguration() != null || getExcludeAttachmentFilePatterns() != null
                || getIncludeAttachmentFilePatterns() != null)) {
            errors.add(new ValidationError(this, null,
                "'crawl-attachments' should be set to 'TRUE' to provide 'object-attachment-configuration', "
                    + "'exclude-attachment-file-patterns' or 'include-attachment-file-patterns'"));
        }

        if (getChatterFeedConfiguration() == null && getKnowledgeArticleConfiguration() == null
            && getObjectConfiguration() == null) {
            errors.add(new ValidationError(this, null,
                "At least one of 'chatter-feed-configuration', 'knowledge-article-configuration' "
                    + "or 'object-configuration' is required."));
        }

        return errors;
    }
}
