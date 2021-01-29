/*
 * Copyright 2019, Perfect Sense, Inc.
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

package gyro.aws.ec2;

import java.util.Date;
import java.util.Set;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.kms.KmsKeyResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateSnapshotResponse;
import software.amazon.awssdk.services.ec2.model.DescribeSnapshotsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Snapshot;
import software.amazon.awssdk.services.ec2.model.SnapshotState;

/**
 * Creates a EBS Snapshot based on the specified EBS volume.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::ebs-snapshot ebs-snapshot-example
 *         description: "ebs-snapshot-example"
 *         volume: $(aws::ebs-volume ebs-volume-example)
 *         tags: {
 *             Name: 'ebs-snapshot-example'
 *         }
 *     end
 */
@Type("ebs-snapshot")
public class EbsSnapshotResource extends Ec2TaggableResource<Snapshot> implements Copyable<Snapshot> {

    private EbsVolumeResource volume;
    private String description;

    // Read-only
    private String id;
    private String dataEncryptionKey;
    private Boolean encrypted;
    private KmsKeyResource kmsKey;
    private String ownerAlias;
    private String ownerId;
    private String progress;
    private Date startTime;
    private SnapshotState state;
    private String stateMessage;
    private Integer volumeSize;

    /**
     * The volume id based on which the snapshot would be created.
     */
    @Required
    public EbsVolumeResource getVolume() {
        return volume;
    }

    public void setVolume(EbsVolumeResource volume) {
        this.volume = volume;
    }

    /**
     * The description for the snapshot.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The id of the snapshot.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The data encryption key of the snapshot.
     */
    @Output
    public String getDataEncryptionKey() {
        return dataEncryptionKey;
    }

    public void setDataEncryptionKey(String dataEncryptionKey) {
        this.dataEncryptionKey = dataEncryptionKey;
    }

    /**
     * The encryption status of the volume created by the snapshot.
     */
    @Output
    public Boolean getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(Boolean encrypted) {
        this.encrypted = encrypted;
    }

    /**
     * The kms key used to protect the volume encryption key for the parent volume..
     */
    @Output
    public KmsKeyResource getKmsKey() {
        return kmsKey;
    }

    public void setKmsKey(KmsKeyResource kmsKey) {
        this.kmsKey = kmsKey;
    }

    /**
     * The owner alias of the snapshot.
     */
    @Output
    public String getOwnerAlias() {
        return ownerAlias;
    }

    public void setOwnerAlias(String ownerAlias) {
        this.ownerAlias = ownerAlias;
    }

    /**
     * The owner id of the snapshot.
     */
    @Output
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * The progress status of the snapshot.
     */
    @Output
    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    /**
     * The start time of the snapshot.
     */
    @Output
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * The state of the snapshot.
     */
    @Output
    public SnapshotState getState() {
        return state;
    }

    public void setState(SnapshotState state) {
        this.state = state;
    }

    /**
     * The state message of the snapshot.
     */
    @Output
    public String getStateMessage() {
        return stateMessage;
    }

    public void setStateMessage(String stateMessage) {
        this.stateMessage = stateMessage;
    }

    /**
     * The size of the volume created by the snapshot.
     */
    @Output
    public Integer getVolumeSize() {
        return volumeSize;
    }

    public void setVolumeSize(Integer volumeSize) {
        this.volumeSize = volumeSize;
    }

    @Override
    public void copyFrom(Snapshot snapshot) {
        setId(snapshot.snapshotId());
        setDataEncryptionKey(snapshot.dataEncryptionKeyId());
        setDescription(snapshot.description());
        setEncrypted(snapshot.encrypted());
        setKmsKey(findById(KmsKeyResource.class, snapshot.kmsKeyId()));
        setOwnerAlias(snapshot.ownerAlias());
        setOwnerId(snapshot.ownerId());
        setProgress(snapshot.progress());
        setStartTime(Date.from(snapshot.startTime()));
        setState(snapshot.state());
        setStateMessage(snapshot.stateMessage());
        setVolumeSize(snapshot.volumeSize());
        setVolume(!ObjectUtils.isBlank(snapshot.volumeId())
            ? findById(EbsVolumeResource.class, snapshot.volumeId()) : null);

        refreshTags();
    }

    @Override
    protected boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        Snapshot snapshot = getSnapshot(client);

        if (snapshot == null) {
            return false;
        }

        copyFrom(snapshot);

        return true;
    }

    @Override
    protected String getResourceId() {
        return getId();
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        CreateSnapshotResponse response = client.createSnapshot(
            r -> r.description(getDescription())
                .volumeId(getVolume().getId())
        );

        setId(response.snapshotId());
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteSnapshot(
            r -> r.snapshotId(getId())
        );
    }

    private Snapshot getSnapshot(Ec2Client client) {
        Snapshot snapshot = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load snapshot.");
        }

        try {
            DescribeSnapshotsResponse response = client.describeSnapshots(
                r -> r.snapshotIds(getId())
            );

            if (!response.snapshots().isEmpty()) {
                snapshot = response.snapshots().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return snapshot;
    }
}
