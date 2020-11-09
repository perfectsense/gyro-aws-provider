/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.dynamodb;

import java.util.Optional;
import java.util.Set;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.BackupDescription;
import software.amazon.awssdk.services.dynamodb.model.BackupDetails;
import software.amazon.awssdk.services.dynamodb.model.CreateBackupResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.SourceTableDetails;

/**
 * Creates a backup of a DynamoDb table.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::dynamodb-table-backup dynamodb-table-backup-example
 *         table-name: "dynamodb-table-example"
 *         backup-name: "dynamodb-table-backup-example"
 *     end
 */
@Type("dynamodb-table-backup")
public class DynamoDbTableBackupResource extends AwsResource implements Copyable<BackupDescription> {

    private String tableName;
    private String backupName;

    // Read-only
    private String backupArn;

    /**
     * The name of the DynamoDb table.
     */
    @Required
    @Regex("[a-zA-Z0-9_.-]+")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * The name of the table backup.
     */
    @Required
    @Regex("[a-zA-Z0-9_.-]+")
    public String getBackupName() {
        return backupName;
    }

    public void setBackupName(String backupName) {
        this.backupName = backupName;
    }

    /**
     * The ARN of the table backup.
     */
    @Id
    @Output
    public String getBackupArn() {
        return backupArn;
    }

    public void setBackupArn(String backupArn) {
        this.backupArn = backupArn;
    }

    @Override
    public void copyFrom(BackupDescription model) {
        setTableName(Optional.ofNullable(model.sourceTableDetails()).map(SourceTableDetails::tableName).orElse(null));
        setBackupName(Optional.ofNullable(model.backupDetails()).map(BackupDetails::backupName).orElse(null));
        setBackupArn(Optional.ofNullable(model.backupDetails()).map(BackupDetails::backupArn).orElse(null));
    }

    @Override
    public boolean refresh() {
        DynamoDbClient client = createClient(DynamoDbClient.class);

        BackupDescription backup = getBackup(client);

        if (backup == null) {
            return false;
        }

        copyFrom(backup);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        DynamoDbClient client = createClient(DynamoDbClient.class);
        CreateBackupResponse backup = client.createBackup(r -> r.tableName(getTableName()).backupName(getBackupName()));

        setBackupArn(backup.backupDetails().backupArn());
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        DynamoDbClient client = createClient(DynamoDbClient.class);
        client.deleteBackup(r -> r.backupArn(getBackupArn()));
    }

    public BackupDescription getBackup(DynamoDbClient client) {
        try {
            return client.describeBackup(r -> r.backupArn(getBackupArn())).backupDescription();
        } catch (ResourceNotFoundException ignore) {
            return null;
        }
    }
}
