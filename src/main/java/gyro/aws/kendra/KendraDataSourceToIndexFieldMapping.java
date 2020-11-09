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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.kendra.model.DataSourceToIndexFieldMapping;

public class KendraDataSourceToIndexFieldMapping extends Diffable
    implements Copyable<DataSourceToIndexFieldMapping> {

    private String dataSourceFieldName;
    private String dateFieldFormat;
    private String indexFieldName;

    /**
     * The name of the column or attribute in the data source.
     */
    @Required
    public String getDataSourceFieldName() {
        return dataSourceFieldName;
    }

    public void setDataSourceFieldName(String dataSourceFieldName) {
        this.dataSourceFieldName = dataSourceFieldName;
    }

    /**
     * The type of data stored in the column or attribute.
     */
    public String getDateFieldFormat() {
        return dateFieldFormat;
    }

    public void setDateFieldFormat(String dateFieldFormat) {
        this.dateFieldFormat = dateFieldFormat;
    }

    /**
     * The name of the field in the index.
     */
    @Required
    public String getIndexFieldName() {
        return indexFieldName;
    }

    public void setIndexFieldName(String indexFieldName) {
        this.indexFieldName = indexFieldName;
    }

    @Override
    public String primaryKey() {
        StringBuilder sb = new StringBuilder("DataSourceToIndexFieldMapping: ");

        sb.append("Field name: ").append(getDataSourceFieldName()).append(" ")
            .append("Index field name: ").append(getIndexFieldName()).append(" ");

        if (getDateFieldFormat() != null) {
            sb.append("Format: ").append(getDateFieldFormat());
        }

        return sb.toString();
    }

    @Override
    public void copyFrom(DataSourceToIndexFieldMapping model) {
        setDataSourceFieldName(model.dataSourceFieldName());
        setDateFieldFormat(model.dateFieldFormat());
        setIndexFieldName(model.indexFieldName());
    }

    public DataSourceToIndexFieldMapping toDataSourceToIndexFieldMapping() {
        return DataSourceToIndexFieldMapping.builder()
            .dataSourceFieldName(getDataSourceFieldName())
            .dateFieldFormat(getDateFieldFormat())
            .indexFieldName(getIndexFieldName())
            .build();
    }
}
