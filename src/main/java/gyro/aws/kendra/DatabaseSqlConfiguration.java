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
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.kendra.model.QueryIdentifiersEnclosingOption;
import software.amazon.awssdk.services.kendra.model.SqlConfiguration;

public class DatabaseSqlConfiguration extends Diffable implements Copyable<SqlConfiguration> {

    private QueryIdentifiersEnclosingOption queryIdentifiersEnclosingOption;

    /**
     * Determines whether SQL identifiers for tables and column names are enclosed in double quotes (") when making a database query.
     */
    @Updatable
    @Required
    @ValidStrings({ "DOUBLE_QUOTES", "NONE" })
    public QueryIdentifiersEnclosingOption getQueryIdentifiersEnclosingOption() {
        return queryIdentifiersEnclosingOption;
    }

    public void setQueryIdentifiersEnclosingOption(QueryIdentifiersEnclosingOption queryIdentifiersEnclosingOption) {
        this.queryIdentifiersEnclosingOption = queryIdentifiersEnclosingOption;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(SqlConfiguration model) {
        setQueryIdentifiersEnclosingOption(model.queryIdentifiersEnclosingOption());
    }

    public SqlConfiguration toSqlConfiguration() {
        return SqlConfiguration.builder().queryIdentifiersEnclosingOption(getQueryIdentifiersEnclosingOption()).build();
    }
}
