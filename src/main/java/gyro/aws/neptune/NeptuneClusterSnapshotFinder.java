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

package gyro.aws.neptune;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.neptune.NeptuneClient;
import software.amazon.awssdk.services.neptune.model.DBClusterSnapshot;
import software.amazon.awssdk.services.neptune.model.DescribeDbClusterSnapshotsResponse;

/**
 * Query Neptune cluster snapshot.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    neptune-cluster-snapshot: $(external-query aws::neptune-cluster-snapshot { db-cluster-snapshot-identifier: 'neptune-cluster-snapshot-example', db-cluster-identifier: 'neptune-cluster-example', snapshot-type: 'manual' })
 */
@Type("neptune-cluster-snapshot")
public class NeptuneClusterSnapshotFinder extends AwsFinder<NeptuneClient, DBClusterSnapshot, NeptuneClusterSnapshotResource> {

    private String dbClusterSnapshotIdentifier;
    private String dbClusterIdentifier;
    private String snapshotType;

    /**
     * The unique name of the Neptune cluster snapshot.
     */
    public String getDbClusterSnapshotIdentifier() {
        return dbClusterSnapshotIdentifier;
    }

    public void setDbClusterSnapshotIdentifier(String dbClusterSnapshotIdentifier) {
        this.dbClusterSnapshotIdentifier = dbClusterSnapshotIdentifier;
    }

    /**
     * The Neptune cluster identifier of the Neptune cluster that this Neptune cluster snapshot was created from.
     */
    public String getDbClusterIdentifier() {
        return dbClusterIdentifier;
    }

    public void setDbClusterIdentifier(String dbClusterIdentifier) {
        this.dbClusterIdentifier = dbClusterIdentifier;
    }

    /**
     * The type of the Neptune cluster snapshot. Valid values are ``automated``, ``manual``, ``shared``, and ``public``.
     */
    public String getSnapshotType() {
        return snapshotType;
    }

    public void setSnapshotType(String snapshotType) {
        this.snapshotType = snapshotType;
    }

    @Override
    protected List<DBClusterSnapshot> findAllAws(NeptuneClient client) {
        return client.describeDBClusterSnapshots().dbClusterSnapshots().stream().collect(Collectors.toList());
    }

    @Override
    protected List<DBClusterSnapshot> findAws(NeptuneClient client, Map<String, String> filters) {
        DescribeDbClusterSnapshotsResponse response = client.describeDBClusterSnapshots(
            r -> r.dbClusterSnapshotIdentifier(filters.get("db-cluster-snapshot-identifier"))
                .dbClusterIdentifier(filters.get("db-cluster-identifier"))
                .snapshotType(filters.get("snapshot-type"))
        );

        return response.dbClusterSnapshots().stream().collect(Collectors.toList());
    }
}
