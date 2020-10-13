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
import software.amazon.awssdk.services.kendra.model.ConnectionConfiguration;

public class DatabaseConnectionConfiguration extends Diffable implements Copyable<ConnectionConfiguration> {

    private String databaseHost;
    private String databaseName;
    private Integer databasePort;
    private String secret;
    private String tableName;

    /**
     * The name of the host for the database. (Required)
     */
    @Updatable
    @Required
    public String getDatabaseHost() {
        return databaseHost;
    }

    public void setDatabaseHost(String databaseHost) {
        this.databaseHost = databaseHost;
    }

    /**
     * The name of the database containing the document data. (Required)
     */
    @Updatable
    @Required
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * The port that the database uses for connections. (Required)
     */
    @Updatable
    @Required
    public Integer getDatabasePort() {
        return databasePort;
    }

    public void setDatabasePort(Integer databasePort) {
        this.databasePort = databasePort;
    }

    /**
     * The Amazon Resource Name (ARN) of credentials stored in AWS Secrets Manager. (Required)
     */
    @Updatable
    @Required
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * The name of the table that contains the document data. (Required)
     */
    @Updatable
    @Required
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(ConnectionConfiguration model) {
        setDatabaseHost(model.databaseHost());
        setDatabaseName(model.databaseName());
        setDatabasePort(model.databasePort());
        setSecret(model.secretArn());
        setTableName(model.tableName());
    }

    public ConnectionConfiguration toConnectionConfiguration() {
        return ConnectionConfiguration.builder()
            .databaseHost(getDatabaseHost())
            .databaseName(getDatabaseName())
            .databasePort(getDatabasePort())
            .secretArn(getSecret())
            .tableName(getTableName())
            .build();
    }
}
