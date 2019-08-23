package gyro.aws.rds;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBClusterSnapshot;
import software.amazon.awssdk.services.rds.model.DbClusterSnapshotNotFoundException;
import software.amazon.awssdk.services.rds.model.DescribeDbClusterSnapshotsRequest;
import software.amazon.awssdk.services.rds.model.DescribeDbClusterSnapshotsResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Query db cluster snapshot.
 *
 * .. code-block:: gyro
 *
 *    cluster-snapshots: $(external-query aws::db-cluster-snapshot { identifier: 'db-cluster-snapshot-example'})
 */
@Type("db-cluster-snapshot")
public class DbClusterSnapshotFinder extends AwsFinder<RdsClient, DBClusterSnapshot, DbClusterSnapshotResource> {

    private String identifier;

    /**
     * The identifier of the cluster snapshot.
     */
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    protected List<DBClusterSnapshot> findAws(RdsClient client, Map<String, String> filters) {
        if (!filters.containsKey("identifier")) {
            throw new IllegalArgumentException("'identifier' is required.");
        }

        try {
            return client.describeDBClusterSnapshots(r -> r.dbClusterSnapshotIdentifier(filters.get("identifier"))).dbClusterSnapshots();
        } catch (DbClusterSnapshotNotFoundException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<DBClusterSnapshot> findAllAws(RdsClient client) {
        List<DBClusterSnapshot> dbClusterSnapshots = new ArrayList<>();
        String marker = null;
        DescribeDbClusterSnapshotsResponse response;

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.describeDBClusterSnapshots();
            } else {
                response = client.describeDBClusterSnapshots(DescribeDbClusterSnapshotsRequest.builder().marker(marker).build());
            }

            marker = response.marker();
            dbClusterSnapshots.addAll(response.dbClusterSnapshots());
        } while (!ObjectUtils.isBlank(marker));

        return dbClusterSnapshots;
    }

}
