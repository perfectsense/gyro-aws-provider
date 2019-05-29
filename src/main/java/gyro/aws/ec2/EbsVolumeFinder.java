package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Volume;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Type("ebs-volume")
public class EbsVolumeFinder extends AwsFinder<Ec2Client, Volume, EbsVolumeResource> {

    private String attachmentAttachTime;
    private String attachmentDeleteOnTermination;
    private String attachmentDevice;
    private String attachmentInstanceId;
    private String attachmentStatus;
    private String availabilityZone;
    private String createTime;
    private String encrypted;
    private String size;
    private String snapshotId;
    private String status;
    private Map<String, String> tag;
    private String tagKey;
    private String volumeId;
    private String volumeType;

    @Filter("attachment.attach-time")
    public String getAttachmentAttachTime() {
        return attachmentAttachTime;
    }

    public void setAttachmentAttachTime(String attachmentAttachTime) {
        this.attachmentAttachTime = attachmentAttachTime;
    }

    @Filter("attachment.delete-on-termination")
    public String getAttachmentDeleteOnTermination() {
        return attachmentDeleteOnTermination;
    }

    public void setAttachmentDeleteOnTermination(String attachmentDeleteOnTermination) {
        this.attachmentDeleteOnTermination = attachmentDeleteOnTermination;
    }

    @Filter("attachment.device")
    public String getAttachmentDevice() {
        return attachmentDevice;
    }

    public void setAttachmentDevice(String attachmentDevice) {
        this.attachmentDevice = attachmentDevice;
    }

    @Filter("attachment.instance-id")
    public String getAttachmentInstanceId() {
        return attachmentInstanceId;
    }

    public void setAttachmentInstanceId(String attachmentInstanceId) {
        this.attachmentInstanceId = attachmentInstanceId;
    }

    @Filter("attachment.status")
    public String getAttachmentStatus() {
        return attachmentStatus;
    }

    public void setAttachmentStatus(String attachmentStatus) {
        this.attachmentStatus = attachmentStatus;
    }

    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(String encrypted) {
        this.encrypted = encrypted;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
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

    public String getVolumeType() {
        return volumeType;
    }

    public void setVolumeType(String volumeType) {
        this.volumeType = volumeType;
    }

    @Override
    protected List<Volume> findAllAws(Ec2Client client) {
        return client.describeVolumes().volumes();
    }

    @Override
    protected List<Volume> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeVolumes(r -> r.filters(createFilters(filters))).volumes();
    }
}
