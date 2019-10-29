/*
 * Copyright 2019, Perfect Sense, Inc.
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

package gyro.aws.elasticache;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.model.Snapshot;
import software.amazon.awssdk.services.elasticache.model.SnapshotNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query cache snapshot.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    cache-snapshots: $(external-query aws::elasticache-snapshot { snapshot-name: 'cache-snapshot-example'})
 */
@Type("elasticache-snapshot")
public class SnapshotFinder extends AwsFinder<ElastiCacheClient, Snapshot, SnapshotResource> {

    private String snapshotName;

    /**
     * Name of the snapshot.
     */
    public String getSnapshotName() {
        return snapshotName;
    }

    public void setSnapshotName(String snapshotName) {
        this.snapshotName = snapshotName;
    }

    @Override
    protected List<Snapshot> findAllAws(ElastiCacheClient client) {
        return client.describeSnapshotsPaginator().snapshots().stream().collect(Collectors.toList());
    }

    @Override
    protected List<Snapshot> findAws(ElastiCacheClient client, Map<String, String> filters) {
        try {
            return client.describeSnapshots(r -> r.snapshotName(filters.get("snapshot-name"))).snapshots();
        } catch (SnapshotNotFoundException ex) {
            return Collections.emptyList();
        }
    }
}
