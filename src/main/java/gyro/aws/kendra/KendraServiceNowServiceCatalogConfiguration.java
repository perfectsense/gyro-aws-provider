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
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.kendra.model.ServiceNowServiceCatalogConfiguration;

public class KendraServiceNowServiceCatalogConfiguration extends Diffable
    implements Copyable<ServiceNowServiceCatalogConfiguration> {

    private List<String> excludeAttachmentFilePatterns;
    private List<String> includeAttachmentFilePatterns;
    private Boolean crawlAttachments;
    private List<KendraDataSourceToIndexFieldMapping> fieldMapping;
    private String documentDataFieldName;
    private String documentTitleFieldName;

    /**
     * The list of regular expressions applied to exclude in the service catalogs.
     */
    @Updatable
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
     * The list of regular expressions applied to include in the service catalogs.
     */
    @Updatable
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
     * Indicates whether Amazon Kendra should index attachments to service catalogs.
     */
    @Updatable
    public Boolean getCrawlAttachments() {
        return crawlAttachments;
    }

    public void setCrawlAttachments(Boolean crawlAttachments) {
        this.crawlAttachments = crawlAttachments;
    }

    /**
     * The list of objects mapping between ServiceNow fields and Amazon Kendra index fields.
     *
     * @subresource gyro.aws.kendra.KendraDataSourceToIndexFieldMapping
     */
    @Updatable
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
     * The name of the ServiceNow field that is mapped to the index document contents field in the Amazon Kendra index.
     */
    @Updatable
    public String getDocumentDataFieldName() {
        return documentDataFieldName;
    }

    public void setDocumentDataFieldName(String documentDataFieldName) {
        this.documentDataFieldName = documentDataFieldName;
    }

    /**
     * The name of the ServiceNow field that is mapped to the index document title field.
     */
    @Updatable
    public String getDocumentTitleFieldName() {
        return documentTitleFieldName;
    }

    public void setDocumentTitleFieldName(String documentTitleFieldName) {
        this.documentTitleFieldName = documentTitleFieldName;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(ServiceNowServiceCatalogConfiguration model) {
        setDocumentDataFieldName(model.documentDataFieldName());
        setDocumentTitleFieldName(model.documentTitleFieldName());
        setCrawlAttachments(model.crawlAttachments());

        if (model.hasExcludeAttachmentFilePatterns()) {
            setExcludeAttachmentFilePatterns(model.excludeAttachmentFilePatterns());
        }

        if (model.hasIncludeAttachmentFilePatterns()) {
            setIncludeAttachmentFilePatterns(model.includeAttachmentFilePatterns());
        }

        if (model.hasFieldMappings()) {
            setFieldMapping(model.fieldMappings().stream().map(f -> {
                KendraDataSourceToIndexFieldMapping mapping = newSubresource(KendraDataSourceToIndexFieldMapping.class);
                mapping.copyFrom(f);

                return mapping;
            }).collect(Collectors.toList()));
        }
    }

    public ServiceNowServiceCatalogConfiguration toServiceNowServiceCatalogConfiguration() {
        ServiceNowServiceCatalogConfiguration.Builder builder = ServiceNowServiceCatalogConfiguration.builder()
            .crawlAttachments(getCrawlAttachments())
            .documentDataFieldName(getDocumentDataFieldName())
            .documentTitleFieldName(getDocumentTitleFieldName())
            .fieldMappings(getFieldMapping().stream()
                .map(KendraDataSourceToIndexFieldMapping::toDataSourceToIndexFieldMapping)
                .collect(Collectors.toList()));

        if (!getExcludeAttachmentFilePatterns().isEmpty()) {
            builder = builder.excludeAttachmentFilePatterns(getExcludeAttachmentFilePatterns());
        }

        if (getIncludeAttachmentFilePatterns() != null) {
            builder = builder.includeAttachmentFilePatterns(getIncludeAttachmentFilePatterns());
        }

        return builder.build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if ((getCrawlAttachments() == null || getCrawlAttachments().equals(Boolean.FALSE)) && (
            getExcludeAttachmentFilePatterns() != null
                || getIncludeAttachmentFilePatterns() != null || getDocumentTitleFieldName() != null
                || getDocumentDataFieldName() != null)) {
            errors.add(new ValidationError(this, null,
                "'crawl-attachments' should be set to 'TRUE' to provide "
                    + "'exclude-attachment-file-patterns', 'include-attachment-file-patterns' or "
                    + "'document-data-field-name' or 'document-title-field-name'"));
        }

        if (getExcludeAttachmentFilePatterns() == null && getIncludeAttachmentFilePatterns() == null
            && getCrawlAttachments() == null && getFieldMapping() == null && getDocumentDataFieldName() == null
            && getDocumentTitleFieldName() == null) {
            errors.add(new ValidationError(this, null,
                "At least one of 'excludeAttachmentFilePatterns', 'includeAttachmentFilePatterns', "
                    + "'crawlAttachments', 'fieldMapping', 'documentDataFieldName' "
                    + "or 'documentTitleFieldName' is required."));
        }

        return errors;

    }
}
