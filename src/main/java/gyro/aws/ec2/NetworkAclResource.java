/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.ec2;

import java.util.LinkedHashSet;
import java.util.Set;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateNetworkAclResponse;
import software.amazon.awssdk.services.ec2.model.DescribeNetworkAclsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.NetworkAcl;
import software.amazon.awssdk.services.ec2.model.NetworkAclEntry;

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
    private Set<NetworkAclIngressRuleResource> ingressRule;
    private Set<NetworkAclEgressRuleResource> egressRule;

    // Read-only
    private String id;

    /**
     * The VPC to create the Network ACL in. See `Network ACLs <https://docs.aws.amazon.com/vpc/latest/userguide/vpc-network-acls.html/>`_.
     */
    @Required
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
    public void copyFrom(NetworkAcl networkAcl) {
        setId(networkAcl.networkAclId());
        setVpc(findById(VpcResource.class, networkAcl.vpcId()));

        getEgressRule().clear();
        getIngressRule().clear();
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

        refreshTags();
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
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        CreateNetworkAclResponse response = client.createNetworkAcl(
            r -> r.vpcId(getVpc().getId())
        );

        setId(response.networkAcl().networkAclId());
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteNetworkAcl(r -> r.networkAclId(getId()));
    }

    private NetworkAcl getNetworkAcl(Ec2Client client) {
        NetworkAcl networkAcl = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load security group.");
        }

        try {
            DescribeNetworkAclsResponse response = client.describeNetworkAcls(r -> r.networkAclIds(getId()));

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
