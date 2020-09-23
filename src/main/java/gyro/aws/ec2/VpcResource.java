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

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsCredentials;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.TestValue;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AttributeBooleanValue;
import software.amazon.awssdk.services.ec2.model.ClassicLinkDnsSupport;
import software.amazon.awssdk.services.ec2.model.CreateVpcRequest;
import software.amazon.awssdk.services.ec2.model.CreateVpcResponse;
import software.amazon.awssdk.services.ec2.model.DeleteVpcRequest;
import software.amazon.awssdk.services.ec2.model.DescribeVpcAttributeRequest;
import software.amazon.awssdk.services.ec2.model.DescribeVpcClassicLinkDnsSupportResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpcClassicLinkResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpcsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.ModifyVpcAttributeRequest;
import software.amazon.awssdk.services.ec2.model.Vpc;
import software.amazon.awssdk.services.ec2.model.VpcAttributeName;
import software.amazon.awssdk.services.ec2.model.VpcClassicLink;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Creates a VPC with the specified IPv4 CIDR block.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::vpc example-vpc
 *         cidr-block: 10.0.0.0/16
 *         enable-classic-link: false
 *         enable-dns-hostnames: true
 *         enable-dns-support: true
 *     end
 */
@Type("vpc")
public class VpcResource extends Ec2TaggableResource<Vpc> implements Copyable<Vpc> {

    private String cidrBlock;
    private Boolean enableDnsHostnames;
    private Boolean enableDnsSupport;
    private DhcpOptionSetResource dhcpOptions;
    private String instanceTenancy;
    private Boolean enableClassicLink;
    private Boolean enableClassicLinkDnsSupport;
    private Boolean provideIpv6CidrBlock;

    // Read-only
    private String id;
    private String ownerId;
    private Boolean defaultVpc;
    private String region;
    private String account;

    /**
     * The IPv4 network range for the VPC, in CIDR notation.
     */
    @Required
    public String getCidrBlock() {
        return cidrBlock;
    }

    public void setCidrBlock(String cidrBlock) {
        this.cidrBlock = cidrBlock;
    }

    /**
     * Launch instances with public hostnames. Defaults to false. See `DNS Support in your VPC <https://docs.aws.amazon.com/vpc/latest/userguide/vpc-dns.html#vpc-dns-support>`_.
     */
    @Updatable
    public Boolean getEnableDnsHostnames() {
        if (enableDnsHostnames == null) {
            enableDnsHostnames = true;
        }

        return enableDnsHostnames;
    }

    public void setEnableDnsHostnames(Boolean enableDnsHostnames) {
        this.enableDnsHostnames = enableDnsHostnames;
    }

    /**
     * Enable Amazon provided DNS server at 169.254.169.253 or base of VPC network range plus two. Default is true. See `DNS Support in your VPC <https://docs.aws.amazon.com/vpc/latest/userguide/vpc-dns.html#vpc-dns-support>`_.
     */
    @Updatable
    public Boolean getEnableDnsSupport() {
        if (enableDnsSupport == null) {
            enableDnsSupport = true;
        }

        return enableDnsSupport;
    }

    public void setEnableDnsSupport(Boolean enableDnsSupport) {
        this.enableDnsSupport = enableDnsSupport;
    }

    /**
     * A custom DHCP option set. See `DHCP Options Sets <https://docs.aws.amazon.com/vpc/latest/userguide/VPC_DHCP_Options.html/>`_.
     */
    @Updatable
    public DhcpOptionSetResource getDhcpOptions() {
        return dhcpOptions;
    }

    public void setDhcpOptions(DhcpOptionSetResource dhcpOptions) {
        this.dhcpOptions = dhcpOptions;
    }

    /**
     * Set whether instances are launched on shared hardware (``default``) or dedicated hardware (``dedicated``). Can only be modified to ``default``.  See `Dedicated Instances <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/dedicated-instance.html/>`_.
     */
    @Updatable
    public String getInstanceTenancy() {
        return instanceTenancy;
    }

    public void setInstanceTenancy(String instanceTenancy) {
        this.instanceTenancy = instanceTenancy;
    }

    /**
     * Enable ClassLink to allow communication with EC2-Classic instances. Defaults to false. See `ClassicLink Basics <https://docs.aws.amazon.com/vpc/latest/userguide/vpc-classiclink.html/>`_.
     */
    @Updatable
    public Boolean getEnableClassicLink() {
        if (enableClassicLink == null) {
            enableClassicLink = false;
        }

        return enableClassicLink;
    }

    public void setEnableClassicLink(Boolean enableClassicLink) {
        this.enableClassicLink = enableClassicLink;
    }

    /**
     * Enable linked EC2-Classic instance hostnames to resolve to private IP address. Defaults to false. See `Enabling ClassicLink DNS Support <https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/vpc-classiclink.html?#classiclink-enable-dns-support/>`_.
     */
    @Updatable
    public Boolean getEnableClassicLinkDnsSupport() {
        if (enableClassicLinkDnsSupport == null) {
            enableClassicLinkDnsSupport = false;
        }

        return enableClassicLinkDnsSupport;
    }

    public void setEnableClassicLinkDnsSupport(Boolean enableClassicLinkDnsSupport) {
        this.enableClassicLinkDnsSupport = enableClassicLinkDnsSupport;
    }

    /**
     * The amazon provided ipv6 CIDR block.
     */
    public Boolean getProvideIpv6CidrBlock() {
        return provideIpv6CidrBlock;
    }

    public void setProvideIpv6CidrBlock(Boolean provideIpv6CidrBlock) {
        this.provideIpv6CidrBlock = provideIpv6CidrBlock;
    }

    /**
     * The ID of the VPC.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Is the current VPC default.
     */
    @Output
    public Boolean getDefaultVpc() {
        if (defaultVpc == null) {
            defaultVpc = false;
        }

        return defaultVpc;
    }

    public void setDefaultVpc(Boolean defaultVpc) {
        this.defaultVpc = defaultVpc;
    }

    /**
     * The owner ID.
     */
    @Output
    @TestValue("owner-12345")
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * The region where the VPC resides.
     */
    @Output
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * The account under which the VPC was created.
     */
    @Output
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public String getResourceId() {
        return getId();
    }

    @Override
    public void copyFrom(Vpc vpc) {
        setId(vpc.vpcId());
        setCidrBlock(vpc.cidrBlock());
        setInstanceTenancy(vpc.instanceTenancyAsString());
        setDhcpOptions(!ObjectUtils.isBlank(vpc.dhcpOptionsId()) ? findById(DhcpOptionSetResource.class,vpc.dhcpOptionsId()) : null);
        setOwnerId(vpc.ownerId());
        setDefaultVpc(vpc.isDefault());
        setProvideIpv6CidrBlock(!vpc.ipv6CidrBlockAssociationSet().isEmpty());

        Ec2Client client = createClient(Ec2Client.class);

        // DNS Settings
        DescribeVpcAttributeRequest request = DescribeVpcAttributeRequest.builder()
            .vpcId(id)
            .attribute(VpcAttributeName.ENABLE_DNS_HOSTNAMES)
            .build();
        setEnableDnsHostnames(client.describeVpcAttribute(request).enableDnsHostnames().value());

        request = DescribeVpcAttributeRequest.builder()
            .vpcId(id)
            .attribute(VpcAttributeName.ENABLE_DNS_SUPPORT)
            .build();
        setEnableDnsSupport(client.describeVpcAttribute(request).enableDnsSupport().value());

        // ClassicLink
        try {
            DescribeVpcClassicLinkResponse clResponse = client.describeVpcClassicLink(r -> r.vpcIds(getId()));
            for (VpcClassicLink classicLink : clResponse.vpcs()) {
                setEnableClassicLink(classicLink.classicLinkEnabled());
                break;
            }

            DescribeVpcClassicLinkDnsSupportResponse cldResponse = client.describeVpcClassicLinkDnsSupport(r -> r.vpcIds(getId()));
            for (ClassicLinkDnsSupport classicLink : cldResponse.vpcs()) {
                setEnableClassicLinkDnsSupport(classicLink.classicLinkDnsSupported());
                break;
            }
        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("not available in this region")) {
                throw ex;
            }
        }

        setRegion(credentials(AwsCredentials.class).getRegion());
        setAccount(getAccountNumber());

        refreshTags();
    }

    @Override
    public boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        Vpc vpc = getVpc(client);

        if (vpc == null) {
            return false;
        }

        copyFrom(vpc);

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        CreateVpcRequest request = CreateVpcRequest.builder()
                .cidrBlock(getCidrBlock())
                .amazonProvidedIpv6CidrBlock(getProvideIpv6CidrBlock())
                .instanceTenancy(getInstanceTenancy())
                .build();

        CreateVpcResponse response = client.createVpc(request);

        Vpc vpc = response.vpc();
        setId(response.vpc().vpcId());
        setOwnerId(vpc.ownerId());
        setInstanceTenancy(vpc.instanceTenancyAsString());
        setRegion(credentials(AwsCredentials.class).getRegion());

        modifySettings(client, new HashSet<>());
    }

    @Override
    public void testCreate(GyroUI ui, State state) throws Exception {
        super.testCreate(ui, state);

        setInstanceTenancy("default");
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource current, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        modifySettings(client, changedProperties);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        DeleteVpcRequest request = DeleteVpcRequest.builder()
                .vpcId(getId())
                .build();

        client.deleteVpc(request);
    }

    private Vpc getVpc(Ec2Client client) {
        Vpc vpc = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load vpc.");
        }

        try {
            DescribeVpcsResponse response = client.describeVpcs(r -> r.vpcIds(Collections.singleton(getId())));

            if (!response.vpcs().isEmpty()) {
                vpc = response.vpcs().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return vpc;
    }

    private void modifySettings(Ec2Client client, Set<String> changedProperties) {
        // DNS Settings
        if (changedProperties.isEmpty() || changedProperties.contains("enable-dns-hostnames")) {
            ModifyVpcAttributeRequest request = ModifyVpcAttributeRequest.builder()
                    .vpcId(getId())
                    .enableDnsHostnames(AttributeBooleanValue.builder().value(getEnableDnsHostnames()).build())
                    .build();

            client.modifyVpcAttribute(request);
        }

        if (changedProperties.isEmpty() || changedProperties.contains("enable-dns-support")) {
            ModifyVpcAttributeRequest request = ModifyVpcAttributeRequest.builder()
                    .vpcId(getId())
                    .enableDnsSupport(AttributeBooleanValue.builder().value(getEnableDnsSupport()).build())
                    .build();

            client.modifyVpcAttribute(request);

            request = ModifyVpcAttributeRequest.builder()
                    .vpcId(getId())
                    .enableDnsHostnames(AttributeBooleanValue.builder().value(getEnableDnsHostnames()).build())
                    .build();

            client.modifyVpcAttribute(request);
        }

        // DCHP Options
        if (changedProperties.isEmpty() || changedProperties.contains("dhcp-options")) {
            if (getDhcpOptions() != null) {
                client.associateDhcpOptions(r -> r.dhcpOptionsId(getDhcpOptions().getId()).vpcId(getId()));
            }
        }

        // ClassicLink
        if (changedProperties.isEmpty() || changedProperties.contains("enable-classic-link") || changedProperties.contains("enable-classic-link-dns-support")) {
            try {
                if (getEnableClassicLink()) {
                    client.enableVpcClassicLink(r -> r.vpcId(getId()));
                } else {
                    client.disableVpcClassicLink(r -> r.vpcId(getId()));
                }

                if (getEnableClassicLinkDnsSupport()) {
                    client.enableVpcClassicLinkDnsSupport(r -> r.vpcId(getId()));
                } else {
                    client.disableVpcClassicLinkDnsSupport(r -> r.vpcId(getId()));
                }
            } catch (Ec2Exception ex) {
                if (!ex.getLocalizedMessage().contains("not available in this region")) {
                    throw ex;
                }
            }
        }

        // Tenancy
        if (changedProperties.contains("instance-tenancy")) {
            if (!ObjectUtils.isBlank(getInstanceTenancy()) && getInstanceTenancy().equals("default")) {
                client.modifyVpcTenancy(r -> r.instanceTenancy(getInstanceTenancy()).vpcId(getId()));
            } else {
                throw new GyroException("'instance-tenancy' can only be modified to `default`.");
            }
        }
    }

    private String getAccountNumber() {
        StsClient client = createClient(StsClient.class);
        GetCallerIdentityResponse response = client.getCallerIdentity();
        return response.account();
    }

}
