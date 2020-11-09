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
import gyro.core.validation.CollectionMax;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.kendra.model.SharePointConfiguration;
import software.amazon.awssdk.services.kendra.model.SharePointVersion;

public class KendraSharePointConfiguration extends Diffable implements Copyable<SharePointConfiguration> {

    private Boolean crawlAttachments;
    private String documentTitleFieldName;
    private List<String> exclusionPatterns;
    private List<String> inclusionPatterns;
    private List<KendraDataSourceToIndexFieldMapping> fieldMapping;
    private String secretArn;
    private SharePointVersion sharePointVersion;
    private List<String> urls;
    private Boolean useChangeLog;
    private KendraDataSourceVpcConfiguration vpcConfiguration;

    /**
     * Indicates whether Amazon Kendra should index attachments to SharePoint objects.
     */
    @Updatable
    public Boolean getCrawlAttachments() {
        return crawlAttachments;
    }

    public void setCrawlAttachments(Boolean crawlAttachments) {
        this.crawlAttachments = crawlAttachments;
    }

    /**
     * The Microsoft SharePoint attribute field that contains the title of the document.
     */
    public String getDocumentTitleFieldName() {
        return documentTitleFieldName;
    }

    public void setDocumentTitleFieldName(String documentTitleFieldName) {
        this.documentTitleFieldName = documentTitleFieldName;
    }

    /**
     * A list of regular expression patterns to exclude.
     */
    @Updatable
    public List<String> getExclusionPatterns() {
        if (exclusionPatterns == null) {
            exclusionPatterns = new ArrayList<>();
        }

        return exclusionPatterns;
    }

    public void setExclusionPatterns(List<String> exclusionPatterns) {
        this.exclusionPatterns = exclusionPatterns;
    }

    /**
     * A list of regular expression patterns to include.
     */
    @Updatable
    public List<String> getInclusionPatterns() {
        if (inclusionPatterns == null) {
            inclusionPatterns = new ArrayList<>();
        }

        return inclusionPatterns;
    }

    public void setInclusionPatterns(List<String> inclusionPatterns) {
        this.inclusionPatterns = inclusionPatterns;
    }

    /**
     * A list of DataSourceToIndexFieldMapping objects that map Microsoft SharePoint attributes to custom fields in the Amazon Kendra index.
     */
    public List<KendraDataSourceToIndexFieldMapping> getFieldMapping() {
        if (fieldMapping == null) {
            fieldMapping = new ArrayList<>();
        }

        return fieldMapping;
    }

    public void setFieldMapping(List<KendraDataSourceToIndexFieldMapping> fieldMapping) {
        this.fieldMapping = fieldMapping;
    }

    /**
     * The Amazon Resource Name (ARN) of credentials stored in AWS Secrets Manager.
     */
    @Updatable
    public String getSecretArn() {
        return secretArn;
    }

    public void setSecretArn(String secretArn) {
        this.secretArn = secretArn;
    }

    /**
     * The version of Microsoft SharePoint that you are using as a data source. Valid value is ``SHAREPOINT_ONLINE``.
     */
    @ValidStrings("SHAREPOINT_ONLINE")
    public SharePointVersion getSharePointVersion() {
        return sharePointVersion;
    }

    public void setSharePointVersion(SharePointVersion sharePointVersion) {
        this.sharePointVersion = sharePointVersion;
    }

    /**
     * The URLs of the Microsoft SharePoint site that contains the documents that should be indexed. Max limit of ``99``.
     */
    @Updatable
    @CollectionMax(99)
    public List<String> getUrls() {
        if (urls == null) {
            urls = new ArrayList<>();
        }

        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    /**
     * Indicates whether to use the Microsoft SharePoint change log to determine the documents that need to be updated in the index.
     */
    public Boolean getUseChangeLog() {
        return useChangeLog;
    }

    public void setUseChangeLog(Boolean useChangeLog) {
        this.useChangeLog = useChangeLog;
    }

    /**
     * The value of the VpcConfiguration property for this object.
     */
    public KendraDataSourceVpcConfiguration getVpcConfiguration() {
        return vpcConfiguration;
    }

    public void setVpcConfiguration(KendraDataSourceVpcConfiguration vpcConfiguration) {
        this.vpcConfiguration = vpcConfiguration;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(SharePointConfiguration model) {
        setCrawlAttachments(model.crawlAttachments());
        setDocumentTitleFieldName(model.documentTitleFieldName());
        setExclusionPatterns(model.exclusionPatterns());
        setInclusionPatterns(model.inclusionPatterns());
        setSecretArn(model.secretArn());
        setSharePointVersion(model.sharePointVersion());
        setUrls(model.urls());
        setUseChangeLog(model.useChangeLog());

        if (model.hasFieldMappings()) {
            setFieldMapping(model.fieldMappings().stream().map(f -> {
                KendraDataSourceToIndexFieldMapping mapping = newSubresource(KendraDataSourceToIndexFieldMapping.class);
                mapping.copyFrom(f);

                return mapping;

            }).collect(Collectors.toList()));
        }

        if (model.vpcConfiguration() != null) {
            KendraDataSourceVpcConfiguration vpcConfig = newSubresource(KendraDataSourceVpcConfiguration.class);
            vpcConfig.copyFrom(model.vpcConfiguration());
            setVpcConfiguration(vpcConfig);
        }
    }

    public SharePointConfiguration toSharePointConfiguration() {
        SharePointConfiguration.Builder builder = SharePointConfiguration.builder()
            .crawlAttachments(getCrawlAttachments())
            .documentTitleFieldName(getDocumentTitleFieldName())
            .exclusionPatterns(getExclusionPatterns())
            .inclusionPatterns(getInclusionPatterns())
            .secretArn(getSecretArn())
            .urls(getUrls())
            .sharePointVersion(getSharePointVersion())
            .useChangeLog(getUseChangeLog())
            .fieldMappings(getFieldMapping().stream()
                .map(KendraDataSourceToIndexFieldMapping::toDataSourceToIndexFieldMapping)
                .collect(Collectors.toList()));

        if (getVpcConfiguration() != null) {
            builder = builder.vpcConfiguration(getVpcConfiguration().toDataSourceVpcConfiguration());
        }

        return builder.build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if ((getCrawlAttachments() == null || getCrawlAttachments().equals(Boolean.FALSE))
            && getDocumentTitleFieldName() != null) {
            errors.add(new ValidationError(this, null,
                "'crawl-attachments' should be set to 'TRUE' to provide 'document-title-field-name'"));
        }

        return errors;
    }
}
