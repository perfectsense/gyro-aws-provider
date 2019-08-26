package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;

import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.scope.State;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AttachNetworkInterfaceResponse;
import software.amazon.awssdk.services.ec2.model.CreateNetworkInterfaceRequest;
import software.amazon.awssdk.services.ec2.model.CreateNetworkInterfaceResponse;
import software.amazon.awssdk.services.ec2.model.DescribeNetworkInterfacesResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.NetworkInterface;
import software.amazon.awssdk.services.ec2.model.NetworkInterfaceAttachment;
import software.amazon.awssdk.services.ec2.model.NetworkInterfaceAttachmentChanges;
import software.amazon.awssdk.services.ec2.model.NetworkInterfacePrivateIpAddress;
import software.amazon.awssdk.services.ec2.model.PrivateIpAddressSpecification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
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
 *          subnet: $(aws::subnet subnet-example-nic)
 *          instance: $(aws::instance instance-example-nic)
 *          device-index: 1
 *          delete-on-termination: false
 *          source-dest-check: false
 *          security-groups: [
 *                  $(aws::security-group security-group-nic-1),
 *                  $(aws::security-group security-group-nic-2),
 *                  $(aws::security-group security-group-nic-3)
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

@Type("network-interface")
public class NetworkInterfaceResource extends Ec2TaggableResource<NetworkInterface> implements Copyable<NetworkInterface> {

    private String description;
    private SubnetResource subnet;
    private String id;
    private InstanceResource instance;
    private Integer deviceIndex;
    private String attachmentId;
    private Boolean isDeleteOnTermination;
    private Set<SecurityGroupResource> securityGroups;
    private String primaryIpv4Address;
    private Set<String> ipv4Addresses;
    private Boolean sourceDestCheck;

    /**
     * The description of the Network Interface that is being created.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The Subnet to create the Network Interface in. (Required)
     */
    @Updatable
    public SubnetResource getSubnet() {
        return subnet;
    }

    public void setSubnet(SubnetResource subnet) {
        this.subnet = subnet;
    }

    /**
     * The list of Security Group's that are getting associated to the Network Interface. If no Security Group is given, the default security group attached to the subnet will be assigned to the network interface.
     */
    @Updatable
    public Set<SecurityGroupResource> getSecurityGroups() {
        if (securityGroups == null) {
            securityGroups = new HashSet<>();
        }

        return securityGroups;
    }

    public void setSecurityGroups(Set<SecurityGroupResource> securityGroups) {
        this.securityGroups = securityGroups;
    }

    /**
     * The Instance to which the Network Interface will be attached.(Optional)
     */
    @Updatable
    public InstanceResource getInstance() {
        return instance;
    }

    public void setInstance(InstanceResource instance) {
        this.instance = instance;
    }

    /**
     * The index of the device for the Network Interface attachment. This will start from ``1`` as boot device is set at ``0``.
     */
    @Updatable
    public Integer getDeviceIndex() {
        return deviceIndex;
    }

    public void setDeviceIndex(Integer deviceIndex) {
        this.deviceIndex = deviceIndex;
    }

    /**
     * Boolean value to indicate if the Network Interface will be deleted when the Instance is terminated.
     */
    @Updatable
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
     * The ID generated when an Instance is attached to the Network Interface.
     */
    @Output
    public String getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    /**
     * The Primary IPV4 address which gets assigned to the Network Interface. (Optional)
     */
    public String getPrimaryIpv4Address() {
        return primaryIpv4Address;
    }

    public void setPrimaryIpv4Address(String primaryIpAddress) {
        this.primaryIpv4Address = primaryIpAddress;
    }

    /**
     * The list of Secondary IPV4 addresses which gets assigned to the Network Interface. The limit of setting secondary instance depends on the instance type. See `IP Address per Instance Type <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/using-eni.html#AvailableIpPerENI>`_.
     */
    @Updatable
    public Set<String> getIpv4Addresses() {
        if (ipv4Addresses == null) {
            ipv4Addresses = new HashSet<>();
        }

        return ipv4Addresses;
    }

    public void setIpv4Addresses(Set<String> ipv4Addresses) {
        this.ipv4Addresses = ipv4Addresses;
    }

    /**
     * Boolean value to enable/disable network traffic on instance that isn't specifically destined for the instance. Defaults to ``true``.
     */
    @Updatable
    public Boolean getSourceDestCheck() {
        if (sourceDestCheck == null) {
            sourceDestCheck = true;
        }
        return sourceDestCheck;
    }

    public void setSourceDestCheck(Boolean sourceDestCheck) {
        this.sourceDestCheck = sourceDestCheck;
    }

    /**
     * The ID of the Network Interface which is generated with the resource.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected String getResourceId() {
        return getId();
    }

    @Override
    public void copyFrom(NetworkInterface networkInterface) {
        setId(networkInterface.networkInterfaceId());
        setDescription(networkInterface.description());
        setSourceDestCheck(networkInterface.sourceDestCheck());
        setSubnet(!ObjectUtils.isBlank(networkInterface.subnetId()) ? findById(SubnetResource.class, networkInterface.subnetId()) : null);

        if (networkInterface.groups() != null) {
            setSecurityGroups(networkInterface.groups()
                .stream()
                .map(o -> findById(SecurityGroupResource.class, o.groupId()))
                .collect(Collectors.toSet()));
        }

        if (networkInterface.privateIpAddresses() != null) {
            Set<String> ipAddresses = new HashSet<>();

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
            setInstance(!ObjectUtils.isBlank(attachment.instanceId()) ? findById(InstanceResource.class, attachment.instanceId()) : null);
            setDeviceIndex(attachment.deviceIndex());
            setAttachmentId(attachment.attachmentId());
            setDeleteOnTermination(attachment.deleteOnTermination());
        }

        refreshTags();
    }

    @Override
    public boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        NetworkInterface networkInterface = getNetworkInterface(client);

        if (networkInterface == null) {
            return false;
        }

        copyFrom(networkInterface);

        return true;
    }

    @Override
    public void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        CreateNetworkInterfaceRequest.Builder builder = CreateNetworkInterfaceRequest.builder();

        builder.subnetId(getSubnet().getId());
        builder.description(getDescription());

        if (!getSecurityGroups().isEmpty()) {
            builder.groups(getSecurityGroups()
                .stream()
                .map(SecurityGroupResource::getId)
                .collect(Collectors.toList()));
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

        setId(networkInterface.networkInterfaceId());

        try {

            if (getInstance() != null) {
                AttachNetworkInterfaceResponse attachNetworkInterfaceResponse = client
                        .attachNetworkInterface(n -> n.networkInterfaceId(getId())
                                .instanceId(getInstance().getId())
                                .deviceIndex(getDeviceIndex())
                );

                setAttachmentId(attachNetworkInterfaceResponse.attachmentId());

                if (getDeleteOnTermination().equals(true)) {

                    NetworkInterfaceAttachmentChanges changes = NetworkInterfaceAttachmentChanges.builder()
                            .deleteOnTermination(getDeleteOnTermination())
                            .attachmentId(getAttachmentId())
                            .build();

                    client.modifyNetworkInterfaceAttribute(r -> r.networkInterfaceId(getId())
                            .attachment(changes)
                    );
                }
            }

            if (!getSourceDestCheck()) {
                client.modifyNetworkInterfaceAttribute(r -> r.networkInterfaceId(getId())
                        .sourceDestCheck(a -> a.value(getSourceDestCheck()))
                );
            }
        } catch(Ec2Exception ex) {
            if (ex.getLocalizedMessage().contains("does not exist")) {
                delete(ui, state);
                throw new GyroException("The instance (" + getInstance().getId() + ") attachment failed.");
            }
        }
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        if (changedProperties.contains("delete-on-termination") && getAttachmentId() != null) {
            NetworkInterfaceAttachmentChanges changes = NetworkInterfaceAttachmentChanges.builder()
                    .deleteOnTermination(getDeleteOnTermination())
                    .attachmentId(getAttachmentId())
                    .build();

            client.modifyNetworkInterfaceAttribute(r -> r.networkInterfaceId(getId())
                    .attachment(changes)
            );
        }

        if (changedProperties.contains("instance")) {
            if (getInstance() != null) {
                AttachNetworkInterfaceResponse attachNetworkInterfaceResponse = client
                        .attachNetworkInterface(n -> n.networkInterfaceId(getId())
                                .instanceId(getInstance().getId())
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
                        r -> r.networkInterfaceId(getId())
                                .privateIpAddresses(deleteIpv4Addresses)
                );
            }

            List<String> addIpv4Addresses = pending.stream().filter(o -> !current.contains(o)).collect(Collectors.toList());
            if (!addIpv4Addresses.isEmpty()) {
                client.assignPrivateIpAddresses(r -> r.allowReassignment(true)
                        .networkInterfaceId(getId())
                        .privateIpAddresses(addIpv4Addresses)
                );
            }
        }

        if (changedProperties.contains("security-groups")) {
            client.modifyNetworkInterfaceAttribute(
                    r -> r.networkInterfaceId(getId())
                            .groups(getSecurityGroups()
                                .stream()
                                .map(SecurityGroupResource::getId)
                                .collect(Collectors.toList()))
            );
        }

        if (changedProperties.contains("source-dest-check")) {
            client.modifyNetworkInterfaceAttribute(r -> r.networkInterfaceId(getId())
                    .sourceDestCheck(a -> a.value(getSourceDestCheck()))
            );
        }

        if (changedProperties.contains("description")) {
            client.modifyNetworkInterfaceAttribute(r -> r.networkInterfaceId(getId())
                    .description(d -> d.value(getDescription()))
            );
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        detachInstance(client);

        client.deleteNetworkInterface(d -> d.networkInterfaceId(getId()));

        //wait for the detachment from subnet.
        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(2, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> client.describeNetworkInterfaces(
                r -> r.filters(Filter.builder().name("subnet-id").values(getSubnet().getId()).build())
            ).networkInterfaces().stream().noneMatch(o -> o.networkInterfaceId().equals(getId())));
    }

    private NetworkInterface getNetworkInterface(Ec2Client client) {
        NetworkInterface networkInterface = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load network interface.");
        }

        try {
            DescribeNetworkInterfacesResponse response = client.describeNetworkInterfaces(d -> d.networkInterfaceIds(getId()));

            if (!response.networkInterfaces().isEmpty()) {
                networkInterface = response.networkInterfaces().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return networkInterface;
    }

    private boolean isInstanceDetached(Ec2Client client) {
        NetworkInterface networkInterface = getNetworkInterface(client);

        return networkInterface != null && networkInterface.attachment() == null;
    }

    private void detachInstance(Ec2Client client) {
        NetworkInterface networkInterface = getNetworkInterface(client);

        if (networkInterface != null) {
            if (networkInterface.attachment() != null) {
                String attachmentId = networkInterface.attachment().attachmentId();
                client.detachNetworkInterface(r -> r.attachmentId(attachmentId));
            }

            Wait.atMost(2, TimeUnit.MINUTES)
                .prompt(true)
                .checkEvery(3, TimeUnit.SECONDS)
                .until(() -> isInstanceDetached(client));
        }
    }
}
