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
 *    cluster-snapshots: $(aws::db-cluster-snapshot EXTERNAL/* | name = 'db-cluster-snapshot-example')
 */
@Type("db-cluster-snapshot")
public class DbClusterSnapshotFinder extends AwsFinder<RdsClient, DBClusterSnapshot, DbClusterSnapshotResource> {

    private String name;

    /**
     * The identifier of the cluster snapshot.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<DBClusterSnapshot> findAws(RdsClient client, Map<String, String> filters) {
        if (!filters.containsKey("name")) {
            throw new IllegalArgumentException("'name' is required.");
        }

        try {
            return client.describeDBClusterSnapshots(r -> r.dbClusterSnapshotIdentifier(filters.get("name"))).dbClusterSnapshots();
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
