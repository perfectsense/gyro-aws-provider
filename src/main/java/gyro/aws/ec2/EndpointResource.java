package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import org.apache.commons.lang.StringUtils;
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

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates an endpoint with the specified vpc and either route tables or subnets and security groups.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::endpoint endpoint-example-gateway
 *         vpc: $(aws::vpc vpc-example-for-endpoint)
 *         service-name: 'com.amazonaws.us-east-1.s3'
 *         policy-doc-path: 'policy.json'
 *         type-interface: false
 *         route-tables: [
 *             $(aws::route-table route-table-example-for-endpoint-1),
 *             $(aws::route-table route-table-example-for-endpoint-2),
 *             $(aws::route-table route-table-example-for-endpoint-3)
*          ]
 *     end
 *
 *     aws::endpoint endpoint-example-interface
 *         vpc-id: $(aws::vpc vpc-example-for-endpoint | vpc-id)
 *         service-name: 'com.amazonaws.us-east-1.ec2'
 *         policy-doc-path: 'policy.json'
 *         type-interface: true
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
@Type("endpoint")
public class EndpointResource extends AwsResource implements Copyable<VpcEndpoint> {

    private String endpointId;
    private String serviceName;
    private VpcResource vpc;
    private Boolean typeInterface;
    private Set<RouteTableResource> routeTables;
    private Set<SubnetResource> subnets;
    private Set<SecurityGroupResource> securityGroups;
    private Boolean enablePrivateDns;
    private String policyDocPath;
    private String policy;

    /**
     * The id of the endpoint.
     */
    @Id
    @Output
    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    /**
     * The name of the service that is going to associated with this Endpoint. (Required)
     */
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * The VPC to create the endpoint in. See `VPC Endpoints <https://docs.aws.amazon.com/vpc/latest/userguide/vpc-endpoints.html/>`_. (Required)
     */
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    /**
     * The type of service being associated is of type interface or gateway. (Required)
     */
    public Boolean getTypeInterface() {
        return typeInterface;
    }

    public void setTypeInterface(Boolean typeInterface) {
        this.typeInterface = typeInterface;
    }

    /**
     * The list of Route Table ID being associated with the Endpoint. (Required if typeInterface set to true.)
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
     * The list of Subnet ID being associated with the Endpoint. (Required if typeInterface set to false.)
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
     * The list of Security Group ID being associated with the Endpoint. (Required if typeInterface set to false.)
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
    public Boolean getEnablePrivateDns() {
        return enablePrivateDns;
    }

    public void setEnablePrivateDns(Boolean enablePrivateDns) {
        this.enablePrivateDns = enablePrivateDns;
    }

    /**
     * Path to the file that contains the policy.
     */
    @Updatable
    public String getPolicyDocPath() {
        return policyDocPath;
    }

    public void setPolicyDocPath(String policyDocPath) {
        this.policyDocPath = policyDocPath;

        if (policyDocPath != null) {
            setPolicyFromPath();
        }
    }

    /**
     * The content of the policy.
     */
    @Updatable
    public String getPolicy() {
        return policy != null ? policy.replaceAll(System.lineSeparator(), " ").replaceAll("\t", " ").trim().replaceAll(" ", "") : policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    @Override
    public void copyFrom(VpcEndpoint vpcEndpoint) {
        setVpc(findById(VpcResource.class, vpcEndpoint.vpcId()));
        setServiceName(vpcEndpoint.serviceName());
        setSecurityGroups(vpcEndpoint.groups().stream().map(o -> findById(SecurityGroupResource.class, o.groupId())).collect(Collectors.toSet()));
        setEndpointId(vpcEndpoint.vpcEndpointId());
        setTypeInterface(vpcEndpoint.vpcEndpointType().equals(VpcEndpointType.INTERFACE));
        setEnablePrivateDns(vpcEndpoint.privateDnsEnabled());
        setRouteTables(vpcEndpoint.routeTableIds().stream().map(o -> findById(RouteTableResource.class, o)).collect(Collectors.toSet()));
        setSubnets(vpcEndpoint.subnetIds().stream().map(o -> findById(SubnetResource.class, o)).collect(Collectors.toSet()));
        setPolicy(vpcEndpoint.policyDocument());
    }

    @Override
    public boolean refresh() {
        Ec2Client client = createClient(Ec2Client.class);

        VpcEndpoint endpoint = getVpcEndpoint(client);

        if (endpoint == null) {
            return false;
        }

        copyFrom(endpoint);

        return true;
    }

    @Override
    public void create() {

        validate();

        CreateVpcEndpointRequest.Builder builder = CreateVpcEndpointRequest.builder();

        builder.vpcId(getVpc().getVpcId());
        builder.vpcEndpointType(getTypeInterface() ? VpcEndpointType.INTERFACE : VpcEndpointType.GATEWAY);
        builder.privateDnsEnabled(getEnablePrivateDns());
        builder.serviceName(getServiceName());

        if (getTypeInterface()) {
            builder.subnetIds(getSubnets().isEmpty() ? null : getSubnets().stream().map(SubnetResource::getSubnetId).collect(Collectors.toList()));
            builder.securityGroupIds(getSecurityGroups().isEmpty() ? null : getSecurityGroups().stream().map(SecurityGroupResource::getGroupId).collect(Collectors.toList()));
        } else {
            builder.routeTableIds(getRouteTables().isEmpty() ? null : getRouteTables().stream().map(RouteTableResource::getRouteTableId).collect(Collectors.toList()));
            builder.policyDocument(getPolicy());
        }

        Ec2Client client = createClient(Ec2Client.class);

        CreateVpcEndpointResponse response = client.createVpcEndpoint(builder.build());

        VpcEndpoint endpoint = response.vpcEndpoint();

        setEndpointId(endpoint.vpcEndpointId());
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {

        validate();

        ModifyVpcEndpointRequest.Builder builder = ModifyVpcEndpointRequest.builder();
        builder.vpcEndpointId(getEndpointId());

        EndpointResource oldEndpoint = (EndpointResource) current;

        if (changedFieldNames.contains("route-tables")) {

            Set<String> currentRouteTableIds = oldEndpoint.getRouteTables().stream().map(RouteTableResource::getRouteTableId).collect(Collectors.toSet());
            Set<String> pendingRouteTableIds = getRouteTables().stream().map(RouteTableResource::getRouteTableId).collect(Collectors.toSet());

            List<String> removeRouteTableIds = currentRouteTableIds.stream().filter(o -> !pendingRouteTableIds.contains(o)).collect(Collectors.toList());
            List<String> addRouteTableIds = pendingRouteTableIds.stream().filter(o -> !currentRouteTableIds.contains(o)).collect(Collectors.toList());

            if (!addRouteTableIds.isEmpty()) {
                builder.addRouteTableIds(addRouteTableIds);
            }

            if (!removeRouteTableIds.isEmpty()) {
                builder.removeRouteTableIds(removeRouteTableIds);
            }
        }

        if (changedFieldNames.contains("subnets")) {
            Set<String> currentSubnetIds = oldEndpoint.getSubnets().stream().map(SubnetResource::getSubnetId).collect(Collectors.toSet());
            Set<String> pendingSubnetIds = getSubnets().stream().map(SubnetResource::getSubnetId).collect(Collectors.toSet());

            List<String> removeSubnetIds = currentSubnetIds.stream().filter(o -> !pendingSubnetIds.contains(o)).collect(Collectors.toList());
            List<String> addSubnetIds = pendingSubnetIds.stream().filter(o -> !currentSubnetIds.contains(o)).collect(Collectors.toList());

            if (!addSubnetIds.isEmpty()) {
                builder.addSubnetIds(addSubnetIds);
            }

            if (!removeSubnetIds.isEmpty()) {
                builder.removeSubnetIds(removeSubnetIds);
            }
        }

        if (changedFieldNames.contains("security-groups")) {
            Set<String> currentSecurityGroupIds = oldEndpoint.getSecurityGroups().stream().map(SecurityGroupResource::getGroupId).collect(Collectors.toSet());
            Set<String> pendingSecurityGroupIds = getSecurityGroups().stream().map(SecurityGroupResource::getGroupId).collect(Collectors.toSet());

            List<String> removeSecurityGroupIds = currentSecurityGroupIds.stream().filter(o -> !pendingSecurityGroupIds.contains(o)).collect(Collectors.toList());
            List<String> addSecurityGroupIds = pendingSecurityGroupIds.stream().filter(o -> !currentSecurityGroupIds.contains(o)).collect(Collectors.toList());

            if (!addSecurityGroupIds.isEmpty()) {
                builder.addSecurityGroupIds(addSecurityGroupIds);
            }

            if (!removeSecurityGroupIds.isEmpty()) {
                builder.removeSecurityGroupIds(removeSecurityGroupIds);
            }
        }

        if (changedFieldNames.contains("policy-doc-path")) {
            setPolicyFromPath();
            builder.policyDocument(getPolicy());
        }

        if (changedFieldNames.contains("policy")) {
            builder.policyDocument(getPolicy());
        }

        Ec2Client client = createClient(Ec2Client.class);

        client.modifyVpcEndpoint(builder.build());
    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteVpcEndpoints(
            r -> r.vpcEndpointIds(getEndpointId())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("endpoint");

        if (!StringUtils.isEmpty(getEndpointId())) {
            sb.append(" - ").append(getEndpointId());
        }

        if (getTypeInterface()) {
            sb.append(" [ Interface ]");
        } else {
            sb.append(" [ Gateway ]");
        }

        return sb.toString();
    }

    private void setPolicyFromPath() {
        try (InputStream input = openInput(getPolicyDocPath())) {
            setPolicy(IoUtils.toUtf8String(input));

        } catch (Exception ignore) {
            // ignore
        }
    }

    private void validate() {
        if (getTypeInterface()) {
            if (!getRouteTables().isEmpty()) {
                throw new GyroException("The param 'route-tables' cannot be set when the param 'type-interface' is set to 'True'");
            }
        } else {
            if (!getSecurityGroups().isEmpty()) {
                throw new GyroException("The param 'security-groups' cannot be set when the param 'type-interface' is set to 'False'");
            }

            if (!getSubnets().isEmpty()) {
                throw new GyroException("The param 'subnets' cannot be set when the param 'type-interface' is set to 'False'");
            }
        }
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
            Filter vpcIdFilter = Filter.builder().name("vpc-id").values(getVpc().getVpcId()).build();
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
