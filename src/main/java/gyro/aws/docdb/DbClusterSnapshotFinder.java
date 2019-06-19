package gyro.aws.docdb;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.DBClusterSnapshot;
import software.amazon.awssdk.services.docdb.model.DescribeDbClusterSnapshotsResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Type("docdb-cluster-snapshot")
public class DbClusterSnapshotFinder extends DocDbFinder<DocDbClient, DBClusterSnapshot, DbClusterSnapshotResource> {

    private String dbClusterId;
    private String dbClusterSnapshotId;
    private Boolean isPublic;
    private Boolean isShared;

    /**
     * The Document DB cluster identifier associated with a snapshot.
     */
    public String getDbClusterId() {
        return dbClusterId;
    }

    public void setDbClusterId(String dbClusterId) {
        this.dbClusterId = dbClusterId;
    }

    /**
     * The Document DB snapshot identifier.
     */
    public String getDbClusterSnapshotId() {
        return dbClusterSnapshotId;
    }

    public void setDbClusterSnapshotId(String dbClusterSnapshotId) {
        this.dbClusterSnapshotId = dbClusterSnapshotId;
    }

    /**
     * Include public snapshots.
     */
    public Boolean getPublic() {
        return isPublic;
    }

    public void setPublic(Boolean aPublic) {
        isPublic = aPublic;
    }

    /**
     * Include shared snapshots.
     */
    public Boolean getShared() {
        return isShared;
    }

    public void setShared(Boolean shared) {
        isShared = shared;
    }

    @Override
    protected List<DBClusterSnapshot> findAllAws(DocDbClient client) {
        List<DBClusterSnapshot> snapshots = new ArrayList<>();

        DescribeDbClusterSnapshotsResponse response = client.describeDBClusterSnapshots();
        do {
            snapshots.addAll(response.dbClusterSnapshots());

            if (response.marker() != null && !response.marker().isEmpty()) {
                final String marker =  response.marker();
                response = client.describeDBClusterSnapshots(r -> r.marker(marker));
            }
        } while (!ObjectUtils.isBlank(response.marker()));

        return snapshots;
    }

    @Override
    protected List<DBClusterSnapshot> findAws(DocDbClient client, Map<String, String> filters) {
        List<DBClusterSnapshot> snapshots = new ArrayList<>();

        DescribeDbClusterSnapshotsResponse response = client.describeDBClusterSnapshots(r -> r
            .dbClusterIdentifier(filters.get("db-cluster-id"))
            .dbClusterSnapshotIdentifier(filters.get("db-cluster-snapshot-id"))
            .includePublic(Boolean.valueOf(filters.get("public")))
            .includeShared(Boolean.getBoolean(filters.get("shared")))
        );

        do {
            snapshots.addAll(response.dbClusterSnapshots());

            if (response.marker() != null && !response.marker().isEmpty()) {
                final String marker = response.marker();
                response = client.describeDBClusterSnapshots(r -> r
                    .dbClusterIdentifier(filters.get("db-cluster-id"))
                    .dbClusterSnapshotIdentifier(filters.get("db-cluster-snapshot-id"))
                    .includePublic(Boolean.valueOf(filters.get("public")))
                    .includeShared(Boolean.getBoolean(filters.get("shared")))
                    .marker(marker)
                );
            }
        } while (!ObjectUtils.isBlank(response.marker()));

        return snapshots;
    }

}
