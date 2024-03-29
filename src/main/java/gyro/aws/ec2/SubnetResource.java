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

import java.util.Collections;
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
import software.amazon.awssdk.services.ec2.model.CreateSubnetRequest;
import software.amazon.awssdk.services.ec2.model.CreateSubnetResponse;
import software.amazon.awssdk.services.ec2.model.DescribeNetworkAclsResponse;
import software.amazon.awssdk.services.ec2.model.DescribeSubnetsRequest;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.ModifySubnetAttributeRequest;
import software.amazon.awssdk.services.ec2.model.NetworkAcl;
import software.amazon.awssdk.services.ec2.model.NetworkAclAssociation;
import software.amazon.awssdk.services.ec2.model.PrivateDnsNameOptionsOnLaunch;
import software.amazon.awssdk.services.ec2.model.ReplaceNetworkAclAssociationResponse;
import software.amazon.awssdk.services.ec2.model.Subnet;

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
    private String ipv6CidrBlock;
    private String availabilityZone;
    private Boolean mapPublicIpOnLaunch;
    private NetworkAclResource networkAcl;
    private String aclAssociationId;
    private String defaultAclId;
    private Boolean enableDns64;
    private Boolean enableResourceNameDnsAaaaRecordOnLaunch;
    private Boolean enableResourceNameDnsARecordOnLaunch;
    private Boolean assignIpv6AddressOnCreation;

    // Read-only
    private String id;

    /**
     * The VPC to create the Subnet in.
     */
    @Required
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    /**
     * The IPv4 network range for the Subnet, in CIDR notation.
     */
    @Required
    public String getCidrBlock() {
        return cidrBlock;
    }

    public void setCidrBlock(String cidrBlock) {
        this.cidrBlock = cidrBlock;
    }

    /**
     * The IPv6 network range for the subnet, in CIDR notation.
     */
    @Updatable
    public String getIpv6CidrBlock() {
        return ipv6CidrBlock;
    }

    public void setIpv6CidrBlock(String ipv6CidrBlock) {
        this.ipv6CidrBlock = ipv6CidrBlock;
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
     * When set to ``true``, DNS queries made to the Amazon-provided DNS Resolver in this subnet should return synthetic IPv6 addresses for IPv4-only destinations.
     */
    @Updatable
    public Boolean getEnableDns64() {
        return enableDns64;
    }

    public void setEnableDns64(Boolean enableDns64) {
        this.enableDns64 = enableDns64;
    }

    /**
     * When set to ``true``, responds to DNS queries for instance hostnames with DNS AAAA records.
     */
    @Updatable
    public Boolean getEnableResourceNameDnsAaaaRecordOnLaunch() {
        return enableResourceNameDnsAaaaRecordOnLaunch;
    }

    public void setEnableResourceNameDnsAaaaRecordOnLaunch(Boolean enableResourceNameDnsAaaaRecordOnLaunch) {
        this.enableResourceNameDnsAaaaRecordOnLaunch = enableResourceNameDnsAaaaRecordOnLaunch;
    }

    /**
     * When set to ``true``, responds to DNS queries for instance hostnames with DNS A records.
     */
    @Updatable
    public Boolean getEnableResourceNameDnsARecordOnLaunch() {
        return enableResourceNameDnsARecordOnLaunch;
    }

    public void setEnableResourceNameDnsARecordOnLaunch(Boolean enableResourceNameDnsARecordOnLaunch) {
        this.enableResourceNameDnsARecordOnLaunch = enableResourceNameDnsARecordOnLaunch;
    }

    /**
     * When set to ``true``, indicates that network interfaces created in the specified subnet should be assigned an IPv6 address.
     */
    @Updatable
    public Boolean getAssignIpv6AddressOnCreation() {
        return assignIpv6AddressOnCreation;
    }

    public void setAssignIpv6AddressOnCreation(Boolean assignIpv6AddressOnCreation) {
        this.assignIpv6AddressOnCreation = assignIpv6AddressOnCreation;
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
        setEnableDns64(subnet.enableDns64());
        setAssignIpv6AddressOnCreation(subnet.assignIpv6AddressOnCreation());
        setVpc(findById(VpcResource.class, subnet.vpcId()));

        PrivateDnsNameOptionsOnLaunch privateDnsNameOptionsOnLaunch = subnet.privateDnsNameOptionsOnLaunch();
        setEnableResourceNameDnsAaaaRecordOnLaunch(privateDnsNameOptionsOnLaunch.enableResourceNameDnsAAAARecord());
        setEnableResourceNameDnsARecordOnLaunch(privateDnsNameOptionsOnLaunch.enableResourceNameDnsARecord());

        if (subnet.hasIpv6CidrBlockAssociationSet() && !subnet.ipv6CidrBlockAssociationSet().isEmpty()) {
            setIpv6CidrBlock(subnet.ipv6CidrBlockAssociationSet().get(0).ipv6CidrBlock());
        }

        refreshTags();
    }

    @Override
    public boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load subnet.");
        }

        try {
            DescribeSubnetsRequest request = DescribeSubnetsRequest.builder()
                .subnetIds(getId())
                .build();

            client.describeSubnets(request).subnets().forEach(this::copyFrom);

            DescribeNetworkAclsResponse aclResponse = client.describeNetworkAcls(
                r -> r.filters(
                    Filter.builder().name("vpc-id").values(getVpc().getId()).build(),
                    Filter.builder().name("association.subnet-id").values(getId()).build()
                )
            );

            for (NetworkAcl acl : aclResponse.networkAcls()) {

                if (!acl.isDefault().equals(true)) {
                    setNetworkAcl(!ObjectUtils.isBlank(acl.networkAclId()) ?
                        findById(NetworkAclResource.class, acl.networkAclId()) : null);
                    if (!acl.associations().isEmpty()) {
                        acl.associations().stream()
                            .filter(a -> getId().equals(a.subnetId()))
                            .map(NetworkAclAssociation::networkAclAssociationId)
                            .forEach(this::setAclAssociationId);
                    }
                } else {
                    setDefaultAclId(acl.networkAclId());
                    setNetworkAcl(null);
                    if (!acl.associations().isEmpty()) {
                        acl.associations().stream()
                            .filter(a -> getId().equals(a.subnetId()))
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
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        CreateSubnetRequest request = CreateSubnetRequest.builder()
            .ipv6CidrBlock(getIpv6CidrBlock())
            .availabilityZone(getAvailabilityZone())
            .cidrBlock(getCidrBlock())
            .vpcId(getVpc().getId())
            .build();

        CreateSubnetResponse response = client.createSubnet(request);

        setId(response.subnet().subnetId());

        state.save();

        DescribeNetworkAclsResponse aclResponse = client.describeNetworkAcls(
            r -> r.filters(
                Filter.builder().name("vpc-id").values(getVpc().getId()).build(),
                Filter.builder().name("association.subnet-id").values(getId()).build()
            )
        );

        for (NetworkAcl acl : aclResponse.networkAcls()) {
            if (!acl.associations().isEmpty()) {
                setDefaultAclId(acl.networkAclId());
                acl.associations().stream()
                    .filter(a -> getId().equals(a.subnetId()))
                    .map(NetworkAclAssociation::networkAclAssociationId)
                    .forEach(this::setAclAssociationId);
            }
        }

        if (getNetworkAcl() != null) {
            ReplaceNetworkAclAssociationResponse replaceNetworkAclAssociationResponse =
                client.replaceNetworkAclAssociation(
                    r -> r.associationId(getAclAssociationId())
                        .networkAclId(getNetworkAcl().getId())
                );

            setAclAssociationId(replaceNetworkAclAssociationResponse.newAssociationId());
        }

        state.save();

        modifyAttribute(client, Collections.emptySet());
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource current, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        if (changedProperties.contains("network-acl")) {
            String acl = getNetworkAcl() != null ? getNetworkAcl().getId() : getDefaultAclId();
            ReplaceNetworkAclAssociationResponse replaceNetworkAclAssociationResponse =
                client.replaceNetworkAclAssociation(
                    r -> r.associationId(getAclAssociationId())
                        .networkAclId(acl)
                );

            setAclAssociationId(replaceNetworkAclAssociationResponse.newAssociationId());
        }

        if (changedProperties.contains("ipv6-cidr-block")) {
            client.associateSubnetCidrBlock(r -> r.ipv6CidrBlock(getIpv6CidrBlock()).subnetId(getId()));
        }

        modifyAttribute(client, changedProperties);
    }

    private void modifyAttribute(Ec2Client client, Set<String> changedProperties) {
        if ((changedProperties.isEmpty() && getMapPublicIpOnLaunch() != null) ||
            changedProperties.contains("map-public-ip-on-launch")) {
            client.modifySubnetAttribute(ModifySubnetAttributeRequest.builder()
                .subnetId(getId())
                .mapPublicIpOnLaunch(r -> r.value(Boolean.TRUE.equals(getMapPublicIpOnLaunch())))
                .build());
        }

        if ((changedProperties.isEmpty() && getEnableDns64() != null) || changedProperties.contains("enable-dns64")) {
            client.modifySubnetAttribute(ModifySubnetAttributeRequest.builder()
                .subnetId(getId())
                .enableDns64(r -> r.value(Boolean.TRUE.equals(getEnableDns64())))
                .build());
        }

        if ((changedProperties.isEmpty() && getEnableResourceNameDnsAaaaRecordOnLaunch() != null) ||
            changedProperties.contains("enable-resource-name-dns-aaaa-record-on-launch")) {
            client.modifySubnetAttribute(ModifySubnetAttributeRequest.builder()
                .subnetId(getId())
                .enableResourceNameDnsAAAARecordOnLaunch(
                    r -> r.value(Boolean.TRUE.equals(getEnableResourceNameDnsAaaaRecordOnLaunch())))
                .build());
        }

        if ((changedProperties.isEmpty() && getEnableResourceNameDnsARecordOnLaunch() != null) ||
            changedProperties.contains("enable-resource-name-dns-a-record-on-launch")) {
            client.modifySubnetAttribute(ModifySubnetAttributeRequest.builder()
                .subnetId(getId())
                .enableResourceNameDnsARecordOnLaunch(
                    r -> r.value(Boolean.TRUE.equals(getEnableResourceNameDnsARecordOnLaunch())))
                .build());
        }

        if ((changedProperties.isEmpty() && getAssignIpv6AddressOnCreation() != null) ||
            changedProperties.contains("assign-ipv6-address-on-creation")) {
            client.modifySubnetAttribute(ModifySubnetAttributeRequest.builder()
                .subnetId(getId())
                .assignIpv6AddressOnCreation(r -> r.value(Boolean.TRUE.equals(getAssignIpv6AddressOnCreation())))
                .build());
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteSubnet(r -> r.subnetId(getId()));
    }
}
