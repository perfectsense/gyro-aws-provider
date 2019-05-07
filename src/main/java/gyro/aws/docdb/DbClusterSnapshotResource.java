package gyro.aws.docdb;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.GyroCore;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceName;
import gyro.core.resource.ResourceOutput;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.CreateDbClusterSnapshotResponse;
import software.amazon.awssdk.services.docdb.model.DBClusterSnapshot;
import software.amazon.awssdk.services.docdb.model.DbClusterNotFoundException;
import software.amazon.awssdk.services.docdb.model.DbClusterSnapshotNotFoundException;
import software.amazon.awssdk.services.docdb.model.DescribeDbClusterSnapshotsResponse;
import software.amazon.awssdk.services.docdb.model.DescribeDbClustersResponse;
import software.amazon.awssdk.services.docdb.model.DescribeDbInstancesResponse;

import java.util.Set;

/**
 * Creates an Document db cluster snapshot.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::db-cluster-snapshot db-cluster-snapshot-example
 *         db-cluster-identifier: $(aws::db-cluster db-cluster-db-cluster-snapshot-example | db-cluster-identifier)
 *         db-cluster-snapshot-identifier: "db-cluster-snapshot-example"
 *
 *         tags: {
 *             Name: "db-cluster-snapshot-example"
 *         }
 *     end
 */
@ResourceName("db-cluster-snapshot")
public class DbClusterSnapshotResource extends DocDbTaggableResource {
    private String dbClusterIdentifier;
    private String dbClusterSnapshotIdentifier;

    private String arn;

    /**
     * Associated db cluster name. (Required)
     */
    public String getDbClusterIdentifier() {
        return dbClusterIdentifier;
    }

    public void setDbClusterIdentifier(String dbClusterIdentifier) {
        this.dbClusterIdentifier = dbClusterIdentifier;
    }

    /**
     * The name of the db cluster snapshot. (Required)
     */
    public String getDbClusterSnapshotIdentifier() {
        return dbClusterSnapshotIdentifier;
    }

    public void setDbClusterSnapshotIdentifier(String dbClusterSnapshotIdentifier) {
        this.dbClusterSnapshotIdentifier = dbClusterSnapshotIdentifier;
    }

    /**
     * The arn of the db cluster snapshot.
     */
    @ResourceOutput
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

        DescribeDbClusterSnapshotsResponse response = client.describeDBClusterSnapshots(
            r -> r.dbClusterSnapshotIdentifier(getDbClusterIdentifier())
                .dbClusterSnapshotIdentifier(getDbClusterSnapshotIdentifier())
        );

        if (!response.dbClusterSnapshots().isEmpty()) {
            DBClusterSnapshot dbClusterSnapshot = response.dbClusterSnapshots().get(0);
            setArn(dbClusterSnapshot.dbClusterSnapshotArn());
            setDbClusterIdentifier(dbClusterSnapshot.dbClusterIdentifier());

            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void doCreate() {
        DocDbClient client = createClient(DocDbClient.class);

        CreateDbClusterSnapshotResponse response = client.createDBClusterSnapshot(
            r -> r.dbClusterIdentifier(getDbClusterIdentifier())
                .dbClusterSnapshotIdentifier(getDbClusterSnapshotIdentifier())
        );

        setArn(response.dbClusterSnapshot().dbClusterSnapshotArn());

        waitForAvailability(client);
    }

    @Override
    protected void doUpdate(Resource current, Set changedProperties) {

    }

    @Override
    public void delete() {
        DocDbClient client = createClient(DocDbClient.class);

        client.deleteDBClusterSnapshot(
            r -> r.dbClusterSnapshotIdentifier(getDbClusterSnapshotIdentifier())
        );

        waitForDelete(client);
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("db cluster snapshot");

        if (!ObjectUtils.isBlank(getDbClusterSnapshotIdentifier())) {
            sb.append(" - ").append(getDbClusterSnapshotIdentifier());
        }

        return sb.toString();
    }

    private void waitForAvailability(DocDbClient client) {
        boolean available = false;
        int count = 0;
        while (!available && count < 6) {
            DescribeDbClusterSnapshotsResponse response = waitHelper(count, client, 10000);

            available = response.dbClusterSnapshots().get(0).status().equals("available");
            count++;

            if (!available && count == 6) {
                boolean wait = GyroCore.ui().readBoolean(Boolean.FALSE, "\nWait for completion?..... ");
                if (wait) {
                    count = 0;
                }
            }
        }
    }

    private void waitForDelete(DocDbClient client) {
        boolean deleted = false;
        int count = 0;
        while (!deleted && count < 6) {
            try {
                DescribeDbClusterSnapshotsResponse response = waitHelper(count, client, 10000);

                deleted = response.dbClusterSnapshots().isEmpty();
            } catch (DbClusterSnapshotNotFoundException ex) {
                deleted = true;
            }
            count++;
        }
    }

    private DescribeDbClusterSnapshotsResponse waitHelper(int count, DocDbClient client, long interval) {
        if (count > 0) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return client.describeDBClusterSnapshots(
            r -> r.dbClusterSnapshotIdentifier(getDbClusterIdentifier())
                .dbClusterSnapshotIdentifier(getDbClusterSnapshotIdentifier())
        );
    }
}
