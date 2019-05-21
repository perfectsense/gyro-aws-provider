package gyro.aws.docdb;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.GyroException;
import gyro.core.Wait;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.resource.ResourceType;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.CreateDbInstanceResponse;
import software.amazon.awssdk.services.docdb.model.DBInstance;
import software.amazon.awssdk.services.docdb.model.DbInstanceNotFoundException;
import software.amazon.awssdk.services.docdb.model.DescribeDbInstancesResponse;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Creates an Document db instance.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::docdb-instance db-instance-example
 *         availability-zone: "us-east-2a"
 *         db-instance-class: "db.r4.large"
 *         db-instance-identifier: "db-instance-example"
 *         engine: "docdb"
 *         preferred-maintenance-window: "wed:03:28-wed:04:58"
 *         promotion-tier: 1
 *         db-cluster-identifier: $(aws::db-cluster db-cluster-db-instance-example | db-cluster-identifier)
 *
 *         tags: {
 *             Name: "db-instance-example"
 *         }
 *     end
 */
@ResourceType("docdb-instance")
public class DbInstance extends DocDbTaggableResource {
    private Boolean autoMinorVersionUpgrade;
    private String availabilityZone;
    private String dbInstanceClass;
    private String dbInstanceIdentifier;
    private String engine;
    private String preferredMaintenanceWindow;
    private Integer promotionTier;
    private String dbClusterIdentifier;

    private String status;
    private String arn;

    /**
     * Enable auto minor version upgrade.
     */
    @Updatable
    public Boolean getAutoMinorVersionUpgrade() {
        return autoMinorVersionUpgrade;
    }

    public void setAutoMinorVersionUpgrade(Boolean autoMinorVersionUpgrade) {
        this.autoMinorVersionUpgrade = autoMinorVersionUpgrade;
    }

    /**
     * Set availability zone for the instance. Must belong to one of the ones specified by the associated db cluster. (Required)
     */
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * Set the size of the data base instance. (Required)
     */
    @Updatable
    public String getDbInstanceClass() {
        return dbInstanceClass;
    }

    public void setDbInstanceClass(String dbInstanceClass) {
        this.dbInstanceClass = dbInstanceClass;
    }

    /**
     * Name of the database instance. (Required)
     */
    public String getDbInstanceIdentifier() {
        return dbInstanceIdentifier;
    }

    public void setDbInstanceIdentifier(String dbInstanceIdentifier) {
        this.dbInstanceIdentifier = dbInstanceIdentifier;
    }

    /**
     * Engine used by the instance. (Required)
     */
    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    /**
     * Set the preferred maintenance window. Valid format ``ddd:hh24:mi-ddd:hh24:mi``. (Required)
     */
    @Updatable
    public String getPreferredMaintenanceWindow() {
        return preferredMaintenanceWindow;
    }

    public void setPreferredMaintenanceWindow(String preferredMaintenanceWindow) {
        this.preferredMaintenanceWindow = preferredMaintenanceWindow;
    }

    /**
     * Set the promotion tier. Valid values ``0-15``. (Required)
     */
    @Updatable
    public Integer getPromotionTier() {
        return promotionTier;
    }

    public void setPromotionTier(Integer promotionTier) {
        this.promotionTier = promotionTier;
    }

    /**
     * Set the name of the parent db cluster. (Required)
     */
    public String getDbClusterIdentifier() {
        return dbClusterIdentifier;
    }

    public void setDbClusterIdentifier(String dbClusterIdentifier) {
        this.dbClusterIdentifier = dbClusterIdentifier;
    }

    /**
     * The status of the db instance.
     */
    @Output
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * The arn of the db instance.
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

        DBInstance dbInstance = getDbInstance(client);

        if (dbInstance == null) {
            return false;
        }

        setAutoMinorVersionUpgrade(dbInstance.autoMinorVersionUpgrade());
        setAvailabilityZone(dbInstance.availabilityZone());
        setDbInstanceClass(dbInstance.dbInstanceClass());
        setEngine(dbInstance.engine());
        setPreferredMaintenanceWindow(dbInstance.preferredMaintenanceWindow());
        setPromotionTier(dbInstance.promotionTier());
        setArn(dbInstance.dbInstanceArn());
        setStatus(dbInstance.dbInstanceStatus());

        return true;
    }

    @Override
    protected void doCreate() {
        DocDbClient client = createClient(DocDbClient.class);

        CreateDbInstanceResponse response = client.createDBInstance(
            r -> r.autoMinorVersionUpgrade(getAutoMinorVersionUpgrade())
                .availabilityZone(getAvailabilityZone())
                .dbInstanceClass(getDbInstanceClass())
                .dbInstanceIdentifier(getDbInstanceIdentifier())
                .engine(getEngine())
                .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
                .promotionTier(getPromotionTier())
                .dbClusterIdentifier(getDbClusterIdentifier())
        );

        setArn(response.dbInstance().dbInstanceArn());

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> isAvailable(client));

        doRefresh();
    }

    @Override
    protected void doUpdate(Resource current, Set changedProperties) {
        DocDbClient client = createClient(DocDbClient.class);

        client.modifyDBInstance(
            r -> r.autoMinorVersionUpgrade(getAutoMinorVersionUpgrade())
                .dbInstanceClass(getDbInstanceClass())
                .dbInstanceIdentifier(getDbInstanceIdentifier())
                .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
                .promotionTier(getPromotionTier())
        );

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> isAvailable(client));
    }

    @Override
    public void delete() {
        DocDbClient client = createClient(DocDbClient.class);

        client.deleteDBInstance(
            r -> r.dbInstanceIdentifier(getDbInstanceIdentifier())
        );

        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> getDbInstance(client) == null);
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("db instance");

        if (!ObjectUtils.isBlank(getDbInstanceIdentifier())) {
            sb.append(" - ").append(getDbInstanceIdentifier());
        }

        return sb.toString();
    }

    private boolean isAvailable(DocDbClient client) {
        DBInstance dbInstance = getDbInstance(client);

        return dbInstance != null && dbInstance.dbInstanceStatus().equals("available");
    }

    private DBInstance getDbInstance(DocDbClient client) {
        DBInstance dbInstance = null;

        if (ObjectUtils.isBlank(getDbInstanceIdentifier())) {
            throw new GyroException("db-instance-identifier is missing, unable to load db instance.");
        }

        try {
            DescribeDbInstancesResponse response = client.describeDBInstances(
                r -> r.dbInstanceIdentifier(getDbInstanceIdentifier())
            );

            if (!response.dbInstances().isEmpty()) {
                dbInstance = response.dbInstances().get(0);
            }

        } catch (DbInstanceNotFoundException ex) {
        }

        return dbInstance;
    }
}
