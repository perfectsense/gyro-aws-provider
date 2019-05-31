package gyro.aws.rds;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBClusterSnapshot;
import software.amazon.awssdk.services.rds.model.DbClusterSnapshotNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Type("db-cluster-snapshot")
public class DbClusterSnapshotFinder extends AwsFinder<RdsClient, DBClusterSnapshot, DbClusterSnapshotResource> {

    private String dbClusterSnapshotIdentifier;

    public String getDbClusterSnapshotIdentifier() {
        return dbClusterSnapshotIdentifier;
    }

    public void setDbClusterSnapshotIdentifier(String dbClusterSnapshotIdentifier) {
        this.dbClusterSnapshotIdentifier = dbClusterSnapshotIdentifier;
    }

    @Override
    protected List<DBClusterSnapshot> findAws(RdsClient client, Map<String, String> filters) {
        try {
            return client.describeDBClusterSnapshots(r -> r.dbClusterSnapshotIdentifier(filters.get("db-cluster-snapshot-identifier"))).dbClusterSnapshots();
        } catch (DbClusterSnapshotNotFoundException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<DBClusterSnapshot> findAllAws(RdsClient client) {
        return client.describeDBClusterSnapshots().dbClusterSnapshots();
    }

}
