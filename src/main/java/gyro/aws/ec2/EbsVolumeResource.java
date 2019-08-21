package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.kms.KmsKeyResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.scope.State;
import org.apache.commons.lang.NotImplementedException;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateVolumeResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVolumeAttributeResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVolumesResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Volume;
import software.amazon.awssdk.services.ec2.model.VolumeAttributeName;
import software.amazon.awssdk.services.ec2.model.VolumeState;

import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Creates a EBS Volume.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::ebs-volume ebs-volume-example
 *         availability-zone: "us-east-2a"
 *         size: 100
 *         auto-enable-io: false
 *         tags: {
 *             Name: "ebs-volume-example"
 *         }
 *     end
 */
@Type("ebs-volume")
public class EbsVolumeResource extends Ec2TaggableResource<Volume> implements Copyable<Volume> {

    private String availabilityZone;
    private Date createTime;
    private Boolean encrypted;
    private Integer iops;
    private KmsKeyResource kms;
    private Integer size;
    private EbsSnapshotResource snapshot;
    private String state;
    private String id;
    private String volumeType;
    private Boolean autoEnableIo;

    /**
     * The availability zone for the volume being created. (Required)
     */
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * Should the volume be encrypted. Defaults to false.
     */
    public Boolean getEncrypted() {
        if (encrypted == null) {
            encrypted = false;
        }

        return encrypted;
    }

    public void setEncrypted(Boolean encrypted) {
        this.encrypted = encrypted;
    }

    /**
     * The number of I/O operations per second (IOPS) to provision for the volume. Only allowed when 'volume-type' set to ``io1``.
     */
    @Updatable
    public Integer getIops() {
        return iops;
    }

    public void setIops(Integer iops) {
        this.iops = iops;
    }

    /**
     * The kms, when using encrypted volume.
     */
    public KmsKeyResource getKms() {
        return kms;
    }

    public void setKms(KmsKeyResource kms) {
        this.kms = kms;
    }

    /**
     * The size of the volume in GiBs.
     */
    @Updatable
    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * The snapshot from which to create the volume. Required if size is not mentioned.
     */
    public EbsSnapshotResource getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(EbsSnapshotResource snapshot) {
        this.snapshot = snapshot;
    }

    /**
     * The state of the volume.
     */
    @Output
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * The id of the volume.
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
     * The type of volume being created. Defaults to 'gp2'. Valid options are ``gp2`` or ``io1`` or ``st1`` or ``sc1`` or ``standard``].
     */
    @Updatable
    public String getVolumeType() {
        if (volumeType == null) {
            volumeType = "gp2";
        }

        return volumeType;
    }

    public void setVolumeType(String volumeType) {
        this.volumeType = volumeType;
    }

    /**
     * Auto Enable IO. Defaults to false.
     */
    @Updatable
    public Boolean getAutoEnableIo() {
        if (autoEnableIo == null) {
            autoEnableIo = false;
        }

        return autoEnableIo;
    }

    public void setAutoEnableIo(Boolean autoEnableIo) {
        this.autoEnableIo = autoEnableIo;
    }

    @Override
    public void copyFrom(Volume volume) {
        setId(volume.volumeId());
        setAvailabilityZone(volume.availabilityZone());
        setCreateTime(Date.from(volume.createTime()));
        setEncrypted(volume.encrypted());
        setIops(volume.iops());
        setKms(!ObjectUtils.isBlank(volume.kmsKeyId()) ? findById(KmsKeyResource.class, volume.kmsKeyId()) : null);
        setSize(volume.size());
        setSnapshot(!ObjectUtils.isBlank(volume.snapshotId()) ? findById(EbsSnapshotResource.class, volume.snapshotId()) : null);
        setState(volume.stateAsString());
        setVolumeType(volume.volumeTypeAsString());

        Ec2Client client = createClient(Ec2Client.class);

        DescribeVolumeAttributeResponse responseAutoEnableIo = client.describeVolumeAttribute(
            r -> r.volumeId(getId())
                .attribute(VolumeAttributeName.AUTO_ENABLE_IO)
        );

        setAutoEnableIo(responseAutoEnableIo.autoEnableIO().value());
    }

    @Override
    protected boolean doRefresh() {
        throw new NotImplementedException();
    }

    @Override
    protected String getResourceId() {
        return getId();
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        throw new NotImplementedException();
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {
        throw new NotImplementedException();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        throw new NotImplementedException();
    }

    private Volume getVolume(Ec2Client client) {
        Volume volume = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load volume.");
        }

        try {
            DescribeVolumesResponse response = client.describeVolumes(
                r -> r.volumeIds(Collections.singleton(getId()))
            );

            if (!response.volumes().isEmpty()) {
                volume = response.volumes().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return volume;
    }

    private void validate(boolean isCreate) {
        if (!getVolumeType().equals("io1") && isCreate && getIops() != null) {
            throw new GyroException("The param 'iops' can only be set when param 'volume-type' is set to 'io1'.");
        }
    }

    private boolean isAvailable(Ec2Client client) {
        Volume volume = getEc2Volume(client);

        return volume != null && volume.state().equals(VolumeState.AVAILABLE);
    }

    private Volume getEc2Volume(Ec2Client client) {
        Volume volume = null;

        try {
            DescribeVolumesResponse response = client.describeVolumes(r -> r.volumeIds(Collections.singletonList(getId())));

            if (!response.volumes().isEmpty()) {
                volume = response.volumes().get(0);
            }
        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return volume;
    }
}
