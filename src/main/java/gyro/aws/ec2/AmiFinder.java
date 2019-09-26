package gyro.aws.ec2;

import gyro.core.Type;
import gyro.core.finder.Filter;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Image;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Query instance.
 *
 * .. code-block:: gyro
 *
 *    ami: $(external-query aws::ami { name: 'ami-example'})
 */
@Type("ami")
public class AmiFinder extends Ec2TaggableAwsFinder<Ec2Client, Image, AmiResource> {
    private String architecture;
    private String blockDeviceMappingDeleteOnTermination;
    private String blockDeviceMappingDeviceName;
    private String blockDeviceMappingSnapshotId;
    private String blockDeviceMappingVolumeSize;
    private String blockDeviceMappingVolumeType;
    private String blockDeviceMappingEncrypted;
    private String description;
    private String enaSupport;
    private String hypervisor;
    private String imageId;
    private String imageType;
    private String isPublic;
    private String kernelId;
    private String manifestLocation;
    private String name;
    private String ownerAlias;
    private String ownerId;
    private String platform;
    private String productCode;
    private String productCodeType;
    private String ramdiskId;
    private String rootDeviceName;
    private String rootDeviceType;
    private String state;
    private String stateReasonCode;
    private String stateReasonMessage;
    private String sriovNetSupport;
    private Map<String, String> tag;
    private String tagKey;
    private String virtualizationType;

    /**
     * The Image architecture. Valid values are ``i386`` or ``x86_64`` or ``arm64``.
     */
    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    /**
     * A Boolean value that indicates whether the Amazon EBS volume is deleted on instance termination.
     */
    @Filter("block-device-mapping.delete-on-termination")
    public String getBlockDeviceMappingDeleteOnTermination() {
        return blockDeviceMappingDeleteOnTermination;
    }

    public void setBlockDeviceMappingDeleteOnTermination(String blockDeviceMappingDeleteOnTermination) {
        this.blockDeviceMappingDeleteOnTermination = blockDeviceMappingDeleteOnTermination;
    }

    /**
     * The device name specified in the block device mapping.
     */
    @Filter("block-device-mapping.device-name")
    public String getBlockDeviceMappingDeviceName() {
        return blockDeviceMappingDeviceName;
    }

    public void setBlockDeviceMappingDeviceName(String blockDeviceMappingDeviceName) {
        this.blockDeviceMappingDeviceName = blockDeviceMappingDeviceName;
    }

    /**
     * The ID of the snapshot used for the EBS volume.
     */
    @Filter("block-device-mapping.snapshot-id")
    public String getBlockDeviceMappingSnapshotId() {
        return blockDeviceMappingSnapshotId;
    }

    public void setBlockDeviceMappingSnapshotId(String blockDeviceMappingSnapshotId) {
        this.blockDeviceMappingSnapshotId = blockDeviceMappingSnapshotId;
    }

    /**
     * The volume size of the EBS volume, in GiB.
     */
    @Filter("block-device-mapping.volume-size")
    public String getBlockDeviceMappingVolumeSize() {
        return blockDeviceMappingVolumeSize;
    }

    public void setBlockDeviceMappingVolumeSize(String blockDeviceMappingVolumeSize) {
        this.blockDeviceMappingVolumeSize = blockDeviceMappingVolumeSize;
    }

    /**
     * The volume type of the EBS volume. Valid values are ``gp2`` or ``io1`` or ``st1`` or ``sc1`` or ``standard``.
     */
    @Filter("block-device-mapping.volume-type")
    public String getBlockDeviceMappingVolumeType() {
        return blockDeviceMappingVolumeType;
    }

    public void setBlockDeviceMappingVolumeType(String blockDeviceMappingVolumeType) {
        this.blockDeviceMappingVolumeType = blockDeviceMappingVolumeType;
    }

    /**
     * A Boolean that indicates whether the EBS volume is encrypted.
     */
    @Filter("block-device-mapping.encrypted")
    public String getBlockDeviceMappingEncrypted() {
        return blockDeviceMappingEncrypted;
    }

    public void setBlockDeviceMappingEncrypted(String blockDeviceMappingEncrypted) {
        this.blockDeviceMappingEncrypted = blockDeviceMappingEncrypted;
    }

    /**
     * The description of the Image.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * A Boolean that indicates whether enhanced networking with ENA is enabled.
     */
    public String getEnaSupport() {
        return enaSupport;
    }

    public void setEnaSupport(String enaSupport) {
        this.enaSupport = enaSupport;
    }

    /**
     * The hypervisor type. Valid values are ``ovm`` or ``xen``.
     */
    public String getHypervisor() {
        return hypervisor;
    }

    public void setHypervisor(String hypervisor) {
        this.hypervisor = hypervisor;
    }

    /**
     * The ID of the Image.
     */
    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    /**
     * The Image type. Valid values are ``machine`` or ``kernel`` or ``ramdisk``.
     */
    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    /**
     * A Boolean that indicates whether the Image is public.
     */
    public String getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(String isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * The kernel ID.
     */
    public String getKernelId() {
        return kernelId;
    }

    public void setKernelId(String kernelId) {
        this.kernelId = kernelId;
    }

    /**
     * The location of the Image manifest.
     */
    public String getManifestLocation() {
        return manifestLocation;
    }

    public void setManifestLocation(String manifestLocation) {
        this.manifestLocation = manifestLocation;
    }

    /**
     * The name of the AMI.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * String value from an Amazon-maintained list. Valid values are ``amazon`` or ``aws-marketplace`` or ``microsoft``.
     */
    public String getOwnerAlias() {
        return ownerAlias;
    }

    public void setOwnerAlias(String ownerAlias) {
        this.ownerAlias = ownerAlias;
    }

    /**
     * The AWS account ID of the Image owner.
     */
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * The platform. To only list Windows-based AMIs, use ``windows``.
     */
    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    /**
     * The product code.
     */
    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    /**
     * The type of the product code. Valid values are ``devpay`` or ``marketplace``.
     */
    @Filter("product-code.type")
    public String getProductCodeType() {
        return productCodeType;
    }

    public void setProductCodeType(String productCodeType) {
        this.productCodeType = productCodeType;
    }

    /**
     * The RAM disk ID.
     */
    public String getRamdiskId() {
        return ramdiskId;
    }

    public void setRamdiskId(String ramdiskId) {
        this.ramdiskId = ramdiskId;
    }

    /**
     * The device name of the root device volume.
     */
    public String getRootDeviceName() {
        return rootDeviceName;
    }

    public void setRootDeviceName(String rootDeviceName) {
        this.rootDeviceName = rootDeviceName;
    }

    /**
     * The type of the root device volume. Valid values are ``ebs`` or ``instance-store``.
     */
    public String getRootDeviceType() {
        return rootDeviceType;
    }

    public void setRootDeviceType(String rootDeviceType) {
        this.rootDeviceType = rootDeviceType;
    }

    /**
     * The state of the Image. Valid values are ``available`` or ``pending`` or ``failed``.
     */
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * The reason code for the state change.
     */
    public String getStateReasonCode() {
        return stateReasonCode;
    }

    public void setStateReasonCode(String stateReasonCode) {
        this.stateReasonCode = stateReasonCode;
    }

    /**
     * The message for the state change.
     */
    public String getStateReasonMessage() {
        return stateReasonMessage;
    }

    public void setStateReasonMessage(String stateReasonMessage) {
        this.stateReasonMessage = stateReasonMessage;
    }

    /**
     * A value of simple indicates that enhanced networking with the Intel 82599 VF interface is enabled.
     */
    public String getSriovNetSupport() {
        return sriovNetSupport;
    }

    public void setSriovNetSupport(String sriovNetSupport) {
        this.sriovNetSupport = sriovNetSupport;
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
     * The key of a tag assigned to the resource. Use this filter to find all resources that have a tag with a specific key, regardless of the tag value.
     */
    public String getTagKey() {
        return tagKey;
    }

    public void setTagKey(String tagKey) {
        this.tagKey = tagKey;
    }

    /**
     * The virtualization type. Valid values are ``paravirtual`` or ``hvm``.
     */
    public String getVirtualizationType() {
        return virtualizationType;
    }

    public void setVirtualizationType(String virtualizationType) {
        this.virtualizationType = virtualizationType;
    }

    @Override
    protected List<Image> findAllAws(Ec2Client client) {
        return client.describeImages().images();
    }

    @Override
    protected List<Image> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeImages(r -> r.filters(createFilters(filters))).images();
    }
}
