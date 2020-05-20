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

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.neptune.NeptuneClient;
import software.amazon.awssdk.services.neptune.model.CreateDbClusterSnapshotResponse;
import software.amazon.awssdk.services.neptune.model.DBClusterSnapshot;
import software.amazon.awssdk.services.neptune.model.DbClusterSnapshotNotFoundException;
import software.amazon.awssdk.services.neptune.model.DescribeDbClusterSnapshotsResponse;

/**
 * Create a Neptune cluster snapshot.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *   aws::neptune-cluster-snapshot neptune-cluster-snapshot-example
 *       db-cluster-snapshot-identifier: "neptune-cluster-snapshot-example"
 *       db-cluster: $(aws::neptune-cluster neptune-cluster-example)
 *
 *       tags: {
 *           Name: "neptune cluster snapshot example"
 *       }
 *
 *   end
 */
@Type("neptune-cluster-snapshot")
public class NeptuneClusterSnapshotResource extends NeptuneTaggableResource implements Copyable<DBClusterSnapshot> {

    private String dbClusterSnapshotIdentifier;
    private NeptuneClusterResource dbCluster;
    private Integer allocatedStorage;
    private List<String> availabilityZones;
    private String clusterCreateTime;
    private String engine;
    private String engineVersion;
    private Boolean iamDatabaseAuthenticationEnabled;
    private String kmsKeyId;
    private String licenseModel;
    private String masterUsername;
    private Integer percentProgress;
    private Integer port;
    private String snapshotCreateTime;
    private String snapshotType;
    private String sourceDBClusterSnapshotArn;
    private String status;
    private Boolean storageEncrypted;
    private String vpcId;

    /**
     * The unique name of the Neptune cluster snapshot. (Required)
     */
    @Id
    @Required
    @Regex(value = "^[a-zA-Z]((?!.*--)[-a-zA-Z0-9]{0,253}[a-z0-9]$)?", message = "1-255 letters, numbers, or hyphens. May not contain two consecutive hyphens. The first character must be a letter, and the last may not be a hyphen.")
    public String getDbClusterSnapshotIdentifier() {
        return dbClusterSnapshotIdentifier;
    }

    public void setDbClusterSnapshotIdentifier(String dbClusterSnapshotIdentifier) {
        this.dbClusterSnapshotIdentifier = dbClusterSnapshotIdentifier;
    }

    /**
     * The Neptune cluster to create a snapshot for. (Required)
     */
    @Required
    public NeptuneClusterResource getDbCluster() {
        return dbCluster;
    }

    public void setDbCluster(NeptuneClusterResource dbCluster) {
        this.dbCluster = dbCluster;
    }

    /**
     * The allocated storage size in gibibytes.
     */
    @Output
    public Integer getAllocatedStorage() {
        return allocatedStorage;
    }

    public void setAllocatedStorage(Integer allocatedStorage) {
        this.allocatedStorage = allocatedStorage;
    }

    /**
     * The list of EC2 Availability Zones that instances in the Neptune cluster snapshot can be restored in.
     */
    @Output
    public List<String> getAvailabilityZones() {
        return availabilityZones;
    }

    public void setAvailabilityZones(List<String> availabilityZones) {
        this.availabilityZones = availabilityZones;
    }

    /**
     * The time when the Neptune cluster was created, in Universal Coordinated Time (UTC).
     */
    @Output
    public String getClusterCreateTime() {
        return clusterCreateTime;
    }

    public void setClusterCreateTime(String clusterCreateTime) {
        this.clusterCreateTime = clusterCreateTime;
    }

    /**
     * The name of the database engine.
     */
    @Output
    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    /**
     * The version of the database engine for this Neptune cluster snapshot.
     */
    @Output
    public String getEngineVersion() {
        return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
        this.engineVersion = engineVersion;
    }

    /**
     * True if mapping of AWS Identity and Access Management (IAM) accounts to database accounts is enabled, and otherwise false.
     */
    @Output
    public Boolean getIamDatabaseAuthenticationEnabled() {
        return iamDatabaseAuthenticationEnabled;
    }

    public void setIamDatabaseAuthenticationEnabled(Boolean iamDatabaseAuthenticationEnabled) {
        this.iamDatabaseAuthenticationEnabled = iamDatabaseAuthenticationEnabled;
    }

    /**
     * If StorageEncrypted is true, the AWS KMS key identifier for the encrypted Neptune cluster snapshot.
     */
    @Output
    public String getKmsKeyId() {
        return kmsKeyId;
    }

    public void setKmsKeyId(String kmsKeyId) {
        this.kmsKeyId = kmsKeyId;
    }

    /**
     * The license model information for this Neptune cluster snapshot.
     */
    @Output
    public String getLicenseModel() {
        return licenseModel;
    }

    public void setLicenseModel(String licenseModel) {
        this.licenseModel = licenseModel;
    }

    /**
     * The master username for the Neptune cluster snapshot.
     */
    @Output
    public String getMasterUsername() {
        return masterUsername;
    }

    public void setMasterUsername(String masterUsername) {
        this.masterUsername = masterUsername;
    }

    /**
     * The percentage of the estimated data that has been transferred.
     */
    @Output
    public Integer getPercentProgress() {
        return percentProgress;
    }

    public void setPercentProgress(Integer percentProgress) {
        this.percentProgress = percentProgress;
    }

    /**
     * The port that the DB cluster was listening on at the time of the snapshot.
     */
    @Output
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * The time when the snapshot was taken, in Universal Coordinated Time (UTC).
     */
    @Output
    public String getSnapshotCreateTime() {
        return snapshotCreateTime;
    }

    public void setSnapshotCreateTime(String snapshotCreateTime) {
        this.snapshotCreateTime = snapshotCreateTime;
    }

    /**
     * The type of the Neptune cluster snapshot. Valid values are ``automated``, ``manual``, ``shared``, and ``public``.
     */
    @Output
    public String getSnapshotType() {
        return snapshotType;
    }

    public void setSnapshotType(String snapshotType) {
        this.snapshotType = snapshotType;
    }

    /**
     * If the Neptune cluster snapshot was copied from a source Neptune cluster snapshot, the Amazon Resource Name (ARN) for the source Neptune cluster snapshot, otherwise, a null value.
     */
    @Output
    public String getSourceDBClusterSnapshotArn() {
        return sourceDBClusterSnapshotArn;
    }

    public void setSourceDBClusterSnapshotArn(String sourceDBClusterSnapshotArn) {
        this.sourceDBClusterSnapshotArn = sourceDBClusterSnapshotArn;
    }

    /**
     * The status of this Neptune cluster snapshot.
     */
    @Output
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Specifies whether the Neptune cluster snapshot is encrypted.
     */
    @Output
    public Boolean getStorageEncrypted() {
        return storageEncrypted;
    }

    public void setStorageEncrypted(Boolean storageEncrypted) {
        this.storageEncrypted = storageEncrypted;
    }

    /**
     * The VPC ID associated with the Neptune cluster snapshot.
     */
    @Output
    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    @Override
    public void copyFrom(DBClusterSnapshot model) {
        setDbClusterSnapshotIdentifier(model.dbClusterSnapshotIdentifier());
        setDbCluster(findById(NeptuneClusterResource.class, model.dbClusterIdentifier()));
        setAllocatedStorage(model.allocatedStorage());
        setAvailabilityZones(model.availabilityZones());
        setClusterCreateTime(model.clusterCreateTime().toString());
        setEngine(model.engine());
        setEngineVersion(model.engineVersion());
        setIamDatabaseAuthenticationEnabled(model.iamDatabaseAuthenticationEnabled());
        setKmsKeyId(model.kmsKeyId());
        setLicenseModel(model.licenseModel());
        setMasterUsername(model.masterUsername());
        setPercentProgress(model.percentProgress());
        setPort(model.port());
        setSnapshotCreateTime(model.snapshotCreateTime().toString());
        setSnapshotType(model.snapshotType());
        setSourceDBClusterSnapshotArn(model.sourceDBClusterSnapshotArn());
        setStatus(model.status());
        setStorageEncrypted(model.storageEncrypted());
        setVpcId(model.vpcId());
        setArn(model.dbClusterSnapshotArn());
    }

    @Override
    protected boolean doRefresh() {
        NeptuneClient client = createClient(NeptuneClient.class);

        DBClusterSnapshot snapshot = getDbClusterSnapshot(client);

        if (snapshot != null) {
            copyFrom(snapshot);
            return true;
        }

        return false;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        NeptuneClient client = createClient(NeptuneClient.class);

        CreateDbClusterSnapshotResponse response = client.createDBClusterSnapshot(
            r -> r.dbClusterSnapshotIdentifier(getDbClusterSnapshotIdentifier())
                .dbClusterIdentifier(getDbCluster().getDbClusterIdentifier())
        );

        setArn(response.dbClusterSnapshot().dbClusterSnapshotArn());

        Wait.atMost(5, TimeUnit.MINUTES)
            .checkEvery(30, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> isAvailable(client));
    }

    @Override
    protected void doUpdate(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        NeptuneClient client = createClient(NeptuneClient.class);

        client.deleteDBClusterSnapshot(r -> r.dbClusterSnapshotIdentifier(getDbClusterSnapshotIdentifier()));

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> getDbClusterSnapshot(client) == null);
    }

    private DBClusterSnapshot getDbClusterSnapshot(NeptuneClient client) {
        DBClusterSnapshot snapshot = null;

        try {
            DescribeDbClusterSnapshotsResponse response = client.describeDBClusterSnapshots(
                r -> r.dbClusterSnapshotIdentifier(getDbClusterSnapshotIdentifier())
            );

            if (response.hasDbClusterSnapshots()) {
                snapshot = response.dbClusterSnapshots().get(0);
            }
        } catch (DbClusterSnapshotNotFoundException ex) {
            // snapshot not found - ignore exception and return null
        }

        return snapshot;
    }

    private boolean isAvailable(NeptuneClient client) {
        DBClusterSnapshot snapshot = getDbClusterSnapshot(client);

        return snapshot != null && snapshot.status().equals("available");
    }
}
