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

@Type("cache-snapshot")
public class SnapshotFinder extends AwsFinder<ElastiCacheClient, Snapshot, SnapshotResource> {

    private String snapshotName;

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
