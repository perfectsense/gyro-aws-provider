package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateImageResponse;
import software.amazon.awssdk.services.ec2.model.DescribeImagesResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Image;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Type("ami")
public class AmiResource extends AwsResource implements Copyable<Image> {
    private String name;
    private String description;
    private InstanceResource instance;
    private Boolean noReboot;
    private Set<BlockDeviceMappingResource> blockDeviceMapping;

    private String id;

    /**
     * The name of the AMI. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The description of the AMI.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The Instance from which teh AMI is going to be created. (Required)
     */
    @Required
    public InstanceResource getInstance() {
        return instance;
    }

    public void setInstance(InstanceResource instance) {
        this.instance = instance;
    }

    /**
     * Specify if the Instance is rebooted when creating the image or not.
     */
    public Boolean getNoReboot() {
        if (noReboot == null) {
            noReboot = false;
        }

        return noReboot;
    }

    public void setNoReboot(Boolean noReboot) {
        this.noReboot = noReboot;
    }

    public Set<BlockDeviceMappingResource> getBlockDeviceMapping() {
        if (blockDeviceMapping == null) {
            blockDeviceMapping = new HashSet<>();
        }

        return blockDeviceMapping;
    }

    public void setBlockDeviceMapping(Set<BlockDeviceMappingResource> blockDeviceMapping) {
        this.blockDeviceMapping = blockDeviceMapping;
    }

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(Image image) {
        setName(image.name());
        setDescription(image.description());
        setId(image.imageId());
        setBlockDeviceMapping(image.blockDeviceMappings().stream().map(o -> {
            BlockDeviceMappingResource blockDeviceMapping = newSubresource(BlockDeviceMappingResource.class);
            blockDeviceMapping.copyFrom(o);
            return blockDeviceMapping;
        }).collect(Collectors.toSet()));
    }

    @Override
    public boolean refresh() {
        Ec2Client client = createClient(Ec2Client.class);

        Image image = getImage(client);

        if (image == null) {
            return false;
        }

        copyFrom(image);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);

        CreateImageResponse response = client.createImage(
            r -> r.name(getName())
                .description(getDescription())
                .instanceId(getInstance().getId())
                .noReboot(getNoReboot())
                .blockDeviceMappings(getBlockDeviceMapping().stream().map(BlockDeviceMappingResource::getBlockDeviceMapping).collect(Collectors.toSet()))
        );

        setId(response.imageId());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);

        client.deregisterImage(r -> r.imageId(getId()));
    }

    private Image getImage(Ec2Client client) {
        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load Ami.");
        }

        try {
            DescribeImagesResponse response = client.describeImages(r -> r.imageIds(getId()));

            if (!response.images().isEmpty()) {
                return response.images().get(0);
            }
        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return null;
    }
}
