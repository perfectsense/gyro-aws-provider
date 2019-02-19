package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import gyro.core.diff.ResourceOutput;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateNetworkAclResponse;
import software.amazon.awssdk.services.ec2.model.DescribeNetworkAclsResponse;
import software.amazon.awssdk.services.ec2.model.NetworkAcl;
import software.amazon.awssdk.services.ec2.model.NetworkAclEntry;

import java.util.ArrayList;
import java.util.List;
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
    private List<NetworkAclRuleResource> rule;
    private Boolean egressRule;

    /**
     * The ID of the VPC to create the Network ACL in. See `Network ACLs <https://docs.aws.amazon.com/vpc/latest/userguide/vpc-network-acls.html/>`_. (Required)
     */
    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
    }

    /**
     * The ID of the network ACL.
     */
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

    /**
     * Rule entries for this resource
     *
     * @subresource beam.aws.ec2.NetworkAclRuleResource
     */
    @ResourceDiffProperty(nullable = true, subresource = true)
    public List<NetworkAclRuleResource> getRule() {
        if (rule == null) {
            rule = new ArrayList<>();
        }
        return rule;
    }

    public void setRule(List<NetworkAclRuleResource> ruleEntries) {
        this.rule = ruleEntries;
    }

    /**
     * Indicates whether this is an egress rule or ingress rule.
     */
    @ResourceDiffProperty
    public Boolean getEgressRule() {
        return egressRule;
    }

    public void setEgressRule(Boolean egressRule) {
        this.egressRule = egressRule;
    }

    @Override
    protected boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        DescribeNetworkAclsResponse response = client.describeNetworkAcls(r -> r.networkAclIds(getNetworkAclId()));

        if (!response.networkAcls().isEmpty()) {
            NetworkAcl networkAcl = response.networkAcls().get(0);

            for (NetworkAclEntry e: networkAcl.entries()) {

                if (e.ruleNumber().equals(32767) && e.protocol().equals("-1")
                    && e.portRange() == null
                    && e.cidrBlock().equals("0.0.0.0/0")) {
                    continue;
                }

                if (e.egress().equals(true)) {
                    setEgressRule(true);
                    NetworkAclRuleResource rule = new NetworkAclRuleResource(e);
                    getRule().add(rule);
                    rule.parent(this);
                    rule.setResourceCredentials(getResourceCredentials());
                } else {
                    setEgressRule(false);
                    NetworkAclRuleResource rule = new NetworkAclRuleResource(e);
                    getRule().add(rule);
                    rule.parent(this);
                    rule.setResourceCredentials(getResourceCredentials());
                }
            }
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
