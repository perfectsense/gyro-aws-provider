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
import software.amazon.awssdk.services.kendra.model.DatabaseConfiguration;
import software.amazon.awssdk.services.kendra.model.DatabaseEngineType;

public class KendraDatabaseConfiguration extends Diffable implements Copyable<DatabaseConfiguration> {

    private DatabaseEngineType engineType;
    private DatabaseAclConfiguration aclConfiguration;
    private DatabaseColumnConfiguration columnConfiguration;
    private DatabaseConnectionConfiguration connectionConfiguration;
    private DatabaseSqlConfiguration sqlConfiguration;
    private KendraDataSourceVpcConfiguration vpcConfiguration;

    /**
     * The type of database engine that runs the database. Valid values are ``RDS_AURORA_MYSQL``, ``RDS_AURORA_POSTGRESQL``, ``RDS_MYSQL`` or ``RDS_POSTGRESQL``. (Required)
     */
    @Updatable
    @Required
    public DatabaseEngineType getEngineType() {
        return engineType;
    }

    public void setEngineType(DatabaseEngineType engineType) {
        this.engineType = engineType;
    }

    /**
     * The information about the database column that provides information for user context filtering.
     */
    @Updatable
    public DatabaseAclConfiguration getAclConfiguration() {
        return aclConfiguration;
    }

    public void setAclConfiguration(DatabaseAclConfiguration aclConfiguration) {
        this.aclConfiguration = aclConfiguration;
    }

    /**
     * The information about where the index should get the document information from the database. (Required)
     */
    @Updatable
    @Required
    public DatabaseColumnConfiguration getColumnConfiguration() {
        return columnConfiguration;
    }

    public void setColumnConfiguration(DatabaseColumnConfiguration columnConfiguration) {
        this.columnConfiguration = columnConfiguration;
    }

    /**
     * The information necessary to connect to a database. (Required)
     */
    @Updatable
    @Required
    public DatabaseConnectionConfiguration getConnectionConfiguration() {
        return connectionConfiguration;
    }

    public void setConnectionConfiguration(DatabaseConnectionConfiguration connectionConfiguration) {
        this.connectionConfiguration = connectionConfiguration;
    }

    /**
     * The information about how Amazon Kendra uses quote marks around SQL identifiers when querying a database data source.
     */
    @Updatable
    public DatabaseSqlConfiguration getSqlConfiguration() {
        return sqlConfiguration;
    }

    public void setSqlConfiguration(DatabaseSqlConfiguration sqlConfiguration) {
        this.sqlConfiguration = sqlConfiguration;
    }

    /**
     * The value of the VpcConfiguration property for this object.
     */
    @Updatable
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
    public void copyFrom(DatabaseConfiguration model) {
        setEngineType(model.databaseEngineType());

        DatabaseColumnConfiguration columnConfig = newSubresource(DatabaseColumnConfiguration.class);
        columnConfig.copyFrom(model.columnConfiguration());
        setColumnConfiguration(columnConfig);

        DatabaseConnectionConfiguration connectionConfig = newSubresource(DatabaseConnectionConfiguration.class);
        connectionConfig.copyFrom(model.connectionConfiguration());
        setConnectionConfiguration(connectionConfig);

        if (model.aclConfiguration() != null) {
            DatabaseAclConfiguration aclConfig = newSubresource(DatabaseAclConfiguration.class);
            aclConfig.copyFrom(model.aclConfiguration());
            setAclConfiguration(aclConfig);
        }

        if (model.sqlConfiguration() != null) {
            DatabaseSqlConfiguration sqlConfig = newSubresource(DatabaseSqlConfiguration.class);
            sqlConfig.copyFrom(model.sqlConfiguration());
            setSqlConfiguration(sqlConfig);
        }

        if (model.vpcConfiguration() != null) {
            KendraDataSourceVpcConfiguration vpcConfig = newSubresource(KendraDataSourceVpcConfiguration.class);
            vpcConfig.copyFrom(model.vpcConfiguration());
            setVpcConfiguration(vpcConfig);
        }
    }

    public DatabaseConfiguration toDatabaseConfiguration() {
        DatabaseConfiguration.Builder builder = DatabaseConfiguration.builder()
            .databaseEngineType(getEngineType())
            .columnConfiguration(getColumnConfiguration().toColumnConfiguration())
            .connectionConfiguration(getConnectionConfiguration().toConnectionConfiguration());

        if (getAclConfiguration() != null) {
            builder = builder.aclConfiguration(getAclConfiguration().toAclConfiguration());
        }

        if (getSqlConfiguration() != null) {
            builder = builder.sqlConfiguration(getSqlConfiguration().toSqlConfiguration());
        }

        if (getVpcConfiguration() != null) {
            builder = builder.vpcConfiguration(getVpcConfiguration().toDataSourceVpcConfiguration());
        }

        return builder.build();
    }
}
