package gyro.aws.docdb;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceName;
import gyro.core.resource.ResourceOutput;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.CreateDbClusterSnapshotResponse;
import software.amazon.awssdk.services.docdb.model.DBClusterSnapshot;
import software.amazon.awssdk.services.docdb.model.DescribeDbClusterSnapshotsResponse;

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

        DescribeDbClusterSnapshotsResponse response = client.describeDBClusterSnapshots();

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
}
