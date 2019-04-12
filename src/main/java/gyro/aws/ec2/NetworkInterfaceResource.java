package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ResourceName("network-interface")
public class NetworkInterfaceResource extends Ec2TaggableResource<NetworkInterface> {

    private String description;
    private String subnetId;
    private String networkInterfaceId;
    private  String ipv4;
    private String instanceId; // Instance Id req
    private String assocationId; // when EIP is attached it gets allocation id (optional)
    private String allocationId; // when eip is allocated to this NIF it gets association id (optional)
    private Integer deviceIndex;
    private String attachmentId;
    private Boolean isDeleteOnTermination;
    private List<String> securityGroups;

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

    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }


    public String getAssocationId() {
        return assocationId;
    }

    public void setAssocationId(String assocationId) {
        this.assocationId = assocationId;
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

    public List<String> getSecurityGroups() {
        if (securityGroups == null) {
            securityGroups = new ArrayList<>();
        }
        return securityGroups;
    }

    public void setSecurityGroups(List<String> securityGroups) {
        this.securityGroups = securityGroups;
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
                    System.out.println("NIC ID --- " + nm.networkInterfaceId());

                    setNetworkInterfaceId(nm.networkInterfaceId());
                    setDescription(nm.description());


                    NetworkInterfaceAttachment attachment = nm.attachment();

                    if ( attachment != null) {

                        setInstanceId(attachment.instanceId() != null ? attachment.instanceId() : null);
                        setDeviceIndex(attachment.deviceIndex() != null ? attachment.deviceIndex() : null);
                        setAttachmentId(attachment.attachmentId());
                        setDeleteOnTermination(attachment.deleteOnTermination());
                    }

                    if (nm.association() != null) {
                        setAssocationId(nm.association().associationId() != null ? nm.association().associationId() : null);
                        setAllocationId(nm.association().allocationId() != null ? nm.association().allocationId() : null);
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


        CreateNetworkInterfaceResponse response = client.createNetworkInterface(r -> r.description(getDescription())
                .privateIpAddresses(d -> d.privateIpAddress(getIpv4()))
                .subnetId(getSubnetId())
                .groups(getSecurityGroups().toString())
        );

        setNetworkInterfaceId(response.networkInterface().networkInterfaceId());

        if (getInstanceId() != null) {
            AttachNetworkInterfaceResponse attachNetworkInterfaceResponse = client
                    .attachNetworkInterface(n -> n.networkInterfaceId(getNetworkInterfaceId())
                    .instanceId(getInstanceId()).deviceIndex(getDeviceIndex()));

            setAttachmentId(attachNetworkInterfaceResponse.attachmentId());

            if (getDeleteOnTermination() != null) {
                NetworkInterfaceAttachmentChanges changes = NetworkInterfaceAttachmentChanges.builder().deleteOnTermination(getDeleteOnTermination())
                        .attachmentId(getAttachmentId())
                        .build();
                client.modifyNetworkInterfaceAttribute(r -> r.networkInterfaceId(getNetworkInterfaceId()).attachment(changes));

            }
        }

    }

    @Override
    protected void doUpdate(AwsResource config, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        if (getDeleteOnTermination() != null) {
            NetworkInterfaceAttachmentChanges changes = NetworkInterfaceAttachmentChanges.builder().deleteOnTermination(getDeleteOnTermination())
                    .attachmentId(getAttachmentId())
                    .build();
            client.modifyNetworkInterfaceAttribute(r -> r.networkInterfaceId(getNetworkInterfaceId()).attachment(changes));

        }
        client.modifyNetworkInterfaceAttribute(r -> r.networkInterfaceId(getNetworkInterfaceId())
                .description(d -> d.value(getDescription())));
    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);

        DescribeNetworkInterfacesResponse response = client.describeNetworkInterfaces(d -> d.filters(Filter.builder()
                .name("network-interface-id").values(getNetworkInterfaceId()).build()
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
