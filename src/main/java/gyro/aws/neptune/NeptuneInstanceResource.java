/*
 * Copyright 2020, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.neptune;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Range;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.neptune.NeptuneClient;
import software.amazon.awssdk.services.neptune.model.CreateDbInstanceRequest;
import software.amazon.awssdk.services.neptune.model.CreateDbInstanceResponse;
import software.amazon.awssdk.services.neptune.model.DBInstance;
import software.amazon.awssdk.services.neptune.model.DbInstanceNotFoundException;
import software.amazon.awssdk.services.neptune.model.DescribeDbInstancesResponse;
import software.amazon.awssdk.services.neptune.model.ModifyDbInstanceRequest;

/**
 * Create a Neptune instance.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *   aws::neptune-instance neptune-instance-example
 *       engine: "neptune"
 *       db-instance-class: "db.r4.large"
 *       db-instance-identifier: "neptune-instance-example"
 *       db-cluster: $(aws::neptune-cluster neptune-cluster-example)
 *
 *       db-parameter-group: $(aws::neptune-parameter-group neptune-parameter-group-cluster-example)
 *       availability-zone: "us-east-2a"
 *       auto-minor-version-upgrade: true
 *       copy-tags-to-snapshot: true
 *       license-model: "amazon-license"
 *       promotion-tier: 1
 *       apply-immediately: true
 *
 *       tags: {
 *           Name: "neptune instance example"
 *       }
 *
 *   end
 */
@Type("neptune-instance")
public class NeptuneInstanceResource extends NeptuneTaggableResource implements Copyable<DBInstance> {

    private String engine;
    private String dbInstanceClass;
    private String dbInstanceIdentifier;
    private NeptuneClusterResource dbCluster;
    private NeptuneParameterGroupResource dbParameterGroup;
    private String availabilityZone;
    private Boolean autoMinorVersionUpgrade;
    private Boolean copyTagsToSnapshot;
    private String licenseModel;
    private Integer promotionTier;
    private Boolean applyImmediately;

    /**
     * The name of the database engine. The only
     */
    @ValidStrings("neptune")
    @Required
    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    /**
     * The compute and memory capacity of the Neptune instance.
     */
    @Updatable
    @Required
    public String getDbInstanceClass() {
        return dbInstanceClass;
    }

    public void setDbInstanceClass(String dbInstanceClass) {
        this.dbInstanceClass = dbInstanceClass;
    }

    /**
     * The unique name of the Neptune instance.
     */
    @Id
    @Required
    public String getDbInstanceIdentifier() {
        return dbInstanceIdentifier;
    }

    public void setDbInstanceIdentifier(String dbInstanceIdentifier) {
        this.dbInstanceIdentifier = dbInstanceIdentifier;
    }

    /**
     * The Neptune cluster that this instance will belong to.
     */
    @Required
    public NeptuneClusterResource getDbCluster() {
        return dbCluster;
    }

    public void setDbCluster(NeptuneClusterResource dbCluster) {
        this.dbCluster = dbCluster;
    }

    /**
     * The Neptune parameter group to associate with this Neptune instance.
     * If this argument is omitted, the default parameter group for the specified engine is used.
     */
    @Updatable
    public NeptuneParameterGroupResource getDbParameterGroup() {
        return dbParameterGroup;
    }

    public void setDbParameterGroup(NeptuneParameterGroupResource dbParameterGroup) {
        this.dbParameterGroup = dbParameterGroup;
    }

    /**
     * The availability zone in which the Neptune instance is created.
     */
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * Indicates that minor engine upgrades are applied automatically to the Neptune instance during the maintenance window.
     * Defaults to ``true``.
     */
    @Updatable
    public Boolean getAutoMinorVersionUpgrade() {
        return autoMinorVersionUpgrade;
    }

    public void setAutoMinorVersionUpgrade(Boolean autoMinorVersionUpgrade) {
        this.autoMinorVersionUpgrade = autoMinorVersionUpgrade;
    }

    /**
     * If this value is ``true``, all tags from the Neptune instance will be copied to snapshots of the Neptune instance.
     * Defaults to ``false``.
     */
    @Updatable
    public Boolean getCopyTagsToSnapshot() {
        return copyTagsToSnapshot;
    }

    public void setCopyTagsToSnapshot(Boolean copyTagsToSnapshot) {
        this.copyTagsToSnapshot = copyTagsToSnapshot;
    }

    /**
     * License model information for this Neptune instance.
     * The only
     */
    @Updatable
    @ValidStrings("amazon-license")
    public String getLicenseModel() {
        return licenseModel;
    }

    public void setLicenseModel(String licenseModel) {
        this.licenseModel = licenseModel;
    }

    /**
     * Specifies the order in which a read replica is promoted to the primary instance after a failure of the existing primary instance.
     * Defaults to 1.
     */
    @Updatable
    @Range(min = 0, max = 15)
    public Integer getPromotionTier() {
        return promotionTier;
    }

    public void setPromotionTier(Integer promotionTier) {
        this.promotionTier = promotionTier;
    }

    /**
     * Specifies whether the modifications in update requests are asynchronously applied as soon as possible.
     * If this field is set to ``false``, changes to the Neptune instance are applied during the next preferred maintenance window.
     */
    @Updatable
    public Boolean getApplyImmediately() {
        return applyImmediately;
    }

    public void setApplyImmediately(Boolean applyImmediately) {
        this.applyImmediately = applyImmediately;
    }

    @Override
    public void copyFrom(DBInstance model) {
        setEngine(model.engine());
        setDbInstanceClass(model.dbInstanceClass());
        setDbInstanceIdentifier(model.dbInstanceIdentifier());
        setDbCluster(findById(NeptuneClusterResource.class, model.dbClusterIdentifier()));
        setDbParameterGroup(
            model.hasDbParameterGroups()
                ? findById(NeptuneParameterGroupResource.class, model.dbParameterGroups().get(0).dbParameterGroupName())
                : null
        );
        setAvailabilityZone(model.availabilityZone());
        setAutoMinorVersionUpgrade(model.autoMinorVersionUpgrade());
        setLicenseModel(model.licenseModel());
        setCopyTagsToSnapshot(model.copyTagsToSnapshot());
        setPromotionTier(model.promotionTier());
        setArn(model.dbInstanceArn());
    }

    @Override
    protected boolean doRefresh() {
        NeptuneClient client = createClient(NeptuneClient.class);

        DBInstance instance = getDbInstance(client);

        if (instance != null) {
            copyFrom(instance);
            return true;
        }

        return false;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        NeptuneClient client = createClient(NeptuneClient.class);

        CreateDbInstanceRequest.Builder builder = CreateDbInstanceRequest.builder()
            .engine(getEngine())
            .dbInstanceClass(getDbInstanceClass())
            .dbInstanceIdentifier(getDbInstanceIdentifier())
            .dbClusterIdentifier(getDbCluster() != null ? getDbCluster().getDbClusterIdentifier() : null)
            .availabilityZone(getAvailabilityZone())
            .autoMinorVersionUpgrade(getAutoMinorVersionUpgrade())
            .copyTagsToSnapshot(getCopyTagsToSnapshot())
            .licenseModel(getLicenseModel())
            .promotionTier(getPromotionTier());

        if (getDbParameterGroup() != null) {
            builder = builder.dbParameterGroupName(getDbParameterGroup().getName());
        }

        CreateDbInstanceResponse response = client.createDBInstance(builder.build());

        setArn(response.dbInstance().dbInstanceArn());

        Wait.atMost(20, TimeUnit.MINUTES)
            .checkEvery(1, TimeUnit.MINUTES)
            .prompt(false)
            .until(() -> isAvailable(client));
    }

    @Override
    protected void doUpdate(Resource current, Set<String> changedProperties) {
        NeptuneClient client = createClient(NeptuneClient.class);

        ModifyDbInstanceRequest.Builder builder = ModifyDbInstanceRequest.builder()
            .dbInstanceIdentifier(getDbInstanceIdentifier())
            .dbInstanceClass(getDbInstanceClass())
            .autoMinorVersionUpgrade(getAutoMinorVersionUpgrade())
            .licenseModel(getLicenseModel())
            .copyTagsToSnapshot(getCopyTagsToSnapshot())
            .promotionTier(getPromotionTier())
            .applyImmediately(getApplyImmediately());

        if (changedProperties.contains("db-parameter-group") && getDbParameterGroup() != null) {
            builder = builder.dbParameterGroupName(getDbParameterGroup().getName());
        }

        client.modifyDBInstance(builder.build());

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> isAvailable(client));
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        NeptuneClient client = createClient(NeptuneClient.class);

        client.deleteDBInstance(r -> r.dbInstanceIdentifier(getDbInstanceIdentifier()));

        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> isDeleted(client));
    }

    private DBInstance getDbInstance(NeptuneClient client) {
        DBInstance instance = null;

        try {
            DescribeDbInstancesResponse response = client.describeDBInstances(
                r -> r.dbInstanceIdentifier(getDbInstanceIdentifier())
            );

            if (response.hasDbInstances()) {
                instance = response.dbInstances().get(0);
            }

        } catch (DbInstanceNotFoundException ex) {
            // instance not found - ignore exception and return null
        }

        return instance;
    }

    private boolean isAvailable(NeptuneClient client) {
        DBInstance instance = getDbInstance(client);

        return instance != null && instance.dbInstanceStatus().equals("available");
    }

    private boolean isDeleted(NeptuneClient client) {
        DBInstance instance = getDbInstance(client);

        return instance == null;
    }
}
