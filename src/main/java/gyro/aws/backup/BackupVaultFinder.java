/*
 * Copyright 2021, Brightspot.
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

package gyro.aws.backup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.backup.BackupClient;
import software.amazon.awssdk.services.backup.model.BackupException;
import software.amazon.awssdk.services.backup.model.DescribeBackupVaultResponse;

/**
 * Query Backup Vault.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    backup-vault: $(external-query aws::backup-vault { name: "target-vault" })
 */
@Type("backup-vault")
public class BackupVaultFinder extends AwsFinder<BackupClient, DescribeBackupVaultResponse, BackupVaultResource> {

    private String name;

    /**
     * The name of the backup vault.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<DescribeBackupVaultResponse> findAllAws(BackupClient client) {
        return client.listBackupVaults().backupVaultList().stream()
            .map(v -> client.describeBackupVault(r -> r.backupVaultName(v.backupVaultName())))
            .collect(Collectors.toList());
    }

    @Override
    protected List<DescribeBackupVaultResponse> findAws(BackupClient client, Map<String, String> filters) {
        List<DescribeBackupVaultResponse> responses = new ArrayList<>();

        try {
            responses = Collections.singletonList(client.describeBackupVault(r -> r.backupVaultName(filters.get("name"))));
        } catch (BackupException ex) {
            // ignore
        }

        return responses;
    }
}
