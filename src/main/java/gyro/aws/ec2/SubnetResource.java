package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AttributeBooleanValue;
import software.amazon.awssdk.services.ec2.model.CreateSubnetRequest;
import software.amazon.awssdk.services.ec2.model.CreateSubnetResponse;
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
import java.util.concurrent.TimeUnit;

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
 *         network-acl: $(aws::network-acl example-network-acl)
 *         availability-zone: us-east-1a
 *         cidr-block: 10.0.0.0/24
 *     end
 */
@Type("subnet")
public class SubnetResource extends Ec2TaggableResource<Subnet> implements Copyable<Subnet> {

    private VpcResource vpc;
    private String cidrBlock;
    private String availabilityZone;
    private Boolean mapPublicIpOnLaunch;
    private String subnetId;
    private NetworkAclResource networkAcl;
    private String aclAssociationId;
    private String defaultAclId;

    /**
     * The VPC to create the Subnet in. (Required)
     */
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    /**
     * The IPv4 network range for the Subnet, in CIDR notation. (Required)
     */
    public String getCidrBlock() {
        return cidrBlock;
    }

    public void setCidrBlock(String cidrBlock) {
        this.cidrBlock = cidrBlock;
    }

    /**
     * The name of the availability zone to create this Subnet (ex. ``us-east-1a``).
     */
    public String getAvailabilityZone() {
        return availabilityZone;
    }

    public void setAvailabilityZone(String availabilityZone) {
        this.availabilityZone = availabilityZone;
    }

    /**
     * Assign a public IPv4 address to Network Interfaces created in this Subnet.
     */
    @Updatable
    public Boolean getMapPublicIpOnLaunch() {
        return mapPublicIpOnLaunch;
    }

    public void setMapPublicIpOnLaunch(Boolean mapPublicIpOnLaunch) {
        this.mapPublicIpOnLaunch = mapPublicIpOnLaunch;
    }

    /**
     * The ID of the Default Network ACL associated to the Subnet.
     */
    public String getDefaultAclId() {
        return defaultAclId;
    }

    public void setDefaultAclId(String defaultAclId) {
        this.defaultAclId = defaultAclId;
    }

    /**
     * The Network ACL associated to the subnet.
     */
    @Updatable
    public NetworkAclResource getNetworkAcl() {
        return networkAcl;
    }

    public void setNetworkAcl(NetworkAclResource networkAcl) {
        this.networkAcl = networkAcl;
    }

    /**
     * The Association ID of the Network ACL currently associated to the Subnet.
     */
    public String getAclAssociationId() {
        return aclAssociationId;
    }

    public void setAclAssociationId(String aclAssociationId) {
        this.aclAssociationId = aclAssociationId;
    }

    /**
     * The ID of the Subnet.
     */
    @Id
    @Output
    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    @Override
    public String getId() {
        return getSubnetId();
    }

    @Override
    public void copyFrom(Subnet subnet) {
        setSubnetId(subnet.subnetId());
        setCidrBlock(subnet.cidrBlock());
        setAvailabilityZone(subnet.availabilityZone());
        setMapPublicIpOnLaunch(subnet.mapPublicIpOnLaunch());
        setVpc(findById(VpcResource.class, subnet.vpcId()));
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

            client.describeSubnets(request).subnets().forEach(this::copyFrom);

            DescribeNetworkAclsResponse aclResponse = client.describeNetworkAcls(
                r -> r.filters(
                    Filter.builder().name("vpc-id").values(getVpc().getId()).build(),
                    Filter.builder().name("association.subnet-id").values(getSubnetId()).build()
                )
            );

            for (NetworkAcl acl: aclResponse.networkAcls()) {

                if (!acl.isDefault().equals(true)) {
                    setNetworkAcl(!ObjectUtils.isBlank(acl.networkAclId()) ? findById(NetworkAclResource.class, acl.networkAclId()) : null);
                    if (!acl.associations().isEmpty()) {
                        acl.associations().stream()
                            .filter(a -> getSubnetId().equals(a.subnetId()))
                            .map(NetworkAclAssociation::networkAclAssociationId)
                            .forEach(this::setAclAssociationId);
                    }
                } else {
                    setDefaultAclId(acl.networkAclId());
                    setNetworkAcl(null);
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

        if (getNetworkAcl() != null) {
            ReplaceNetworkAclAssociationResponse replaceNetworkAclAssociationResponse = client.replaceNetworkAclAssociation(
                r -> r.associationId(getAclAssociationId())
                        .networkAclId(getNetworkAcl().getNetworkAclId())
            );

            setAclAssociationId(replaceNetworkAclAssociationResponse.newAssociationId());
        }

        modifyAttribute(client);
    }

    @Override
    protected void doUpdate(AwsResource current, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        if (changedProperties.contains("network-acl")) {
            String acl = getNetworkAcl() != null ? getNetworkAcl().getNetworkAclId() : getDefaultAclId();
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
        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(2, TimeUnit.SECONDS)
            .prompt(true)
            .until(() -> client.describeNetworkInterfaces(
                r -> r.filters(Filter.builder().name("subnet-id").values(getSubnetId()).build())
            ).networkInterfaces().isEmpty());

        client.deleteSubnet(r -> r.subnetId(getSubnetId()));
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("subnet");

        if (!ObjectUtils.isBlank(getSubnetId())) {
            sb.append(" - ").append(getSubnetId());
        }

        if (!ObjectUtils.isBlank(getCidrBlock())) {
            sb.append(' ');
            sb.append(getCidrBlock());
        }

        if (!ObjectUtils.isBlank(getAvailabilityZone())) {
            sb.append(" in ");
            sb.append(getAvailabilityZone());
        }

        return sb.toString();
    }

}
