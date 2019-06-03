package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Volume;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Query volume.
 *
 * .. code-block:: gyro
 *
 *    ebs-volume: $(aws::ebs-volume EXTERNAL/* | volume-id = '')
 */
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

    /**
     * The time stamp when the attachment initiated.
     */
    @Filter("attachment.attach-time")
    public String getAttachmentAttachTime() {
        return attachmentAttachTime;
    }

    public void setAttachmentAttachTime(String attachmentAttachTime) {
        this.attachmentAttachTime = attachmentAttachTime;
    }

    /**
     * Whether the volume is deleted on instance termination.
     */
    @Filter("attachment.delete-on-termination")
    public String getAttachmentDeleteOnTermination() {
        return attachmentDeleteOnTermination;
    }

    public void setAttachmentDeleteOnTermination(String attachmentDeleteOnTermination) {
        this.attachmentDeleteOnTermination = attachmentDeleteOnTermination;
    }

    /**
     * The device name specified in the block device mapping (for example, /dev/sda1).
     */
    @Filter("attachment.device")
    public String getAttachmentDevice() {
        return attachmentDevice;
    }

    public void setAttachmentDevice(String attachmentDevice) {
        this.attachmentDevice = attachmentDevice;
    }

    /**
     * The ID of the instance the volume is attached to.
     */
    @Filter("attachment.instance-id")
    public String getAttachmentInstanceId() {
        return attachmentInstanceId;
    }

    public void setAttachmentInstanceId(String attachmentInstanceId) {
        this.attachmentInstanceId = attachmentInstanceId;
    }

    /**
     * The attachment state . Valid values are ``attaching`` or ``attached`` or ``detaching``.
     */
    @Filter("attachment.status")
    public String getAttachmentStatus() {
        return attachmentStatus;
    }

    public void setAttachmentStatus(String attachmentStatus) {
        this.attachmentStatus = attachmentStatus;
    }

    /**
     * The Availability Zone in which the volume was created.
     */
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * The time stamp when the volume was created.
     */
    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    /**
     * Indicates whether the volume is encrypted . Valid values are ``true`` or ``false``
     */
    public String getEncrypted() {
        return encrypted;
    }

    public void setEncrypted(String encrypted) {
        this.encrypted = encrypted;
    }

    /**
     * The size of the volume, in GiB.
     */
    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    /**
     * The snapshot from which the volume was created.
     */
    public String getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(String snapshotId) {
        this.snapshotId = snapshotId;
    }

    /**
     * The status of the volume . Valid values are ``creating`` or ``available`` or ``in-use`` or ``deleting`` or ``deleted`` or ``error``.
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
     * The volume ID.
     */
    public String getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(String volumeId) {
        this.volumeId = volumeId;
    }

    /**
     * The Amazon EBS volume type. Valid values are ``gp2`` or ``io1`` or ``st1`` or ``sc1`` or ``standard``.
     */
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