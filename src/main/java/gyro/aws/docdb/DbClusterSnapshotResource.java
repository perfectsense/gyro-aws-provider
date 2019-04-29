package gyro.aws.docdb;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceName;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.CreateDbClusterSnapshotResponse;
import software.amazon.awssdk.services.docdb.model.DBClusterSnapshot;
import software.amazon.awssdk.services.docdb.model.DescribeDbClusterSnapshotsResponse;

import java.util.Set;

@ResourceName("db-cluster-snapshot")
public class DbClusterSnapshotResource extends DocDbTaggableResource {
    private String dbClusterIdentifier;
    private String dbClusterSnapshotIdentifier;

    private String arn;

    public String getDbClusterIdentifier() {
        return dbClusterIdentifier;
    }

    public void setDbClusterIdentifier(String dbClusterIdentifier) {
        this.dbClusterIdentifier = dbClusterIdentifier;
    }

    public String getDbClusterSnapshotIdentifier() {
        return dbClusterSnapshotIdentifier;
    }

    public void setDbClusterSnapshotIdentifier(String dbClusterSnapshotIdentifier) {
        this.dbClusterSnapshotIdentifier = dbClusterSnapshotIdentifier;
    }

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
