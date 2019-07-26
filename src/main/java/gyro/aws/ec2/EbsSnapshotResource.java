package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.diff.Context;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateSnapshotResponse;
import software.amazon.awssdk.services.ec2.model.DescribeSnapshotsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Snapshot;

import java.util.Date;
import java.util.Set;

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
    private String snapshotId;

    private String dataEncryptionKeyId;
    private Boolean encrypted;
    private String kmsKeyId;
    private String ownerAlias;
    private String ownerId;
    private String progress;
    private Date startTime;
    private String state;
    private String stateMessage;
    private Integer volumeSize;

    /**
     * The volume id based on which the snapshot would be created. (Required)
     */
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
    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    /**
     * The data encryption key of the snapshot.
     */
    @Output
    public String getDataEncryptionKeyId() {
        return dataEncryptionKeyId;
    }

    public void setDataEncryptionKeyId(String dataEncryptionKeyId) {
        this.dataEncryptionKeyId = dataEncryptionKeyId;
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
     * The kms key id of the snapshot.
     */
    @Output
    public String getKmsKeyId() {
        return kmsKeyId;
    }

    public void setKmsKeyId(String kmsKeyId) {
        this.kmsKeyId = kmsKeyId;
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
    public String getState() {
        return state;
    }

    public void setState(String state) {
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
        setSnapshotId(snapshot.snapshotId());
        setDataEncryptionKeyId(snapshot.dataEncryptionKeyId());
        setDescription(snapshot.description());
        setEncrypted(snapshot.encrypted());
        setKmsKeyId(snapshot.kmsKeyId());
        setOwnerAlias(snapshot.ownerAlias());
        setOwnerId(snapshot.ownerId());
        setProgress(snapshot.progress());
        setStartTime(Date.from(snapshot.startTime()));
        setState(snapshot.stateAsString());
        setStateMessage(snapshot.stateMessage());
        setVolumeSize(snapshot.volumeSize());
        setVolume(!ObjectUtils.isBlank(snapshot.volumeId()) ? findById(EbsVolumeResource.class, snapshot.volumeId()) : null);
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
    protected String getId() {
        return getSnapshotId();
    }

    @Override
    protected void doCreate(GyroUI ui, Context context) {
        Ec2Client client = createClient(Ec2Client.class);

        CreateSnapshotResponse response = client.createSnapshot(
            r -> r.description(getDescription())
                .volumeId(getVolume().getVolumeId())
        );

        setSnapshotId(response.snapshotId());
    }

    @Override
    protected void doUpdate(GyroUI ui, Context context, AwsResource config, Set<String> changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, Context context) {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteSnapshot(
            r -> r.snapshotId(getSnapshotId())
        );
    }

    private Snapshot getSnapshot(Ec2Client client) {
        Snapshot snapshot = null;

        if (ObjectUtils.isBlank(getSnapshotId())) {
            throw new GyroException("snapshot-id is missing, unable to load snapshot.");
        }

        try {
            DescribeSnapshotsResponse response = client.describeSnapshots(
                r -> r.snapshotIds(getSnapshotId())
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
