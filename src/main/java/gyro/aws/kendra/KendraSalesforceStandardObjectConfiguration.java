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
import software.amazon.awssdk.services.kendra.model.SalesforceStandardObjectConfiguration;
import software.amazon.awssdk.services.kendra.model.SalesforceStandardObjectName;

public class KendraSalesforceStandardObjectConfiguration extends Diffable
    implements Copyable<SalesforceStandardObjectConfiguration> {

    private String documentDataFieldName;
    private String documentTitleFieldName;
    private List<KendraDataSourceToIndexFieldMapping> fieldMapping;
    private SalesforceStandardObjectName name;

    /**
     * The name of the field in the standard object configuration that contains the document data to index. (Required)
     */
    @Required
    public String getDocumentDataFieldName() {
        return documentDataFieldName;
    }

    public void setDocumentDataFieldName(String documentDataFieldName) {
        this.documentDataFieldName = documentDataFieldName;
    }

    /**
     * The name of the field in the standard object configuration that contains the document title.
     */
    public String getDocumentTitleFieldName() {
        return documentTitleFieldName;
    }

    public void setDocumentTitleFieldName(String documentTitleFieldName) {
        this.documentTitleFieldName = documentTitleFieldName;
    }

    /**
     * The list of objects that map fields in the standard object configuration to fields in the Amazon Kendra index.
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
     * The name of the configuration. (Required)
     */
    @Required
    public SalesforceStandardObjectName getName() {
        return name;
    }

    public void setName(SalesforceStandardObjectName name) {
        this.name = name;
    }

    @Override
    public String primaryKey() {
        return "Name: " + getName();
    }

    @Override
    public void copyFrom(SalesforceStandardObjectConfiguration model) {
        setDocumentDataFieldName(model.documentDataFieldName());
        setDocumentTitleFieldName(model.documentTitleFieldName());
        setName(model.name());

        if (model.hasFieldMappings()) {
            setFieldMapping(model.fieldMappings().stream().map(f -> {
                KendraDataSourceToIndexFieldMapping mapping = newSubresource(KendraDataSourceToIndexFieldMapping.class);
                mapping.copyFrom(f);

                return mapping;
            }).collect(Collectors.toList()));
        }
    }

    public SalesforceStandardObjectConfiguration toSalesforceStandardObjectConfiguration() {
        return SalesforceStandardObjectConfiguration.builder()
            .documentDataFieldName(getDocumentDataFieldName())
            .documentTitleFieldName(getDocumentTitleFieldName())
            .name(getName())
            .fieldMappings(getFieldMapping().stream()
                .map(KendraDataSourceToIndexFieldMapping::toDataSourceToIndexFieldMapping)
                .collect(Collectors.toList()))
            .build();
    }
}
