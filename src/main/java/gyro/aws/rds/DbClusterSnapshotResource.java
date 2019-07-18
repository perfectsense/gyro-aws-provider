package gyro.aws.rds;

import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.scope.State;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.CreateDbClusterSnapshotResponse;
import software.amazon.awssdk.services.rds.model.DBClusterSnapshot;
import software.amazon.awssdk.services.rds.model.DbClusterSnapshotNotFoundException;
import software.amazon.awssdk.services.rds.model.DescribeDbClusterSnapshotsResponse;
import software.amazon.awssdk.services.rds.model.InvalidDbClusterStateException;

import java.util.Set;

/**
 * Create a db cluster snapshot.
 *
 * .. code-block:: gyro
 *
 *    aws::db-cluster-snapshot db-cluster-snapshot-example
 *        db-cluster: $(aws::db-cluster db-cluster-example)
 *        name: "db-cluster-snapshot-example"
 *        tags: {
 *            Name: "db-cluster-snapshot-example"
 *        }
 *    end
 */
@Type("db-cluster-snapshot")
public class DbClusterSnapshotResource extends RdsTaggableResource implements Copyable<DBClusterSnapshot> {

    private DbClusterResource dbCluster;
    private String name;

    /**
     * The DB cluster to create a snapshot for. (Required)
     */
    public DbClusterResource getDbCluster() {
        return dbCluster;
    }

    public void setDbCluster(DbClusterResource dbCluster) {
        this.dbCluster = dbCluster;
    }

    /**
     * The unique identifier of the DB cluster snapshot. (Required)
     */
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void copyFrom(DBClusterSnapshot snapshot) {
        setDbCluster(findById(DbClusterResource.class, snapshot.dbClusterIdentifier()));
        setArn(snapshot.dbClusterSnapshotArn());
    }

    @Override
    protected boolean doRefresh() {
        RdsClient client = createClient(RdsClient.class);

        if (ObjectUtils.isBlank(getName())) {
            throw new GyroException("name is missing, unable to load db cluster snapshot.");
        }

        try {
            DescribeDbClusterSnapshotsResponse response = client.describeDBClusterSnapshots(
                r -> r.dbClusterSnapshotIdentifier(getName())
            );

            response.dbClusterSnapshots().forEach(this::copyFrom);

        } catch (DbClusterSnapshotNotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    protected void doCreate() {
        try {
            RdsClient client = createClient(RdsClient.class);
            CreateDbClusterSnapshotResponse response = client.createDBClusterSnapshot(
                r -> r.dbClusterIdentifier(getDbCluster().getName())
                    .dbClusterSnapshotIdentifier(getName())
            );

            setArn(response.dbClusterSnapshot().dbClusterSnapshotArn());
        } catch (InvalidDbClusterStateException ex) {
            throw new GyroException(ex.getLocalizedMessage());
        }
    }

    @Override
    protected void doUpdate(Resource config, Set<String> changedProperties) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        RdsClient client = createClient(RdsClient.class);
        client.deleteDBClusterSnapshot(
            r -> r.dbClusterSnapshotIdentifier(getName())
        );
    }
}
