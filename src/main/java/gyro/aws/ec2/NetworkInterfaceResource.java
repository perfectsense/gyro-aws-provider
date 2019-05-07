package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;

import gyro.core.GyroException;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceName;
import gyro.core.resource.ResourceOutput;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AttachNetworkInterfaceResponse;
import software.amazon.awssdk.services.ec2.model.CreateNetworkInterfaceRequest;
import software.amazon.awssdk.services.ec2.model.CreateNetworkInterfaceResponse;
import software.amazon.awssdk.services.ec2.model.DescribeNetworkInterfacesResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.GroupIdentifier;
import software.amazon.awssdk.services.ec2.model.NetworkInterface;
import software.amazon.awssdk.services.ec2.model.NetworkInterfaceAttachment;
import software.amazon.awssdk.services.ec2.model.NetworkInterfaceAttachmentChanges;
import software.amazon.awssdk.services.ec2.model.NetworkInterfacePrivateIpAddress;
import software.amazon.awssdk.services.ec2.model.PrivateIpAddressSpecification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
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
 *          source-dest-check: false
 *          security-group-ids: [
 *                  $(aws::security-group security-group-nic-1| group-id),
 *                  $(aws::security-group security-group-nic-2| group-id),
 *                  $(aws::security-group security-group-nic-3| group-id)
 *                  ]
 *          primary-ipv4-address: "10.0.0.32"
 *          ipv4-addresses: [
 *              "10.0.0.55",
 *              "10.0.0.36"
 *          ]
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
    private String instanceId;
    private Integer deviceIndex;
    private String attachmentId;
    private Boolean isDeleteOnTermination;
    private List<String> securityGroupIds;
    private String primaryIpv4Address;
    private List<String> ipv4Addresses;
    private Boolean sourceDestCheck;

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
     * The list of Security Group ID's that are getting associated to the Network Interface. (Optional)
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
     * The value of attachment order to the instance, this would be `1` since the default network interface index value is `0` when an instance is created.
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
        if (isDeleteOnTermination == null) {
            isDeleteOnTermination = false;
        }

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

    /**
     * The Primary IPV4 address which gets assigned to the Network Interface. (Optional)
     */
    @ResourceDiffProperty(nullable = true)
    public String getPrimaryIpv4Address() {
        return primaryIpv4Address;
    }

    public void setPrimaryIpv4Address(String primaryIpAddress) {
        this.primaryIpv4Address = primaryIpAddress;
    }

    /**
     * The list of Secondary IPV4 addresses which gets assigned to the Network Interface. The limit of setting secondary instance depends on the instance type.
     * See `IP Address per Instance Type <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-eni.html#AvailableIpPerENI>`_.
     */
    @ResourceDiffProperty(updatable = true)
    public List<String> getIpv4Addresses() {
        if (ipv4Addresses == null) {
            ipv4Addresses = new ArrayList<>();
        }

        Collections.sort(ipv4Addresses);
        return ipv4Addresses;
    }

    public void setIpv4Addresses(List<String> ipv4Addresses) {
        this.ipv4Addresses = ipv4Addresses;
    }

    /**
     * Boolean value to enable/disable network traffic on instance that isn't specifically destined for the instance.
     * Default is `true`
     */
    @ResourceDiffProperty(updatable = true)
    public Boolean getSourceDestCheck() {
        if (sourceDestCheck == null) {
            sourceDestCheck = true;
        }
        return sourceDestCheck;
    }

    public void setSourceDestCheck(Boolean sourceDestCheck) {
        this.sourceDestCheck = sourceDestCheck;
    }

    @Override
    protected String getId() {
        return getNetworkInterfaceId();
    }

    @Override
    public boolean doRefresh() {
        NetworkInterface networkInterface = getNetworkInterface();

        if (networkInterface != null) {
            setNetworkInterfaceId(networkInterface.networkInterfaceId());
            setDescription(networkInterface.description());
            setSourceDestCheck(networkInterface.sourceDestCheck());

            if (networkInterface.groups() != null) {
                setSecurityGroupIds(new ArrayList<>(networkInterface.groups()
                        .stream()
                        .map(GroupIdentifier::groupId)
                        .collect(Collectors.toList()))
                );
            }

            if (networkInterface.privateIpAddresses() != null) {
                ArrayList<String> ipAddresses = new ArrayList<>();

                for (NetworkInterfacePrivateIpAddress address : networkInterface.privateIpAddresses()) {
                    if (address.primary()) {
                        setPrimaryIpv4Address(address.privateIpAddress());
                    } else {
                        ipAddresses.add(address.privateIpAddress());
                    }
                }
                setIpv4Addresses(ipAddresses);
            }

            NetworkInterfaceAttachment attachment = networkInterface.attachment();
            if (attachment != null) {
                setInstanceId(attachment.instanceId());
                setDeviceIndex(attachment.deviceIndex());
                setAttachmentId(attachment.attachmentId());
                setDeleteOnTermination(attachment.deleteOnTermination());
            }

            return true;
        }

       return false;
    }

    @Override
    public void doCreate() {
        Ec2Client client = createClient(Ec2Client.class);

        CreateNetworkInterfaceRequest.Builder builder = CreateNetworkInterfaceRequest.builder();

        builder.subnetId(getSubnetId());
        builder.description(getDescription());

        if (!getSecurityGroupIds().isEmpty()) {
            builder.groups(getSecurityGroupIds());
        }

        List<PrivateIpAddressSpecification> privateIpAddressSpecifications = new ArrayList<>();
        privateIpAddressSpecifications.add(
                PrivateIpAddressSpecification.builder()
                        .privateIpAddress(getPrimaryIpv4Address())
                        .primary(true)
                        .build()
        );

        if (!getIpv4Addresses().isEmpty()) {
            privateIpAddressSpecifications.addAll(
                    getIpv4Addresses().stream().map(o -> PrivateIpAddressSpecification.builder()
                            .privateIpAddress(o)
                            .build()).collect(Collectors.toList())
            );
        }

        if (getPrimaryIpv4Address() != null) {
            builder.privateIpAddresses(privateIpAddressSpecifications);
        }

        CreateNetworkInterfaceResponse response = client.createNetworkInterface(builder.build());

        NetworkInterface networkInterface = response.networkInterface();

        setNetworkInterfaceId(networkInterface.networkInterfaceId());

        try {

            if (!ObjectUtils.isBlank(getInstanceId())) {
                AttachNetworkInterfaceResponse attachNetworkInterfaceResponse = client
                        .attachNetworkInterface(n -> n.networkInterfaceId(getNetworkInterfaceId())
                                .instanceId(getInstanceId())
                                .deviceIndex(getDeviceIndex())
                );

                setAttachmentId(attachNetworkInterfaceResponse.attachmentId());

                if (getDeleteOnTermination().equals(true)) {

                    NetworkInterfaceAttachmentChanges changes = NetworkInterfaceAttachmentChanges.builder()
                            .deleteOnTermination(getDeleteOnTermination())
                            .attachmentId(getAttachmentId())
                            .build();

                    client.modifyNetworkInterfaceAttribute(r -> r.networkInterfaceId(getNetworkInterfaceId())
                            .attachment(changes)
                    );
                }
            }

            if (!getSourceDestCheck()) {
                client.modifyNetworkInterfaceAttribute(r -> r.networkInterfaceId(getNetworkInterfaceId())
                        .sourceDestCheck(a -> a.value(getSourceDestCheck()))
                );
            }
        } catch(Ec2Exception ex) {
            if (ex.getLocalizedMessage().contains("does not exist")) {
                delete();
                throw new GyroException("The instance (" + getInstanceId() + ") attachment failed, invalid instance-id.");
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
                    .attachment(changes)
            );
        }

        if (changedProperties.contains("instance-id")) {
            if (!ObjectUtils.isBlank(getInstanceId())) {
                AttachNetworkInterfaceResponse attachNetworkInterfaceResponse = client
                        .attachNetworkInterface(n -> n.networkInterfaceId(getNetworkInterfaceId())
                                .instanceId(getInstanceId())
                                .deviceIndex(getDeviceIndex())
                        );
                setAttachmentId(attachNetworkInterfaceResponse.attachmentId());
            } else {
                detachInstance(client);
            }
        }

        if (changedProperties.contains("ipv4-addresses") ) {

            NetworkInterfaceResource currentNetworkInterfaceResource = (NetworkInterfaceResource) config;

            HashSet<String> current = new HashSet<>(currentNetworkInterfaceResource.getIpv4Addresses());
            HashSet<String> pending = new HashSet<>(getIpv4Addresses());

            List<String> deleteIpv4Addresses = current.stream().filter(o -> !pending.contains(o)).collect(Collectors.toList());
            if (!deleteIpv4Addresses.isEmpty()) {
                client.unassignPrivateIpAddresses(
                        r -> r.networkInterfaceId(getNetworkInterfaceId())
                                .privateIpAddresses(deleteIpv4Addresses)
                );
            }

            List<String> addIpv4Addresses = pending.stream().filter(o -> !current.contains(o)).collect(Collectors.toList());
            if (!addIpv4Addresses.isEmpty()) {
                client.assignPrivateIpAddresses(r -> r.allowReassignment(true)
                        .networkInterfaceId(getNetworkInterfaceId())
                        .privateIpAddresses(addIpv4Addresses)
                );
            }
        }

        if (changedProperties.contains("security-group-ids")) {
            client.modifyNetworkInterfaceAttribute(
                    r -> r.networkInterfaceId(getNetworkInterfaceId())
                            .groups(getSecurityGroupIds())
            );
        }

        if (changedProperties.contains("source-dest-check")) {
            client.modifyNetworkInterfaceAttribute(r -> r.networkInterfaceId(getNetworkInterfaceId())
                    .sourceDestCheck(a -> a.value(getSourceDestCheck()))
            );
        }

        if (changedProperties.contains("description")) {
            client.modifyNetworkInterfaceAttribute(r -> r.networkInterfaceId(getNetworkInterfaceId())
                    .description(d -> d.value(getDescription()))
            );
        }
    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);

        detachInstance(client);
        client.deleteNetworkInterface(d -> d.networkInterfaceId(getNetworkInterfaceId()));
    }

    private NetworkInterface getNetworkInterface() {

        Ec2Client client = createClient(Ec2Client.class);

        if (ObjectUtils.isBlank(getNetworkInterfaceId())) {
            throw new GyroException("network-interface-id is missing, unable to load network interface.");
        }

        DescribeNetworkInterfacesResponse response = client.describeNetworkInterfaces(d -> d.networkInterfaceIds(getNetworkInterfaceId()));

        if (!response.networkInterfaces().isEmpty()) {
            return response.networkInterfaces().get(0);
        } else {
            return null;
        }
    }

    public void detachInstance(Ec2Client client) {
        NetworkInterface networkInterface = getNetworkInterface();

        if (networkInterface != null) {
            if (networkInterface.attachment() != null) {
                String attachmentId = networkInterface.attachment().attachmentId();
                client.detachNetworkInterface(r -> r.attachmentId(attachmentId));
            }

            int count = 0;

            while (networkInterface.attachment() != null && count < 10) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                networkInterface = getNetworkInterface();
                count++;
            }
        }
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("network interface ");

        if (ObjectUtils.isBlank(getDescription())) {
            sb.append(getDescription());
        }

        if (getNetworkInterfaceId() != null) {
            sb.append(getNetworkInterfaceId());
        }
        return sb.toString();
    }
}
