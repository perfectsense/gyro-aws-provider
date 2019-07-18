package gyro.aws.rds;

import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.scope.State;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.CreateDbSnapshotResponse;
import software.amazon.awssdk.services.rds.model.DBSnapshot;
import software.amazon.awssdk.services.rds.model.DbSnapshotNotFoundException;
import software.amazon.awssdk.services.rds.model.DescribeDbSnapshotsResponse;
import software.amazon.awssdk.services.rds.model.InvalidDbInstanceStateException;

import java.util.Set;

/**
 * Create a db snapshot.
 *
 * .. code-block:: gyro
 *
 *    aws::db-snapshot db-snapshot-example
 *        db-instance: $(aws::db-instance db-instance-example)
 *        name: "db-snapshot-example"
 *        tags: {
 *            Name: "db-snapshot-example"
 *        }
 *    end
 */
@Type("db-snapshot")
public class DbSnapshotResource extends RdsTaggableResource implements Copyable<DBSnapshot> {

    private DbInstanceResource dbInstance;
    private String name;
    private String engineVersion;
    private DbOptionGroupResource optionGroup;

    /**
     * The DB instance to create a snapshot for. (Required)
     */
    public DbInstanceResource getDbInstance() {
        return dbInstance;
    }

    public void setDbInstance(DbInstanceResource dbInstance) {
        this.dbInstance = dbInstance;
    }

    /**
     * The unique identifier of the DB instance snapshot. (Required)
     */
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The engine version to upgrade the DB snapshot to.
     */
    @Updatable
    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    /**
     * The option group associate with the upgraded DB snapshot. Only applicable when upgrading an Oracle DB snapshot.
     */
    @Updatable
    public DbOptionGroupResource getOptionGroup() {
        return optionGroup;
    }

    public void setOptionGroup(DbOptionGroupResource optionGroup) {
        this.optionGroup = optionGroup;
    }

    @Override
    public void copyFrom(DBSnapshot snapshot) {
        setDbInstance(findById(DbInstanceResource.class, snapshot.dbInstanceIdentifier()));
        setEngineVersion(snapshot.engineVersion());
        setOptionGroup(findById(DbOptionGroupResource.class, snapshot.optionGroupName()));
        setArn(snapshot.dbSnapshotArn());
    }

    @Override
    protected boolean doRefresh() {
        RdsClient client = createClient(RdsClient.class);

        if (ObjectUtils.isBlank(getName())) {
            throw new GyroException("name is missing, unable to load db snapshot.");
        }

        try {
            DescribeDbSnapshotsResponse response = client.describeDBSnapshots(
                r -> r.dbSnapshotIdentifier(getName())
            );

            response.dbSnapshots().forEach(this::copyFrom);

        } catch (DbSnapshotNotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    protected void doCreate() {
        try {
            RdsClient client = createClient(RdsClient.class);
            CreateDbSnapshotResponse response = client.createDBSnapshot(
                r -> r.dbInstanceIdentifier(getDbInstance().getName())
                    .dbSnapshotIdentifier(getName())
            );

            setArn(response.dbSnapshot().dbSnapshotArn());
        } catch (InvalidDbInstanceStateException ex) {
            throw new GyroException(ex.getLocalizedMessage());
        }
    }

    @Override
    protected void doUpdate(Resource config, Set<String> changedProperties) {
        RdsClient client = createClient(RdsClient.class);
        client.modifyDBSnapshot(
            r -> r.dbSnapshotIdentifier(getName())
                    .engineVersion(getEngineVersion())
                    .optionGroupName(getOptionGroup() != null ? getOptionGroup().getName() : null)
        );
    }

    @Override
    public void delete(GyroUI ui, State state) {
        RdsClient client = createClient(RdsClient.class);
        client.deleteDBSnapshot(
            r -> r.dbSnapshotIdentifier(getName())
        );
    }
}
