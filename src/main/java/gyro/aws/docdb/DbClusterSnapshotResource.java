package gyro.aws.docdb;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Wait;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.Type;
import gyro.core.scope.State;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.CreateDbClusterSnapshotResponse;
import software.amazon.awssdk.services.docdb.model.DBClusterSnapshot;
import software.amazon.awssdk.services.docdb.model.DbSnapshotNotFoundException;
import software.amazon.awssdk.services.docdb.model.DescribeDbClusterSnapshotsResponse;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Creates an Document db cluster snapshot.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::docdb-cluster-snapshot db-cluster-snapshot-example
 *         db-cluster: $(aws::db-cluster db-cluster-db-cluster-snapshot-example)
 *         identifier: "db-cluster-snapshot-example"
 *
 *         tags: {
 *             Name: "db-cluster-snapshot-example"
 *         }
 *     end
 */
@Type("docdb-cluster-snapshot")
public class DbClusterSnapshotResource extends DocDbTaggableResource implements Copyable<DBClusterSnapshot> {

    private DbClusterResource dbCluster;
    private String identifier;

    //-- Read-only Attributes

    private String arn;

    /**
     * Associated db cluster. (Required)
     */
    public DbClusterResource getDbCluster() {
        return dbCluster;
    }

    public void setDbCluster(DbClusterResource dbCluster) {
        this.dbCluster = dbCluster;
    }

    /**
     * The name of the db cluster snapshot. (Required)
     */
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * The arn of the db cluster snapshot.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    protected String getId() {
        return getArn();
    }

    @Override
    protected boolean doRefresh() {
        DocDbClient client = createClient(DocDbClient.class);

        DBClusterSnapshot dbClusterSnapshot = getDbClusterSnapshot(client);

        if (dbClusterSnapshot == null) {
            return false;
        }

        copyFrom(dbClusterSnapshot);

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        DocDbClient client = createClient(DocDbClient.class);

        CreateDbClusterSnapshotResponse response = client.createDBClusterSnapshot(
            r -> r.dbClusterIdentifier(getDbCluster().getIdentifier())
                .dbClusterSnapshotIdentifier(getIdentifier())
        );

        setArn(response.dbClusterSnapshot().dbClusterSnapshotArn());

        state.save();

        boolean waitResult = Wait.atMost(5, TimeUnit.MINUTES)
            .checkEvery(30, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> isAvailable(client));

        if (!waitResult) {
            throw new GyroException("Unable to reach 'available' state for docdb cluster snapshot - " + getDbClusterSnapshotIdentifier());
        }
    }

    @Override
    protected void doUpdate(Resource current, Set changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, State state) {
        DocDbClient client = createClient(DocDbClient.class);

        client.deleteDBClusterSnapshot(
            r -> r.dbClusterSnapshotIdentifier(getIdentifier())
        );

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> getDbClusterSnapshot(client) == null);
    }

    @Override
    public void copyFrom(DBClusterSnapshot dbClusterSnapshot) {
        setArn(dbClusterSnapshot.dbClusterSnapshotArn());
        setDbCluster(findById(DbClusterResource.class, dbClusterSnapshot.dbClusterIdentifier()));
        setIdentifier(dbClusterSnapshot.dbClusterSnapshotIdentifier());
    }

    private boolean isAvailable(DocDbClient client) {
        DBClusterSnapshot dbClusterSnapshot = getDbClusterSnapshot(client);

        return dbClusterSnapshot != null && dbClusterSnapshot.status().equals("available");
    }

    private DBClusterSnapshot getDbClusterSnapshot(DocDbClient client) {
        DBClusterSnapshot dbClusterSnapshot = null;

        if (ObjectUtils.isBlank(getDbCluster())) {
            throw new GyroException("db-cluster is missing, unable to load db cluster snapshot.");
        }

        if (ObjectUtils.isBlank(getIdentifier())) {
            throw new GyroException("identifier is missing, unable to load db cluster snapshot.");
        }

        try {
            DescribeDbClusterSnapshotsResponse response = client.describeDBClusterSnapshots(
                r -> r.dbClusterSnapshotIdentifier(getDbCluster().getIdentifier())
                    .dbClusterSnapshotIdentifier(getIdentifier())
            );

            if (!response.dbClusterSnapshots().isEmpty()) {
                dbClusterSnapshot = response.dbClusterSnapshots().get(0);
            }

        } catch (DbSnapshotNotFoundException ex) {

        }

        return dbClusterSnapshot;
    }

}
