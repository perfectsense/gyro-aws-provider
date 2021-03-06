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
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.kendra.model.SalesforceStandardKnowledgeArticleTypeConfiguration;

public class KendraSalesforceStandardKnowledgeArticleTypeConfiguration extends Diffable
    implements Copyable<SalesforceStandardKnowledgeArticleTypeConfiguration> {

    private String documentDataFieldName;
    private String documentTitleFieldName;
    private List<KendraDataSourceToIndexFieldMapping> fieldMapping;

    /**
     * The name of the field in the standard knowledge article that contains the document data to index.
     */
    @Required
    public String getDocumentDataFieldName() {
        return documentDataFieldName;
    }

    public void setDocumentDataFieldName(String documentDataFieldName) {
        this.documentDataFieldName = documentDataFieldName;
    }

    /**
     * The name of the field in the standard knowledge article that contains the document title.
     */
    public String getDocumentTitleFieldName() {
        return documentTitleFieldName;
    }

    public void setDocumentTitleFieldName(String documentTitleFieldName) {
        this.documentTitleFieldName = documentTitleFieldName;
    }

    /**
     * The list of objects that map fields in the standard knowledge article to fields in the Amazon Kendra index.
     *
     * @subresource gyro.aws.kendra.KendraDataSourceToIndexFieldMapping
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

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(SalesforceStandardKnowledgeArticleTypeConfiguration model) {
        setDocumentDataFieldName(model.documentDataFieldName());
        setDocumentTitleFieldName(model.documentTitleFieldName());

        if (model.hasFieldMappings()) {
            setFieldMapping(model.fieldMappings().stream().map(f -> {
                KendraDataSourceToIndexFieldMapping mapping = newSubresource(KendraDataSourceToIndexFieldMapping.class);
                mapping.copyFrom(f);

                return mapping;

            }).collect(Collectors.toList()));
        }
    }

    public SalesforceStandardKnowledgeArticleTypeConfiguration toSalesforceStandardKnowledgeArticleTypeConfiguration() {
        return SalesforceStandardKnowledgeArticleTypeConfiguration.builder()
            .documentDataFieldName(getDocumentDataFieldName())
            .documentTitleFieldName(getDocumentTitleFieldName())
            .fieldMappings(getFieldMapping().stream()
                .map(KendraDataSourceToIndexFieldMapping::toDataSourceToIndexFieldMapping)
                .collect(Collectors.toList()))
            .build();
    }
}
