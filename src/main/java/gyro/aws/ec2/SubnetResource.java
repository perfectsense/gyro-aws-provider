package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.scope.State;
import org.apache.commons.lang.NotImplementedException;
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
    private String id;
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
     * Assign a public IPv4 address to Network Interfaces created in this Subnet. Defaults to ``false``.
     */
    @Updatable
    public Boolean getMapPublicIpOnLaunch() {
        if (mapPublicIpOnLaunch == null) {
            mapPublicIpOnLaunch = false;
        }

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
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getResourceId() {
        return getId();
    }

    @Override
    public void copyFrom(Subnet subnet) {
        setId(subnet.subnetId());
        setCidrBlock(subnet.cidrBlock());
        setAvailabilityZone(subnet.availabilityZone());
        setMapPublicIpOnLaunch(subnet.mapPublicIpOnLaunch());
        setVpc(findById(VpcResource.class, subnet.vpcId()));
    }

    @Override
    public boolean doRefresh() {
        throw new NotImplementedException();
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        throw new NotImplementedException();
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource current, Set<String> changedProperties) {
        throw new NotImplementedException();
    }

    private void modifyAttribute(Ec2Client client) {
        if (getMapPublicIpOnLaunch() != null) {
            ModifySubnetAttributeRequest request = ModifySubnetAttributeRequest.builder()
                .subnetId(getId())
                .mapPublicIpOnLaunch(AttributeBooleanValue.builder().value(getMapPublicIpOnLaunch()).build())
                .build();

            client.modifySubnetAttribute(request);
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        throw new NotImplementedException();
    }
}