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

    private String name;

    /**
     * The identifier of the db snapshot.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<DBSnapshot> findAws(RdsClient client, Map<String, String> filters) {
        if (!filters.containsKey("name")) {
            throw new IllegalArgumentException("'name' is required.");
        }

        try {
            return client.describeDBSnapshots(r -> r.dbSnapshotIdentifier(filters.get("name"))).dbSnapshots();
        } catch (DbSnapshotNotFoundException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<DBSnapshot> findAllAws(RdsClient client) {
        return client.describeDBSnapshotsPaginator().dbSnapshots().stream().collect(Collectors.toList());
    }

}
