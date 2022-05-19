/*
 * Copyright 2022, Brightspot.
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

package gyro.aws.eventbridge;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.eventbridge.model.RedshiftDataParameters;

public class RedshiftDataParameter extends Diffable implements Copyable<RedshiftDataParameters> {

    private String database;
    private String dbUser;
    private String secretManagerArn;
    private String sql;
    private String statementName;
    private Boolean withEvent;

    /**
     * The database name.
     */
    @Required
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    /**
     * The user name for the database.
     */
    @Required
    @Updatable
    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    /**
     * The name or ARN of the secret that enables access to the database.
     */
    @Updatable
    public String getSecretManagerArn() {
        return secretManagerArn;
    }

    public void setSecretManagerArn(String secretManagerArn) {
        this.secretManagerArn = secretManagerArn;
    }

    /**
     * The SQL statement test to run.
     */
    @Updatable
    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    /**
     * The name of the sql statement.
     */
    @Updatable
    public String getStatementName() {
        return statementName;
    }

    public void setStatementName(String statementName) {
        this.statementName = statementName;
    }

    /**
     * When set to ``true``, an event is sent back to EventBridge after the SQL statement runs.
     */
    @Updatable
    public Boolean getWithEvent() {
        return withEvent;
    }

    public void setWithEvent(Boolean withEvent) {
        this.withEvent = withEvent;
    }

    @Override
    public void copyFrom(RedshiftDataParameters model) {
        setDatabase(model.database());
        setDbUser(model.dbUser());
        setSecretManagerArn(model.secretManagerArn());
        setSql(model.sql());
        setStatementName(model.statementName());
        setWithEvent(model.withEvent());
    }

    @Override
    public String primaryKey() {
        return getDatabase();
    }

    protected RedshiftDataParameters toRedshiftDataParameters() {
        RedshiftDataParameters.Builder builder = RedshiftDataParameters.builder().database(getDatabase());

        if (getDbUser() != null) {
            builder = builder.dbUser(getDbUser());
        }

        if (getSecretManagerArn() != null) {
            builder = builder.secretManagerArn(getSecretManagerArn());
        }

        if (getSql() != null) {
            builder = builder.sql(getSql());
        }

        if (getStatementName() != null) {
            builder = builder.statementName(getStatementName());
        }

        if (getWithEvent() != null) {
            builder = builder.withEvent(getWithEvent());
        }

        return builder.build();
    }
}
