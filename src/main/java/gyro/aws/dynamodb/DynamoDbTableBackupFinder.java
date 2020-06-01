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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.BackupDescription;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

/**
 * Query dynamodb-table-backup.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    dynamodb-table-backup: $(external-query aws::dynamodb-table-backup { backup-arn: ""})
 */
@Type("dynamodb-table-backup")
public class DynamoDbTableBackupFinder
    extends AwsFinder<DynamoDbClient, BackupDescription, DynamoDbTableBackupResource> {

    private String tableName;
    private String backupArn;

    /**
     * The DynamoDb table name.
     */
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * The table backup ARN.
     */
    public String getBackupArn() {
        return backupArn;
    }

    public void setBackupArn(String backupArn) {
        this.backupArn = backupArn;
    }

    @Override
    protected List<BackupDescription> findAllAws(DynamoDbClient client) {
        return client.listBackups().backupSummaries().stream()
            .map(t -> client.describeBackup(r -> r.backupArn(t.backupArn())).backupDescription())
            .collect(Collectors.toList());
    }

    @Override
    protected List<BackupDescription> findAws(DynamoDbClient client, Map<String, String> filters) {
        if (!filters.containsKey("backup-arn") && !filters.containsKey("table-name")) {
            throw new IllegalArgumentException("'table-name' or 'backup-arn' is required.");
        }

        try {
            if (filters.containsKey("backup-arn")) {
                return Collections.singletonList(
                    client.describeBackup(r -> r.backupArn(filters.get("backup-arn"))).backupDescription());
            } else {
                return client.listBackups(r -> r.tableName(filters.get("table-name")))
                    .backupSummaries()
                    .stream()
                    .map(t -> client.describeBackup(r -> r.backupArn(t.backupArn())).backupDescription())
                    .collect(Collectors.toList());
            }
        } catch (ResourceNotFoundException ex) {
            return Collections.emptyList();
        }
    }
}
