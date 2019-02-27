package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.core.diff.ResourceName;
import gyro.core.diff.ResourceOutput;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateNetworkAclResponse;
import software.amazon.awssdk.services.ec2.model.DescribeNetworkAclsResponse;
import software.amazon.awssdk.services.ec2.model.NetworkAcl;

import java.util.Set;

/**
 * Create Network ACL in the provided VPC.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::network-acl network-acl-example
 *         vpc-id: $(aws::vpc vpc-example-for-network-acl | vpc-id)
 *
 *         tags: {
 *             Name: "network-acl-example"
 *         }
 *     end
 */
@ResourceName("network-acl")
public class NetworkAclResource extends Ec2TaggableResource<NetworkAcl> {

    private String vpcId;
    private String networkAclId;

    /**
     * The ID of the VPC to create the Network ACL in. See `Network ACLs <https://docs.aws.amazon.com/vpc/latest/userguide/vpc-network-acls.html/>`_. (Required)
     */
    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    @ResourceOutput
    public String getNetworkAclId() {
        return networkAclId;
    }

    public void setNetworkAclId(String networkAclId) {
        this.networkAclId = networkAclId;
    }

    @Override
    protected String getId() {
        return getNetworkAclId();
    }

    @Override
    protected boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        DescribeNetworkAclsResponse response = client.describeNetworkAcls(r -> r.networkAclIds(getNetworkAclId()));

        if (!response.networkAcls().isEmpty()) {
            NetworkAcl networkAcl = response.networkAcls().get(0);
            setVpcId(networkAcl.vpcId());

            return true;
        }

        return false;
    }

    @Override
    protected void doCreate() {
        Ec2Client client = createClient(Ec2Client.class);

        CreateNetworkAclResponse response = client.createNetworkAcl(
            r -> r.vpcId(getVpcId())
        );

        setNetworkAclId(response.networkAcl().networkAclId());
    }

    @Override
    protected void doUpdate(AwsResource config, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteNetworkAcl(r -> r.networkAclId(getNetworkAclId()));
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        if (getNetworkAclId() != null) {
            sb.append(getNetworkAclId());

        } else {
            sb.append("network acl");
        }

        return sb.toString();
    }
}
