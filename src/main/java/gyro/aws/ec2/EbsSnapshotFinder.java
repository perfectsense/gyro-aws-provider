package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Snapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(String encrypted) {
        this.encrypted = encrypted;
    }

    public String getOwnerAlias() {
        return ownerAlias;
    }

    public void setOwnerAlias(String ownerAlias) {
        this.ownerAlias = ownerAlias;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, String> getTag() {
        if (tag == null) {
            tag = new HashMap<>();
        }

        return tag;
    }

    public void setTag(Map<String, String> tag) {
        this.tag = tag;
    }

    public String getTagKey() {
        return tagKey;
    }

    public void setTagKey(String tagKey) {
        this.tagKey = tagKey;
    }

    public String getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
    }

    public String getVolumeSize() {
        return volumeSize;
    }

    public void setVolumeSize(String volumeSize) {
        this.volumeSize = volumeSize;
    }

    @Override
    protected List<Snapshot> findAllAws(Ec2Client client) {
        return client.describeSnapshots().snapshots();
    }

    @Override
    protected List<Snapshot> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeSnapshots(r -> r.filters(createFilters(filters))).snapshots();
    }
}

