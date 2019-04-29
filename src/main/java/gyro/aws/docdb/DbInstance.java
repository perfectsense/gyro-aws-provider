package gyro.aws.docdb;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.CreateDbInstanceResponse;
import software.amazon.awssdk.services.docdb.model.DBInstance;
import software.amazon.awssdk.services.docdb.model.DescribeDbInstancesResponse;

import java.util.Set;

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

    @ResourceDiffProperty(updatable = true)
    public Boolean getAutoMinorVersionUpgrade() {
        return autoMinorVersionUpgrade;
    }

    public void setAutoMinorVersionUpgrade(Boolean autoMinorVersionUpgrade) {
        this.autoMinorVersionUpgrade = autoMinorVersionUpgrade;
    }

    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    @ResourceDiffProperty(updatable = true)
    public String getDbInstanceClass() {
        return dbInstanceClass;
    }

    public void setDbInstanceClass(String dbInstanceClass) {
        this.dbInstanceClass = dbInstanceClass;
    }

    public String getDbInstanceIdentifier() {
        return dbInstanceIdentifier;
    }

    public void setDbInstanceIdentifier(String dbInstanceIdentifier) {
        this.dbInstanceIdentifier = dbInstanceIdentifier;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    @ResourceDiffProperty(updatable = true)
    public String getPreferredMaintenanceWindow() {
        return preferredMaintenanceWindow;
    }

    public void setPreferredMaintenanceWindow(String preferredMaintenanceWindow) {
        this.preferredMaintenanceWindow = preferredMaintenanceWindow;
    }

    @ResourceDiffProperty(updatable = true)
    public Integer getPromotionTier() {
        return promotionTier;
    }

    public void setPromotionTier(Integer promotionTier) {
        this.promotionTier = promotionTier;
    }

    public String getDbClusterIdentifier() {
        return dbClusterIdentifier;
    }

    public void setDbClusterIdentifier(String dbClusterIdentifier) {
        this.dbClusterIdentifier = dbClusterIdentifier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
