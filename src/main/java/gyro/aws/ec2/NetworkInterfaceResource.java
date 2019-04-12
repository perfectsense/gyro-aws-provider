package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.services.ec2.model.SecurityGroupIdentifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ResourceName("network-interface")
public class NetworkInterfaceResource extends Ec2TaggableResource<NetworkInterface> {

    private String description;
    private String subnetId;
    private String networkInterfaceId;
    private  String ipv4Address;
    private String ipv6Address;
    private String instanceId;
    private String associationId;
    private String allocationId;
    private Integer deviceIndex;
    private String attachmentId;
    private Boolean isDeleteOnTermination;
    private List<String> securityGroupIds;

    @ResourceDiffProperty(updatable = true)
    public Boolean getDeleteOnTermination() {
        return isDeleteOnTermination;
    }

    public void setDeleteOnTermination(Boolean deleteOnTermination) {
        isDeleteOnTermination = deleteOnTermination;
    }

//Indicates whether the network interface is deleted when the instance is terminated.

    public String getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    public Integer getDeviceIndex() {
        return deviceIndex;
    }

    public void setDeviceIndex(Integer deviceIndex) {
        this.deviceIndex = deviceIndex;
    }

    public String getNetworkInterfaceId() {
        return networkInterfaceId;
    }

    public void setNetworkInterfaceId(String networkInterfaceId) {
        this.networkInterfaceId = networkInterfaceId;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    @ResourceDiffProperty(updatable = true)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getAssociationId() {
        return associationId;
    }

    public void setAssociationId(String associationId) {
        this.associationId = associationId;
    }

    public String getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(String allocationId) {
        this.allocationId = allocationId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * The list of Security Group ID being associated with the Network Interface. (Required if typeInterface set to false.)
     */
    @ResourceDiffProperty(updatable = true)
    public List<String> getSecurityGroupIds() {
        if (securityGroupIds == null) {
            securityGroupIds = new ArrayList<>();
        }
        Collections.sort(securityGroupIds);
        return securityGroupIds;
    }

    public String getIpv4Address() {
        return ipv4Address;
    }

    public void setIpv4Address(String ipv4Address) {
        this.ipv4Address = ipv4Address;
    }

    public String getIpv6Address() {
        return ipv6Address;
    }

    public void setIpv6Address(String ipv6Address) {
        this.ipv6Address = ipv6Address;
    }

    public void setSecurityGroupIds(List<String> securityGroupIds) {
        this.securityGroupIds = securityGroupIds;
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

                    setSecurityGroupIds(new ArrayList<>(nm.groups()
                            .stream()
                            .map(GroupIdentifier::groupId)
                            .collect(Collectors.toList()))
                    );

                    NetworkInterfaceAttachment attachment = nm.attachment();
                    if (attachment != null) {
                        setInstanceId(attachment.instanceId());
                        setDeviceIndex(attachment.deviceIndex());
                        setAttachmentId(attachment.attachmentId());
                        setDeleteOnTermination(attachment.deleteOnTermination());
                    }

                    if (nm.association() != null) {
                        setAssociationId(nm.association().associationId());
                        setAllocationId(nm.association().allocationId());
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
        builder.groups(getSecurityGroupIds() != null ? getSecurityGroupIds() : null);
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

        if (getDeleteOnTermination() != null) {
            NetworkInterfaceAttachmentChanges changes = NetworkInterfaceAttachmentChanges.builder()
                    .deleteOnTermination(getDeleteOnTermination())
                    .attachmentId(getAttachmentId())
                    .build();

            client.modifyNetworkInterfaceAttribute(r -> r.networkInterfaceId(getNetworkInterfaceId())
                    .attachment(changes));

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
