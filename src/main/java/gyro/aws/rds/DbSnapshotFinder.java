package gyro.aws.rds;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBSnapshot;

import java.util.List;
import java.util.Map;

@Type("db-snapshot")
public class DbSnapshotFinder extends AwsFinder<RdsClient, DBSnapshot, DbSnapshotResource> {

    private String dbSnapshotIdentifier;

    public String getDbSnapshotIdentifier() {
        return dbSnapshotIdentifier;
    }

    public void setDbSnapshotIdentifier(String dbSnapshotIdentifier) {
        this.dbSnapshotIdentifier = dbSnapshotIdentifier;
    }

    @Override
    protected List<DBSnapshot> findAws(RdsClient client, Map<String, String> filters) {
        return client.describeDBSnapshots(r -> r.dbSnapshotIdentifier(filters.get("db-snapshot-identifier"))).dbSnapshots();
    }

    @Override
    protected List<DBSnapshot> findAllAws(RdsClient client) {
        return client.describeDBSnapshots().dbSnapshots();
    }

}
