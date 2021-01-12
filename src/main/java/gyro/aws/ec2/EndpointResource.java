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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateVpcEndpointRequest;
import software.amazon.awssdk.services.ec2.model.CreateVpcEndpointResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpcEndpointsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.ModifyVpcEndpointRequest;
import software.amazon.awssdk.services.ec2.model.VpcEndpoint;
import software.amazon.awssdk.services.ec2.model.VpcEndpointType;
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a vpc endpoint with the specified vpc and either route tables or subnets and security groups.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::vpc-endpoint endpoint-example-gateway
 *         vpc: $(aws::vpc vpc-example-for-endpoint)
 *         service-name: 'com.amazonaws.us-east-1.s3'
 *         policy: 'policy.json'
 *         type: 'Gateway'
 *         route-tables: [
 *             $(aws::route-table route-table-example-for-endpoint-1),
 *             $(aws::route-table route-table-example-for-endpoint-2),
 *             $(aws::route-table route-table-example-for-endpoint-3)
 *          ]
 *     end
 *
 *     aws::vpc-endpoint endpoint-example-interface
 *         vpc: $(aws::vpc vpc-example-for-endpoint)
 *         service-name: 'com.amazonaws.us-east-1.ec2'
 *         policy: 'policy.json'
 *         type: 'Interface'
 *         subnets: [
 *             $(aws::subnet subnet-public-us-east-1a-example-for-endpoint-1),
 *             $(aws::subnet subnet-public-us-east-1b-example-for-endpoint-1),
 *             $(aws::subnet subnet-public-us-east-1c-example-for-endpoint-1)
 *         ]
 *         security-groups: [
 *             $(aws::security-group security-group-example-for-endpoint-1),
 *             $(aws::security-group security-group-example-for-endpoint-2),
 *             $(aws::security-group security-group-example-for-endpoint-3)
 *         ]
 *     end
 */
@Type("vpc-endpoint")
public class EndpointResource extends Ec2TaggableResource<VpcEndpoint> implements Copyable<VpcEndpoint> {

    private String id;
    private String serviceName;
    private VpcResource vpc;
    private VpcEndpointType type;
    private Set<RouteTableResource> routeTables;
    private Set<SubnetResource> subnets;
    private Set<SecurityGroupResource> securityGroups;
    private Boolean privateDnsEnabled;
    private String policy;

    private String state;
    private Date createTime;
    private Set<NetworkInterfaceResource> networkInterfaces;
    private Set<DnsEntry> dnsEntries;
    private Boolean requesterManaged;

    /**
     * The ID of the endpoint.
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
     * The name of the service that is going to associated with this Endpoint.
     */
    @Required
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * The VPC to create the endpoint in. See `VPC Endpoints <https://docs.aws.amazon.com/vpc/latest/userguide/vpc-endpoints.html>`_.
     */
    @Required
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    /**
     * The type of service being associated. Defaults to ``GATEWAY``.
     */
    @ValidStrings({"Interface", "Gateway"})
    public VpcEndpointType getType() {
        if (type == null) {
            type = VpcEndpointType.GATEWAY;
        }

        return type;
    }

    public void setType(VpcEndpointType type) {
        this.type = type;
    }

    /**
     * The set of Route Tables being associated with the Endpoint. (Required if ``type-interface`` set to true.)
     */
    @Updatable
    public Set<RouteTableResource> getRouteTables() {
        if (routeTables == null) {
            routeTables = new HashSet<>();
        }

        return routeTables;
    }

    public void setRouteTables(Set<RouteTableResource> routeTables) {
        this.routeTables = routeTables;
    }

    /**
     * The set of Subnets being associated with the Endpoint. (Required if ``type-interface`` set to false.)
     */
    @Updatable
    public Set<SubnetResource> getSubnets() {
        if (subnets == null) {
            subnets = new HashSet<>();
        }

        return subnets;
    }

    public void setSubnets(Set<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    /**
     * The set of of Security Groups being associated with the Endpoint. (Required if ``type-interface`` set to false.)
     */
    @Updatable
    public Set<SecurityGroupResource> getSecurityGroups() {
        if (securityGroups == null) {
            securityGroups = new HashSet<>();
        }

        return securityGroups;
    }

    public void setSecurityGroups(Set<SecurityGroupResource> securityGroups) {
        this.securityGroups = securityGroups;
    }

    /**
     * Enable private DNS on the Endpoint.
     */
    @Updatable
    public Boolean getPrivateDnsEnabled() {
        return privateDnsEnabled;
    }

    public void setPrivateDnsEnabled(Boolean privateDnsEnabled) {
        this.privateDnsEnabled = privateDnsEnabled;
    }

    /**
     * The content of the policy.
     */
    @Updatable
    public String getPolicy() {
        policy = getProcessedPolicy(policy);
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    /**
     * The state of the Endpoint.
     */
    @Output
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * The creation time of the Endpoint.
     */
    @Output
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * A set of Network Interface attached to the Endpoint.
     */
    @Output
    public Set<NetworkInterfaceResource> getNetworkInterfaces() {
        if (networkInterfaces == null) {
            networkInterfaces = new HashSet<>();
        }

        return networkInterfaces;
    }

    public void setNetworkInterfaces(Set<NetworkInterfaceResource> networkInterfaces) {
        this.networkInterfaces = networkInterfaces;
    }

    /**
     * A set of Dns Entry attached to the Endpoint.
     */
    @Output
    public Set<DnsEntry> getDnsEntries() {
        if (dnsEntries == null) {
            dnsEntries = new HashSet<>();
        }

        return dnsEntries;
    }

    /**
     * Is the requester managed.
     */
    @Output
    public Boolean getRequesterManaged() {
        return requesterManaged;
    }

    public void setRequesterManaged(Boolean requesterManaged) {
        this.requesterManaged = requesterManaged;
    }

    public void setDnsEntries(Set<DnsEntry> dnsEntries) {
        this.dnsEntries = dnsEntries;
    }

    private String getProcessedPolicy(String policy) {
        if (policy == null) {
            return null;
        } else if (policy.endsWith(".json")) {
            try (InputStream input = openInput(policy)) {
                policy = IoUtils.toUtf8String(input);

            } catch (IOException ex) {
                throw new GyroException(String.format("File at path '%s' not found.", policy));
            }
        }

        ObjectMapper obj = new ObjectMapper();
        try {
            JsonNode jsonNode = obj.readTree(policy);
            return jsonNode.toString();
        } catch (IOException ex) {
            throw new GyroException(String.format("Could not read the json `%s`",policy),ex);
        }
    }

    @Override
    public void copyFrom(VpcEndpoint vpcEndpoint) {
        setVpc(findById(VpcResource.class, vpcEndpoint.vpcId()));
        setServiceName(vpcEndpoint.serviceName());
        setSecurityGroups(vpcEndpoint.groups().stream().map(o -> findById(SecurityGroupResource.class, o.groupId())).collect(Collectors.toSet()));
        setId(vpcEndpoint.vpcEndpointId());
        setType(vpcEndpoint.vpcEndpointType());
        setPrivateDnsEnabled(vpcEndpoint.privateDnsEnabled());
        setRouteTables(vpcEndpoint.routeTableIds().stream().map(o -> findById(RouteTableResource.class, o)).collect(Collectors.toSet()));
        setSubnets(vpcEndpoint.subnetIds().stream().map(o -> findById(SubnetResource.class, o)).collect(Collectors.toSet()));
        setPolicy(vpcEndpoint.policyDocument());

        setState(vpcEndpoint.stateAsString());
        setCreateTime(Date.from(vpcEndpoint.creationTimestamp()));
        setNetworkInterfaces(vpcEndpoint.networkInterfaceIds().stream().map(o -> findById(NetworkInterfaceResource.class, o)).collect(Collectors.toSet()));

        setRequesterManaged(vpcEndpoint.requesterManaged());

        getDnsEntries().clear();
        for (software.amazon.awssdk.services.ec2.model.DnsEntry dnsEntry : vpcEndpoint.dnsEntries()) {
            gyro.aws.ec2.DnsEntry entry = newSubresource(gyro.aws.ec2.DnsEntry.class);
            entry.copyFrom(dnsEntry);
            getDnsEntries().add(entry);
        }

        refreshTags();
    }

    @Override
    protected String getResourceId() {
        return getId();
    }

    @Override
    public boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        VpcEndpoint endpoint = getVpcEndpoint(client);

        if (endpoint == null) {
            return false;
        }

        copyFrom(endpoint);

        return true;
    }

    @Override
    public void doCreate(GyroUI ui, State state) {

        validate();

        CreateVpcEndpointRequest.Builder builder = CreateVpcEndpointRequest.builder();

        builder.vpcId(getVpc().getId());
        builder.vpcEndpointType(getType());
        builder.privateDnsEnabled(getPrivateDnsEnabled());
        builder.serviceName(getServiceName());

        if (getType().equals(VpcEndpointType.INTERFACE)) {
            builder.subnetIds(getSubnets().isEmpty() ? null : getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList()));
            builder.securityGroupIds(getSecurityGroups().isEmpty() ? null : getSecurityGroups().stream().map(SecurityGroupResource::getId).collect(Collectors.toList()));
        } else {
            builder.routeTableIds(getRouteTables().isEmpty() ? null : getRouteTables().stream().map(RouteTableResource::getId).collect(Collectors.toList()));
            builder.policyDocument(getPolicy());
        }

        Ec2Client client = createClient(Ec2Client.class);

        CreateVpcEndpointResponse response = client.createVpcEndpoint(builder.build());

        VpcEndpoint endpoint = response.vpcEndpoint();

        setId(endpoint.vpcEndpointId());

        copyFrom(getVpcEndpoint(client));
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {
        validate();

        ModifyVpcEndpointRequest.Builder builder = ModifyVpcEndpointRequest.builder();
        builder.vpcEndpointId(getId());

        EndpointResource oldEndpoint = (EndpointResource) config;

        if (changedProperties.contains("route-tables")) {

            Set<String> currentRouteTableIds = oldEndpoint.getRouteTables().stream().map(RouteTableResource::getId).collect(Collectors.toSet());
            Set<String> pendingRouteTableIds = getRouteTables().stream().map(RouteTableResource::getId).collect(Collectors.toSet());

            List<String> removeRouteTableIds = currentRouteTableIds.stream().filter(o -> !pendingRouteTableIds.contains(o)).collect(Collectors.toList());
            List<String> addRouteTableIds = pendingRouteTableIds.stream().filter(o -> !currentRouteTableIds.contains(o)).collect(Collectors.toList());

            if (!addRouteTableIds.isEmpty()) {
                builder.addRouteTableIds(addRouteTableIds);
            }

            if (!removeRouteTableIds.isEmpty()) {
                builder.removeRouteTableIds(removeRouteTableIds);
            }
        }

        if (changedProperties.contains("subnets")) {
            Set<String> currentSubnetIds = oldEndpoint.getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toSet());
            Set<String> pendingSubnetIds = getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toSet());

            List<String> removeSubnetIds = currentSubnetIds.stream().filter(o -> !pendingSubnetIds.contains(o)).collect(Collectors.toList());
            List<String> addSubnetIds = pendingSubnetIds.stream().filter(o -> !currentSubnetIds.contains(o)).collect(Collectors.toList());

            if (!addSubnetIds.isEmpty()) {
                builder.addSubnetIds(addSubnetIds);
            }

            if (!removeSubnetIds.isEmpty()) {
                builder.removeSubnetIds(removeSubnetIds);
            }
        }

        if (changedProperties.contains("security-groups")) {
            Set<String> currentSecurityGroupIds = oldEndpoint.getSecurityGroups().stream().map(SecurityGroupResource::getId).collect(Collectors.toSet());
            Set<String> pendingSecurityGroupIds = getSecurityGroups().stream().map(SecurityGroupResource::getId).collect(Collectors.toSet());

            List<String> removeSecurityGroupIds = currentSecurityGroupIds.stream().filter(o -> !pendingSecurityGroupIds.contains(o)).collect(Collectors.toList());
            List<String> addSecurityGroupIds = pendingSecurityGroupIds.stream().filter(o -> !currentSecurityGroupIds.contains(o)).collect(Collectors.toList());

            if (!addSecurityGroupIds.isEmpty()) {
                builder.addSecurityGroupIds(addSecurityGroupIds);
            }

            if (!removeSecurityGroupIds.isEmpty()) {
                builder.removeSecurityGroupIds(removeSecurityGroupIds);
            }
        }

        if (changedProperties.contains("policy-doc-path")) {
            builder.policyDocument(getPolicy());
        }

        if (changedProperties.contains("policy")) {
            builder.policyDocument(getPolicy());
        }

        Ec2Client client = createClient(Ec2Client.class);

        client.modifyVpcEndpoint(builder.build());
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteVpcEndpoints(
            r -> r.vpcEndpointIds(getId())
        );

        // Delay for residual dependency to be gone. 2 Min
        try {
            Thread.sleep(120000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if (getType().equals(VpcEndpointType.INTERFACE)) {
            if (!getRouteTables().isEmpty()) {
                errors.add(new ValidationError(this, null, "The param 'route-tables' cannot be set when the param 'type' is set to 'Interface'"));
            }
        } else {
            if (!getSecurityGroups().isEmpty()) {
                errors.add(new ValidationError(this, null, "The param 'security-groups' cannot be set when the param 'type' is set to 'Gateway'"));
            }

            if (!getSubnets().isEmpty()) {
                errors.add(new ValidationError(this, null, "The param 'subnets' cannot be set when the param 'type' is set to 'Gateway'"));
            }
        }

        return errors;
    }

    private VpcEndpoint getVpcEndpoint(Ec2Client client) {
        VpcEndpoint vpcEndpoint = null;

        if (ObjectUtils.isBlank(getServiceName())) {
            throw new GyroException("service-name is missing, unable to load endpoint.");
        }

        if (getVpc() == null) {
            throw new GyroException("vpc is missing, unable to load endpoint.");
        }

        try {
            Filter serviceNameFilter = Filter.builder().name("service-name").values(getServiceName()).build();
            Filter vpcIdFilter = Filter.builder().name("vpc-id").values(getVpc().getId()).build();
            DescribeVpcEndpointsResponse response = client.describeVpcEndpoints(r -> r.maxResults(1).filters(serviceNameFilter, vpcIdFilter));

            if (!response.vpcEndpoints().isEmpty()) {
                vpcEndpoint = response.vpcEndpoints().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return vpcEndpoint;
    }
}
