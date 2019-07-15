package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.scope.State;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateNetworkAclResponse;
import software.amazon.awssdk.services.ec2.model.DescribeNetworkAclsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.NetworkAcl;
import software.amazon.awssdk.services.ec2.model.NetworkAclEntry;

import java.util.LinkedHashSet;
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
 *         vpc: $(aws::vpc vpc-example-for-network-acl)
 *
 *         tags: {
 *             Name: "network-acl-example"
 *         }
 *     end
 */
@Type("network-acl")
public class NetworkAclResource extends Ec2TaggableResource<NetworkAcl> implements Copyable<NetworkAcl> {

    private VpcResource vpc;
    private String networkAclId;
    private Set<NetworkAclIngressRuleResource> ingressRule;
    private Set<NetworkAclEgressRuleResource> egressRule;

    /**
     * The VPC to create the Network ACL in. See `Network ACLs <https://docs.aws.amazon.com/vpc/latest/userguide/vpc-network-acls.html/>`_. (Required)
     */
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    /**
     * A set of ingress rules for the Network ACL.
     *
     * @subresource gyro.aws.ec2.NetworkAclIngressRuleResource
     */
    @Updatable
    public Set<NetworkAclIngressRuleResource> getIngressRule() {
        if (ingressRule == null) {
            ingressRule = new LinkedHashSet<>();
        }

        return ingressRule;
    }

    public void setIngressRule(Set<NetworkAclIngressRuleResource> ingressRule) {
        this.ingressRule = ingressRule;
    }

    /**
     * A list of egress rules for the Network ACL.
     *
     * @subresource gyro.aws.ec2.NetworkAclEgressRuleResource
     */
    @Updatable
    public Set<NetworkAclEgressRuleResource> getEgressRule() {
        if (egressRule == null) {
            egressRule = new LinkedHashSet<>();
        }

        return egressRule;
    }

    public void setEgressRule(Set<NetworkAclEgressRuleResource> egressRule) {
        this.egressRule = egressRule;
    }

    /**
     * The ID of the network ACL.
     */
    @Id
    @Output
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
    public void copyFrom(NetworkAcl networkAcl) {
        setNetworkAclId(networkAcl.networkAclId());

        for (NetworkAclEntry e: networkAcl.entries()) {

            if (e.ruleNumber().equals(32767) && e.protocol().equals("-1")
                && e.portRange() == null
                && e.cidrBlock().equals("0.0.0.0/0")) {
                continue;
            }

            if (e.egress()) {
                NetworkAclEgressRuleResource egressRule = newSubresource(NetworkAclEgressRuleResource.class);
                egressRule.copyFrom(e);
                getEgressRule().add(egressRule);
            } else {
                NetworkAclIngressRuleResource ingressRule = newSubresource(NetworkAclIngressRuleResource.class);
                ingressRule.copyFrom(e);
                getIngressRule().add(ingressRule);
            }
        }

        setVpc(findById(VpcResource.class, networkAcl.vpcId()));
    }

    @Override
    protected boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        NetworkAcl networkAcl = getNetworkAcl(client);

        if (networkAcl == null) {
            return false;
        }

        copyFrom(networkAcl);

        return true;
    }

    @Override
    protected void doCreate(State state) {
        Ec2Client client = createClient(Ec2Client.class);

        CreateNetworkAclResponse response = client.createNetworkAcl(
            r -> r.vpcId(getVpc().getVpcId())
        );

        setNetworkAclId(response.networkAcl().networkAclId());
    }

    @Override
    protected void doUpdate(AwsResource config, Set<String> changedProperties, State state) {

    }

    @Override
    public void delete(State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteNetworkAcl(r -> r.networkAclId(getNetworkAclId()));
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("network acl");

        if (getNetworkAclId() != null) {
            sb.append(" - ").append(getNetworkAclId());
        }

        return sb.toString();
    }

    private NetworkAcl getNetworkAcl(Ec2Client client) {
        NetworkAcl networkAcl = null;

        if (ObjectUtils.isBlank(getNetworkAclId())) {
            throw new GyroException("network-acl-id is missing, unable to load security group.");
        }

        try {
            DescribeNetworkAclsResponse response = client.describeNetworkAcls(r -> r.networkAclIds(getNetworkAclId()));

            if (!response.networkAcls().isEmpty()) {
                networkAcl = response.networkAcls().get(0);
            }
        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return networkAcl;
    }
}
