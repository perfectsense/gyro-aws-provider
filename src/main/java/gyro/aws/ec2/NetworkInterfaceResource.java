package gyro.aws.ec2;

import gyro.aws.AwsResource;

import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import gyro.core.resource.ResourceOutput;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AttachNetworkInterfaceResponse;
import software.amazon.awssdk.services.ec2.model.CreateNetworkInterfaceRequest;
import software.amazon.awssdk.services.ec2.model.CreateNetworkInterfaceResponse;
import software.amazon.awssdk.services.ec2.model.DescribeNetworkInterfacesResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.GroupIdentifier;
import software.amazon.awssdk.services.ec2.model.NetworkInterface;
import software.amazon.awssdk.services.ec2.model.NetworkInterfaceAttachment;
import software.amazon.awssdk.services.ec2.model.NetworkInterfaceAttachmentChanges;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Create a network interface in a VPC.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *      aws::network-interface network-interface-example
 *          description: "network-interface-example"
 *          subnet-id: $(aws::subnet subnet-example-nic | subnet-id)
 *          instance-id: $(aws::instance instance-example-nic | instance-id)
 *          device-index: 1
 *          delete-on-termination: false
 *          security-group-ids: [
 *                  $(aws::security-group security-group-nic-1| group-id),
 *                  $(aws::security-group security-group-nic-2| group-id),
 *                  $(aws::security-group security-group-nic-3| group-id)
 *                  ]
 *          tags: {
 *              Name: "network-interface-example"
 *          }
 *      end
 *
 */

@ResourceName("network-interface")
public class NetworkInterfaceResource extends Ec2TaggableResource<NetworkInterface> {

    private String description;
    private String subnetId;
    private String networkInterfaceId;
    private  String ipv4Address;
    private String ipv6Address;
    private String instanceId;
    private Integer deviceIndex;
    private String attachmentId;
    private Boolean isDeleteOnTermination;
    private List<String> securityGroupIds;


    /**
     * The description of the network interface card that is being created.
     */
    @ResourceDiffProperty(updatable = true)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The subnet id to create the network interface in. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    /**
     * The ID of the Network Interface which is generated with the resource.
     */
    @ResourceOutput
    public String getNetworkInterfaceId() {
        return networkInterfaceId;
    }

    public void setNetworkInterfaceId(String networkInterfaceId) {
        this.networkInterfaceId = networkInterfaceId;
    }

    /**
     * The list of Security Group ID being associated with the Network Interface. (Optional)
     * If no security id is given, the default security group attached to the subnet will be assigned to the network interface.
     */
    @ResourceDiffProperty(updatable = true, nullable = true)
    public List<String> getSecurityGroupIds() {
        if (securityGroupIds == null) {
            securityGroupIds = new ArrayList<>();
        }

        Collections.sort(securityGroupIds);
        return securityGroupIds;
    }

    public void setSecurityGroupIds(List<String> securityGroupIds) {
        this.securityGroupIds = securityGroupIds;
    }

    /**
     * The IPV4 address which gets assigned to the Network Interface. (Optional)
     */
    public String getIpv4Address() {
        return ipv4Address;
    }

    public void setIpv4Address(String ipv4Address) {
        this.ipv4Address = ipv4Address;
    }

    /**
     * The IPV6 address which gets assigned to the Network Interface. (Optional)
     */
    public String getIpv6Address() {
        return ipv6Address;
    }

    public void setIpv6Address(String ipv6Address) {
        this.ipv6Address = ipv6Address;
    }

    /**
     * The id of the instance to which the network interface card will be attached.(Optional)
     */
    @ResourceDiffProperty(updatable = true, nullable = true)
    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * The value of attachment order to the instance, this would be `1` since the default network interface index value is `0`, when an instance is created.
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getDeviceIndex() {
        return deviceIndex;
    }

    public void setDeviceIndex(Integer deviceIndex) {
        this.deviceIndex = deviceIndex;
    }

    /**
     * Boolean value to indicate if the network interface will be deleted when the instance is terminated.
     */
    @ResourceDiffProperty(updatable = true)
    public Boolean getDeleteOnTermination() {
        return isDeleteOnTermination;
    }

    public void setDeleteOnTermination(Boolean deleteOnTermination) {
        isDeleteOnTermination = deleteOnTermination;
    }

    /**
     * The id generated when an instance is attached to the network interface.
     */
    @ResourceDiffProperty
    public String getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    @Override
    protected String getId() {
        return getNetworkInterfaceId();
    }

    @Override
    public boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        try {

            DescribeNetworkInterfacesResponse response = client.describeNetworkInterfaces(d -> d.networkInterfaceIds(getNetworkInterfaceId()));

            if (response.networkInterfaces() != null) {
                for (NetworkInterface nm : response.networkInterfaces()) {

                    setNetworkInterfaceId(nm.networkInterfaceId());
                    setDescription(nm.description());

                    if (nm.groups() != null) {
                        setSecurityGroupIds(new ArrayList<>(nm.groups()
                                .stream()
                                .map(GroupIdentifier::groupId)
                                .collect(Collectors.toList()))
                        );
                    }

                    NetworkInterfaceAttachment attachment = nm.attachment();
                    if (attachment != null) {
                        setInstanceId(attachment.instanceId());
                        setDeviceIndex(attachment.deviceIndex());
                        setAttachmentId(attachment.attachmentId());
                        setDeleteOnTermination(attachment.deleteOnTermination());
                    }
                }
            }
        } catch (Ec2Exception ex) {
            if (ex.getLocalizedMessage().contains("does not exist")) {
                return false;
            }

            throw ex;
        }

        return true;
    }

    @Override
    public void doCreate() {
        Ec2Client client = createClient(Ec2Client.class);

        CreateNetworkInterfaceRequest.Builder builder = CreateNetworkInterfaceRequest.builder();

        builder.subnetId(getSubnetId());
        builder.description(getDescription());

        if (getSecurityGroupIds().size() != 0) {
            builder.groups(getSecurityGroupIds());
        }

        builder.privateIpAddress(getIpv4Address() != null ? getIpv4Address() : null);

        CreateNetworkInterfaceResponse response = client.createNetworkInterface(builder.build());

        NetworkInterface networkInterface = response.networkInterface();

        setNetworkInterfaceId(networkInterface.networkInterfaceId());

        if (getInstanceId() != null) {
            AttachNetworkInterfaceResponse attachNetworkInterfaceResponse = client
                    .attachNetworkInterface(n -> n.networkInterfaceId(getNetworkInterfaceId())
                    .instanceId(getInstanceId())
                    .deviceIndex(getDeviceIndex())
                    );

            setAttachmentId(attachNetworkInterfaceResponse.attachmentId());

            if (getDeleteOnTermination() != null) {

                NetworkInterfaceAttachmentChanges changes = NetworkInterfaceAttachmentChanges.builder()
                        .deleteOnTermination(getDeleteOnTermination())
                        .attachmentId(getAttachmentId())
                        .build();

                client.modifyNetworkInterfaceAttribute(r -> r.networkInterfaceId(getNetworkInterfaceId())
                        .attachment(changes)
                );
            }
        }
    }

    @Override
    protected void doUpdate(AwsResource config, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        if (changedProperties.contains("delete-on-termination") && getAttachmentId() != null) {
            NetworkInterfaceAttachmentChanges changes = NetworkInterfaceAttachmentChanges.builder()
                    .deleteOnTermination(getDeleteOnTermination())
                    .attachmentId(getAttachmentId())
                    .build();

            client.modifyNetworkInterfaceAttribute(r -> r.networkInterfaceId(getNetworkInterfaceId())
                    .attachment(changes));

        }

        if (changedProperties.contains("instance-id")) {
            if (getInstanceId() != null) {
                AttachNetworkInterfaceResponse attachNetworkInterfaceResponse = client
                        .attachNetworkInterface(n -> n.networkInterfaceId(getNetworkInterfaceId())
                        .instanceId(getInstanceId())
                        .deviceIndex(getDeviceIndex())
                );

                setAttachmentId(attachNetworkInterfaceResponse.attachmentId());

            } else {
                executeService(() -> {
                    client.detachNetworkInterface(r -> r.attachmentId(getAttachmentId()));
                    return null;
                });
            }
        }

        if (changedProperties.contains("security-group-ids")) {
            client.modifyNetworkInterfaceAttribute(
                    r -> r.networkInterfaceId(getNetworkInterfaceId())
                            .groups(getSecurityGroupIds())
            );
        }
        client.modifyNetworkInterfaceAttribute(r -> r.networkInterfaceId(getNetworkInterfaceId())
                .description(d -> d.value(getDescription())));
    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);

        DescribeNetworkInterfacesResponse response = client.describeNetworkInterfaces(d -> d.filters(Filter.builder()
                .name("network-interface-id")
                .values(getNetworkInterfaceId())
                .build()
        ));

        for (NetworkInterface nm : response.networkInterfaces()) {

            NetworkInterfaceAttachment attachment = nm.attachment();

            if (attachment != null) {
                executeService(() -> {
                    client.detachNetworkInterface(r -> r.attachmentId(attachment.attachmentId()));
                    return null;
                });
            }
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
                executeService(() -> {
                    client.deleteNetworkInterface(d -> d.networkInterfaceId(getNetworkInterfaceId()));
                    return null;
                });
            } catch (Exception e) {
                e.printStackTrace();
        }
    }

    @Override
    public String toDisplayString() {
        return "network interface " + getDescription();
    }
}
