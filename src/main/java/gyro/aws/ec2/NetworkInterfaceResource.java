package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.List;
import java.util.Set;

@ResourceName("network-interface")
public class NetworkInterfaceResource extends Ec2TaggableResource<NetworkInterface> {

    private String description;
    private String subnetId;
    private String networkInterfaceId;
    private  String ipv4;
    private String vpcId;
    private String instanceId; // Instance Id req
    private String assocationId; // when EIP is attached it gets allocation id
    private String allocationId; // when instance is attached to this NIF it gets association id

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

    @ResourceDiffProperty
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

    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
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

        if (getIpv4() != null) {
            CreateNetworkInterfaceResponse response = client.createNetworkInterface(r -> r.description(getDescription())
                    .privateIpAddresses(d -> d.privateIpAddress(getIpv4()))
                    .subnetId(getSubnetId()));
            setNetworkInterfaceId(response.networkInterface().networkInterfaceId());

        } else {
            CreateNetworkInterfaceResponse response = client.createNetworkInterface(r -> r.description(getDescription())
                    .subnetId(getSubnetId()));
            setNetworkInterfaceId(response.networkInterface().networkInterfaceId());
        }

        DescribeNetworkInterfacesResponse response = client.describeNetworkInterfaces(d -> d.networkInterfaceIds(getNetworkInterfaceId()));

        for ( NetworkInterface ni: response.networkInterfaces()) {
            if (getInstanceId() != null) {
                ni.attachment().instanceId();
            }
        }




    }

    @Override
    protected void doUpdate(AwsResource config, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        //update description
//        DescribeNetworkInterfaceAttributeRequest request = DescribeNetworkInterfaceAttributeRequest.builder()
//                .networkInterfaceId(getNetworkInterfaceId())
//                .build();
//
        ModifyNetworkInterfaceAttributeRequest request = ModifyNetworkInterfaceAttributeRequest.builder()
                .networkInterfaceId(getNetworkInterfaceId())
                .build();


        client.modifyNetworkInterfaceAttribute(r -> r.networkInterfaceId(getNetworkInterfaceId())
                .description(d -> d.value(getDescription())));

        if (getAssocationId() != null) {

        }

        if (getAllocationId() != null) {

        }

    }


    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);
        client.deleteNetworkInterface(d -> d.networkInterfaceId(getNetworkInterfaceId()));

    }

    @Override
    public String toDisplayString() {
        return "network interface " + getDescription();
    }
}
