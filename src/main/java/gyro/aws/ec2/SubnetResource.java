package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.core.GyroException;
import gyro.core.resource.ResourceUpdatable;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceOutput;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AttributeBooleanValue;
import software.amazon.awssdk.services.ec2.model.CreateSubnetRequest;
import software.amazon.awssdk.services.ec2.model.CreateSubnetResponse;
import software.amazon.awssdk.services.ec2.model.DeleteSubnetRequest;
import software.amazon.awssdk.services.ec2.model.DescribeNetworkInterfacesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeNetworkAclsResponse;
import software.amazon.awssdk.services.ec2.model.DescribeSubnetsRequest;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.ModifySubnetAttributeRequest;
import software.amazon.awssdk.services.ec2.model.NetworkAcl;
import software.amazon.awssdk.services.ec2.model.NetworkAclAssociation;
import software.amazon.awssdk.services.ec2.model.ReplaceNetworkAclAssociationResponse;
import software.amazon.awssdk.services.ec2.model.Subnet;

import java.util.Set;

/**
 * Create a subnet in a VPC.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::subnet example-subnet
 *         vpc: $(aws::vpc example-vpc)
 *         acl-id: $(aws::network-acl example-network-acl | network-acl-id)
 *         availability-zone: us-east-1a
 *         cidr-block: 10.0.0.0/24
 *     end
 */
@ResourceType("subnet")
public class SubnetResource extends Ec2TaggableResource<Subnet> {

    private VpcResource vpc;
    private String cidrBlock;
    private String availabilityZone;
    private Boolean mapPublicIpOnLaunch;
    private String subnetId;
    private String aclId;
    private String aclAssociationId;
    private String defaultAclId;

    public SubnetResource() {

    }

    public SubnetResource(Ec2Client client, Subnet subnet) {
        setSubnetId(subnet.subnetId());
        setCidrBlock(subnet.cidrBlock());
        setAvailabilityZone(subnet.availabilityZone());
        setMapPublicIpOnLaunch(subnet.mapPublicIpOnLaunch());
        setVpc(findById(VpcResource.class, subnet.vpcId()));
    }

    /**
     * The VPC to create the subnet in. (Required)
     */
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    /**
     * The IPv4 network range for the subnet, in CIDR notation. (Required)
     */
    public String getCidrBlock() {
        return cidrBlock;
    }

    public void setCidrBlock(String cidrBlock) {
        this.cidrBlock = cidrBlock;
    }

    /**
     * The name of the availablity zone to create this subnet (ex. ``us-east-1a``).
     */
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * Assign a public IPv4 address to network interfaces created in this subnet.
     */
    @ResourceUpdatable
    public Boolean getMapPublicIpOnLaunch() {
        return mapPublicIpOnLaunch;
    }

    public void setMapPublicIpOnLaunch(Boolean mapPublicIpOnLaunch) {
        this.mapPublicIpOnLaunch = mapPublicIpOnLaunch;
    }

    @ResourceOutput
    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public String getId() {
        return getSubnetId();
    }

    /**
     * The ID of the Default Network ACL associated to the subnet.
     */
    public String getDefaultAclId() {
        return defaultAclId;
    }

    public void setDefaultAclId(String defaultAclId) {
        this.defaultAclId = defaultAclId;
    }

    /**
     * The ID of the Network ACL associated to the subnet.
     */
    @ResourceUpdatable
    public String getAclId() {
        return aclId;
    }

    public void setAclId(String aclId) {
        this.aclId = aclId;
    }

    /**
     * The Association ID of the Network ACL currently associated to the subnet.
     */
    public String getAclAssociationId() {
        return aclAssociationId;
    }

    public void setAclAssociationId(String aclAssociationId) {
        this.aclAssociationId = aclAssociationId;
    }

    @Override
    public boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        if (ObjectUtils.isBlank(getSubnetId())) {
            throw new GyroException("subnet-id is missing, unable to load subnet.");
        }

        try {
            DescribeSubnetsRequest request = DescribeSubnetsRequest.builder()
                .subnetIds(getSubnetId())
                .build();

            for (Subnet subnet : client.describeSubnets(request).subnets()) {
                setSubnetId(subnet.subnetId());
                setAvailabilityZone(subnet.availabilityZone());
                setCidrBlock(subnet.cidrBlock());
                setMapPublicIpOnLaunch(subnet.mapPublicIpOnLaunch());
            }

            DescribeNetworkAclsResponse aclResponse = client.describeNetworkAcls(
                r -> r.filters(
                    Filter.builder().name("vpc-id").values(getVpc().getId()).build(),
                    Filter.builder().name("association.subnet-id").values(getSubnetId()).build()
                )
            );

            for (NetworkAcl acl: aclResponse.networkAcls()) {

                if (!acl.isDefault().equals(true)) {
                    setAclId(acl.networkAclId());
                    if (!acl.associations().isEmpty()) {
                        acl.associations().stream()
                            .filter(a -> getSubnetId().equals(a.subnetId()))
                            .map(NetworkAclAssociation::networkAclAssociationId)
                            .forEach(this::setAclAssociationId);
                    }
                } else {
                    setDefaultAclId(acl.networkAclId());
                    setAclId(null);
                    if (!acl.associations().isEmpty()) {
                        acl.associations().stream()
                            .filter(a -> getSubnetId().equals(a.subnetId()))
                            .map(NetworkAclAssociation::networkAclAssociationId)
                            .forEach(this::setAclAssociationId);
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
    protected void doCreate() {
        Ec2Client client = createClient(Ec2Client.class);

        CreateSubnetRequest request = CreateSubnetRequest.builder()
                .availabilityZone(getAvailabilityZone())
                .cidrBlock(getCidrBlock())
                .vpcId(getVpc().getId())
                .build();

        CreateSubnetResponse response = client.createSubnet(request);
        setSubnetId(response.subnet().subnetId());

        DescribeNetworkAclsResponse aclResponse = client.describeNetworkAcls(
            r -> r.filters(
                Filter.builder().name("vpc-id").values(getVpc().getId()).build(),
                Filter.builder().name("association.subnet-id").values(getSubnetId()).build()
            )
        );

        for (NetworkAcl acl: aclResponse.networkAcls()) {
            if (!acl.associations().isEmpty()) {
                setDefaultAclId(acl.networkAclId());
                acl.associations().stream()
                    .filter(a -> getSubnetId().equals(a.subnetId()))
                    .map(NetworkAclAssociation::networkAclAssociationId)
                    .forEach(this::setAclAssociationId);
            }
        }

        if (getAclId() != null) {
            ReplaceNetworkAclAssociationResponse replaceNetworkAclAssociationResponse = client.replaceNetworkAclAssociation(
                r -> r.associationId(getAclAssociationId())
                        .networkAclId(getAclId())
            );

            setAclAssociationId(replaceNetworkAclAssociationResponse.newAssociationId());
        }

        modifyAttribute(client);
    }

    @Override
    protected void doUpdate(AwsResource current, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        if (changedProperties.contains("acl-id")) {
            String acl = getAclId() != null ? getAclId() : getDefaultAclId();
            ReplaceNetworkAclAssociationResponse replaceNetworkAclAssociationResponse = client.replaceNetworkAclAssociation(
                r -> r.associationId(getAclAssociationId())
                        .networkAclId(acl)
            );

            setAclAssociationId(replaceNetworkAclAssociationResponse.newAssociationId());
        }

        modifyAttribute(client);
    }

    private void modifyAttribute(Ec2Client client) {
        if (getMapPublicIpOnLaunch() != null) {
            ModifySubnetAttributeRequest request = ModifySubnetAttributeRequest.builder()
                    .subnetId(getSubnetId())
                    .mapPublicIpOnLaunch(AttributeBooleanValue.builder().value(getMapPublicIpOnLaunch()).build())
                    .build();

            client.modifySubnetAttribute(request);
        }
    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);

        // Network interfaces may still be detaching, so check and wait
        // before deleting the subnet.
        while (true) {
            DescribeNetworkInterfacesRequest request = DescribeNetworkInterfacesRequest.builder()
                    .filters(Filter.builder()
                            .name("subnet-id")
                            .values(getSubnetId()).build())
                    .build();

            if (client.describeNetworkInterfaces(request).networkInterfaces().isEmpty()) {
                break;
            }

            try {
                Thread.sleep(1000);

            } catch (InterruptedException error) {
                break;
            }
        }

        DeleteSubnetRequest request = DeleteSubnetRequest.builder()
                .subnetId(getSubnetId())
                .build();

        client.deleteSubnet(request);
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        String subnetId = getSubnetId();

        if (subnetId != null) {
            sb.append(subnetId);

        } else {
            sb.append("subnet");
        }

        String cidrBlock = getCidrBlock();

        if (cidrBlock != null) {
            sb.append(' ');
            sb.append(getCidrBlock());
        }

        String availabilityZone = getAvailabilityZone();

        if (availabilityZone != null) {
            sb.append(" in ");
            sb.append(availabilityZone);
        }

        return sb.toString();
    }

}
