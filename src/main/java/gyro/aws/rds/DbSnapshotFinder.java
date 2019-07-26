package gyro.aws.rds;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.DBSnapshot;
import software.amazon.awssdk.services.rds.model.DbSnapshotNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query db snapshot.
 *
 * .. code-block:: gyro
 *
 *    db-snapshots: $(aws::db-snapshot EXTERNAL/* | name = 'db-snapshot-example')
 */
@Type("db-snapshot")
public class DbSnapshotFinder extends AwsFinder<RdsClient, DBSnapshot, DbSnapshotResource> {

    private String identifier;

    /**
     * The identifier of the db snapshot.
     */
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    protected List<DBSnapshot> findAws(RdsClient client, Map<String, String> filters) {
        if (!filters.containsKey("identifier")) {
            throw new IllegalArgumentException("'identifier' is required.");
        }

        try {
            return client.describeDBSnapshots(r -> r.dbSnapshotIdentifier(filters.get("identifier"))).dbSnapshots();
        } catch (DbSnapshotNotFoundException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<DBSnapshot> findAllAws(RdsClient client) {
        return client.describeDBSnapshotsPaginator().dbSnapshots().stream().collect(Collectors.toList());
    }

}
