package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Snapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query ebs snapshot.
 *
 * .. code-block:: gyro
 *
 *    ebs-snapshot: $(aws::ebs-snapshot EXTERNAL/* | owner-alias = 'amazon')
 */
@Type("ebs-snapshot")
public class EbsSnapshotFinder extends AwsFinder<Ec2Client, Snapshot, EbsSnapshotResource> {

    private String description;
    private String encrypted;
    private String ownerAlias;
    private String ownerId;
    private String progress;
    private String snapshotId;
    private String startTime;
    private String status;
    private Map<String, String> tag;
    private String tagKey;
    private String volumeId;
    private String volumeSize;

    /**
     * A description of the snapshot.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Indicates whether the snapshot is encrypted . Valid values are ``true`` or ``false``
     */
    public String getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(String encrypted) {
        this.encrypted = encrypted;
    }

    /**
     * Value from an Amazon-maintained list . Valid values are ``amazon`` or ``self`` or ``all`` or ``aws-marketplace`` or ``microsoft``.
     */
    public String getOwnerAlias() {
        return ownerAlias;
    }

    public void setOwnerAlias(String ownerAlias) {
        this.ownerAlias = ownerAlias;
    }

    /**
     * The ID of the AWS account that owns the snapshot.
     */
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * The progress of the snapshot, as a percentage (for example, 80%).
     */
    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    /**
     * The snapshot ID.
     */
    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    /**
     * The time stamp when the snapshot was initiated.
     */
    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     * The status of the snapshot . Valid values are ``pending`` or ``completed`` or ``error``.
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * The key/value combination of a tag assigned to the resource.
     */
    public Map<String, String> getTag() {
        if (tag == null) {
            tag = new HashMap<>();
        }

        return tag;
    }

    public void setTag(Map<String, String> tag) {
        this.tag = tag;
    }

    /**
     * The key of a tag assigned to the resource. Use this filter to find all resources assigned a tag with a specific key, regardless of the tag value.
     */
    public String getTagKey() {
        return tagKey;
    }

    public void setTagKey(String tagKey) {
        this.tagKey = tagKey;
    }

    /**
     * The ID of the volume the snapshot is for.
     */
    public String getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
    }

    /**
     * The size of the volume, in GiB.
     */
    public String getVolumeSize() {
        return volumeSize;
    }

    public void setVolumeSize(String volumeSize) {
        this.volumeSize = volumeSize;
    }

    @Override
    protected List<Snapshot> findAllAws(Ec2Client client) {
        return client.describeSnapshotsPaginator().snapshots().stream().collect(Collectors.toList());
    }

    @Override
    protected List<Snapshot> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeSnapshotsPaginator(r -> r.filters(createFilters(filters))).snapshots().stream().collect(Collectors.toList());
    }
}
