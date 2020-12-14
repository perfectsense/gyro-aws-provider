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
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.kendra.model.SalesforceChatterFeedConfiguration;

public class KendraSalesforceChatterFeedConfiguration extends Diffable
    implements Copyable<SalesforceChatterFeedConfiguration> {

    private String documentDataFieldName;
    private String documentTitleFieldName;
    private List<KendraDataSourceToIndexFieldMapping> fieldMapping;
    private List<String> includeFilterTypes;

    /**
     * The name of the column in the Salesforce FeedItem table that contains the content to index.
     */
    @Updatable
    @Required
    public String getDocumentDataFieldName() {
        return documentDataFieldName;
    }

    public void setDocumentDataFieldName(String documentDataFieldName) {
        this.documentDataFieldName = documentDataFieldName;
    }

    /**
     * The name of the column in the Salesforce FeedItem table that contains the title of the document.
     */
    @Updatable
    public String getDocumentTitleFieldName() {
        return documentTitleFieldName;
    }

    public void setDocumentTitleFieldName(String documentTitleFieldName) {
        this.documentTitleFieldName = documentTitleFieldName;
    }

    /**
     * The mapping from Salesforce chatter feed fields into Amazon Kendra index fields.
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
     * The filters for the documents in the feed based on status of the user.
     */
    @Updatable
    public List<String> getIncludeFilterTypes() {
        if (includeFilterTypes == null) {
            includeFilterTypes = new ArrayList<String>();
        }

        return includeFilterTypes;
    }

    public void setIncludeFilterTypes(List<String> includeFilterTypes) {
        this.includeFilterTypes = includeFilterTypes;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(SalesforceChatterFeedConfiguration model) {
        setDocumentDataFieldName(model.documentDataFieldName());
        setDocumentTitleFieldName(model.documentTitleFieldName());
        setIncludeFilterTypes(model.includeFilterTypesAsStrings());

        if (model.hasFieldMappings()) {
            setFieldMapping(model.fieldMappings().stream().map(f -> {
                KendraDataSourceToIndexFieldMapping mapping = newSubresource(KendraDataSourceToIndexFieldMapping.class);
                mapping.copyFrom(f);

                return mapping;

            }).collect(Collectors.toList()));
        }
    }

    public SalesforceChatterFeedConfiguration toSalesforceChatterFeedConfiguration() {
        return SalesforceChatterFeedConfiguration.builder()
            .documentDataFieldName(getDocumentDataFieldName())
            .documentTitleFieldName(getDocumentTitleFieldName())
            .includeFilterTypesWithStrings(getIncludeFilterTypes())
            .fieldMappings(getFieldMapping().stream()
                .map(KendraDataSourceToIndexFieldMapping::toDataSourceToIndexFieldMapping)
                .collect(Collectors.toList()))
            .build();
    }
}
