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
import software.amazon.awssdk.services.kendra.model.SalesforceStandardObjectAttachmentConfiguration;

public class KendraSalesforceStandardObjectAttachmentConfiguration extends Diffable
    implements Copyable<SalesforceStandardObjectAttachmentConfiguration> {

    private String documentTitleFieldName;
    private List<KendraDataSourceToIndexFieldMapping> fieldMapping;

    /**
     * The name of the field in the custom knowledge article that contains the document title.
     */
    @Required
    @Updatable
    public String getDocumentTitleFieldName() {
        return documentTitleFieldName;
    }

    public void setDocumentTitleFieldName(String documentTitleFieldName) {
        this.documentTitleFieldName = documentTitleFieldName;
    }

    /**
     * The list of objects that map fields in attachments to Amazon Kendra index fields.
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

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(SalesforceStandardObjectAttachmentConfiguration model) {
        setDocumentTitleFieldName(model.documentTitleFieldName());

        if (model.hasFieldMappings()) {
            setFieldMapping(model.fieldMappings().stream().map(f -> {
                KendraDataSourceToIndexFieldMapping mapping = newSubresource(KendraDataSourceToIndexFieldMapping.class);
                mapping.copyFrom(f);

                return mapping;

            }).collect(Collectors.toList()));
        }
    }

    public SalesforceStandardObjectAttachmentConfiguration toSalesforceStandardObjectAttachmentConfiguration() {
        return SalesforceStandardObjectAttachmentConfiguration.builder()
            .documentTitleFieldName(getDocumentTitleFieldName())
            .fieldMappings(getFieldMapping().stream()
                .map(KendraDataSourceToIndexFieldMapping::toDataSourceToIndexFieldMapping)
                .collect(Collectors.toList()))
            .build();
    }
}
