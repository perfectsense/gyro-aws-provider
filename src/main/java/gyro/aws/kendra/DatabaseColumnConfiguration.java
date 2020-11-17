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
import gyro.core.validation.CollectionMax;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.kendra.model.ColumnConfiguration;

public class DatabaseColumnConfiguration extends Diffable implements Copyable<ColumnConfiguration> {

    private String documentDataColumnName;
    private String documentIdColumnName;
    private String documentTitleColumnName;
    private List<String> changeDetectingColumns;
    private List<KendraDataSourceToIndexFieldMapping> fieldMapping;

    /**
     * The column that contains the contents of the document.
     */
    @Required
    public String getDocumentDataColumnName() {
        return documentDataColumnName;
    }

    public void setDocumentDataColumnName(String documentDataColumnName) {
        this.documentDataColumnName = documentDataColumnName;
    }

    /**
     * The column that provides the document's unique identifier.
     */
    @Required
    public String getDocumentIdColumnName() {
        return documentIdColumnName;
    }

    public void setDocumentIdColumnName(String documentIdColumnName) {
        this.documentIdColumnName = documentIdColumnName;
    }

    /**
     * The column that contains the title of the document.
     */
    public String getDocumentTitleColumnName() {
        return documentTitleColumnName;
    }

    public void setDocumentTitleColumnName(String documentTitleColumnName) {
        this.documentTitleColumnName = documentTitleColumnName;
    }

    /**
     * The columns that indicate when a document in the database has changed.
     */
    @Updatable
    @Required
    @CollectionMax(5)
    public List<String> getChangeDetectingColumns() {
        if (changeDetectingColumns == null) {
            changeDetectingColumns = new ArrayList<>();
        }

        return changeDetectingColumns;
    }

    public void setChangeDetectingColumns(List<String> changeDetectingColumns) {
        this.changeDetectingColumns = changeDetectingColumns;
    }

    /**
     * The list of objects that map database column names to the corresponding fields in an index.
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
    public void copyFrom(ColumnConfiguration model) {
        setChangeDetectingColumns(model.changeDetectingColumns());
        setDocumentDataColumnName(model.documentDataColumnName());
        setDocumentIdColumnName(model.documentIdColumnName());
        setDocumentTitleColumnName(model.documentTitleColumnName());

        if (model.hasFieldMappings()) {
            setFieldMapping(model.fieldMappings().stream().map(f -> {
                KendraDataSourceToIndexFieldMapping mapping = newSubresource(KendraDataSourceToIndexFieldMapping.class);
                mapping.copyFrom(f);

                return mapping;
            }).collect(Collectors.toList()));
        }
    }

    public ColumnConfiguration toColumnConfiguration() {
        return ColumnConfiguration.builder()
            .documentDataColumnName(getDocumentDataColumnName())
            .documentIdColumnName(getDocumentIdColumnName())
            .changeDetectingColumns(getChangeDetectingColumns())
            .documentTitleColumnName(getDocumentTitleColumnName())
            .fieldMappings(getFieldMapping().stream()
                .map(KendraDataSourceToIndexFieldMapping::toDataSourceToIndexFieldMapping)
                .collect(Collectors.toList()))
            .build();
    }
}
