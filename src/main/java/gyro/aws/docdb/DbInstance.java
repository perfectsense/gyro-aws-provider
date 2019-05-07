package gyro.aws.docdb;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import gyro.core.resource.ResourceOutput;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.CreateDbInstanceResponse;
import software.amazon.awssdk.services.docdb.model.DBInstance;
import software.amazon.awssdk.services.docdb.model.DescribeDbInstancesResponse;

import java.util.Set;

/**
 * Creates an Document db instance.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::db-instance db-instance-example
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
@ResourceName("db-instance")
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
    @ResourceDiffProperty(updatable = true)
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
    @ResourceDiffProperty(updatable = true)
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
    @ResourceDiffProperty(updatable = true)
    public String getPreferredMaintenanceWindow() {
        return preferredMaintenanceWindow;
    }

    public void setPreferredMaintenanceWindow(String preferredMaintenanceWindow) {
        this.preferredMaintenanceWindow = preferredMaintenanceWindow;
    }

    /**
     * Set the promotion tier. Valid values ``0-15``. (Required)
     */
    @ResourceDiffProperty(updatable = true)
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
    @ResourceOutput
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * The arn of the db instance.
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

        DescribeDbInstancesResponse response = client.describeDBInstances(
            r -> r.dbInstanceIdentifier(getDbInstanceIdentifier())
        );

        if (!response.dbInstances().isEmpty()) {
            DBInstance dbInstance = response.dbInstances().get(0);

            setAutoMinorVersionUpgrade(dbInstance.autoMinorVersionUpgrade());
            setAvailabilityZone(dbInstance.availabilityZone());
            setDbInstanceClass(dbInstance.dbInstanceClass());
            setEngine(dbInstance.engine());
            setPreferredMaintenanceWindow(dbInstance.preferredMaintenanceWindow());
            setPromotionTier(dbInstance.promotionTier());
            setArn(dbInstance.dbInstanceArn());
            setStatus(dbInstance.dbInstanceStatus());

            return true;
        } else {
            return false;
        }
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
                .tags(toDocDbTags(getTags()))
        );

        setArn(response.dbInstance().dbInstanceArn());
        waitForAvailability(client);
        refresh();
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

        waitForAvailability(client);
    }

    @Override
    public void delete() {
        DocDbClient client = createClient(DocDbClient.class);

        client.deleteDBInstance(
            r -> r.dbInstanceIdentifier(getDbInstanceIdentifier())
        );

        waitForDelete(client);
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

    private void waitForAvailability(DocDbClient client) {
        boolean available = false;
        int count = 0;
        while (!available && count < 6) {
            DescribeDbInstancesResponse response = waitHelper(count, client, 10000);

            available = response.dbInstances().get(0).dbInstanceStatus().equals("available");
            count++;
        }
    }

    private void waitForDelete(DocDbClient client) {
        boolean deleted = false;
        int count = 0;
        while (!deleted && count < 10) {
            DescribeDbInstancesResponse response = waitHelper(count, client, 60000);

            deleted = response.dbInstances().isEmpty();
            count++;
        }
    }

    private DescribeDbInstancesResponse waitHelper(int count, DocDbClient client, long interval) {
        if (count > 0) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return client.describeDBInstances(
            r -> r.dbInstanceIdentifier(getDbInstanceIdentifier())
        );
    }
}
