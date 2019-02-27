package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.core.BeamException;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import gyro.core.diff.ResourceOutput;
import gyro.lang.Resource;
import org.apache.commons.lang.StringUtils;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateVpcEndpointRequest;
import software.amazon.awssdk.services.ec2.model.CreateVpcEndpointResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpcEndpointsResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.ModifyVpcEndpointRequest;
import software.amazon.awssdk.services.ec2.model.SecurityGroupIdentifier;
import software.amazon.awssdk.services.ec2.model.VpcEndpoint;
import software.amazon.awssdk.services.ec2.model.VpcEndpointType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
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
 *         vpc-id: $(aws::vpc vpc-example-for-endpoint | vpc-id)
 *         service-name: 'com.amazonaws.us-east-1.s3'
 *         policy-doc-path: 'policy.json'
 *         type-interface: false
 *         route-table-ids: [
 *             $(aws::route-table route-table-example-for-endpoint-1 | route-table-id),
 *             $(aws::route-table route-table-example-for-endpoint-2 | route-table-id),
 *             $(aws::route-table route-table-example-for-endpoint-3 | route-table-id)
*          ]
 *     end
 *
 *     aws::endpoint endpoint-example-interface
 *         vpc-id: $(aws::vpc vpc-example-for-endpoint | vpc-id)
 *         service-name: 'com.amazonaws.us-east-1.ec2'
 *         policy-doc-path: 'policy.json'
 *         type-interface: true
 *         subnet-ids: [
 *             $(aws::subnet subnet-public-us-east-1a-example-for-endpoint-1 | subnet-id),
 *             $(aws::subnet subnet-public-us-east-1b-example-for-endpoint-1 | subnet-id),
 *             $(aws::subnet subnet-public-us-east-1c-example-for-endpoint-1 | subnet-id)
 *         ]
 *         security-group-ids: [
 *             $(aws::security-group security-group-example-for-endpoint-1 | group-id),
 *             $(aws::security-group security-group-example-for-endpoint-2 | group-id),
 *             $(aws::security-group security-group-example-for-endpoint-3 | group-id)
 *         ]
 *     end
 */
@ResourceName("endpoint")
public class EndpointResource extends AwsResource {

    private String endpointId;
    private String serviceName;
    private String vpcId;
    private Boolean typeInterface;
    private List<String> routeTableIds;
    private List<String> subnetIds;
    private List<String> securityGroupIds;
    private Boolean enablePrivateDns;
    private String policyDocPath;
    private String policy;

    @ResourceOutput
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
     * The ID of the VPC to create the endpoint in. See `VPC Endpoints <https://docs.aws.amazon.com/vpc/latest/userguide/vpc-endpoints.html/>`_. (Required)
     */
    public String getVpcId() {
        return vpcId;
    }

    public void setVpcId(String vpcId) {
        this.vpcId = vpcId;
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
    @ResourceDiffProperty(updatable = true, nullable = true)
    public List<String> getRouteTableIds() {
        if (routeTableIds == null) {
            routeTableIds = new ArrayList<>();
        }

        return routeTableIds;
    }

    public void setRouteTableIds(List<String> routeTableIds) {
        this.routeTableIds = routeTableIds;
    }

    /**
     * The list of Subnet ID being associated with the Endpoint. (Required if typeInterface set to false.)
     */
    @ResourceDiffProperty(updatable = true, nullable = true)
    public List<String> getSubnetIds() {
        if (subnetIds == null) {
            subnetIds = new ArrayList<>();
        }

        return subnetIds;
    }

    public void setSubnetIds(List<String> subnetIds) {
        this.subnetIds = subnetIds;
    }

    /**
     * The list of Security Group ID being associated with the Endpoint. (Required if typeInterface set to false.)
     */
    @ResourceDiffProperty(updatable = true)
    public List<String> getSecurityGroupIds() {
        if (securityGroupIds == null) {
            securityGroupIds = new ArrayList<>();
        }
        return securityGroupIds;
    }

    public void setSecurityGroupIds(List<String> securityGroupIds) {
        this.securityGroupIds = securityGroupIds;
    }

    /**
     * Enable private DNS on the Endpoint.
     */
    @ResourceDiffProperty(updatable = true)
    public Boolean getEnablePrivateDns() {
        return enablePrivateDns;
    }

    public void setEnablePrivateDns(Boolean enablePrivateDns) {
        this.enablePrivateDns = enablePrivateDns;
    }

    /**
     * Path to the file that contains the policy.
     */
    @ResourceDiffProperty(updatable = true)
    public String getPolicyDocPath() {
        return policyDocPath;
    }

    public void setPolicyDocPath(String policyDocPath) {
        this.policyDocPath = policyDocPath;

        if (policyDocPath != null) {
            setPolicyFromPath();
        }
    }

    @ResourceDiffProperty(updatable = true)
    public String getPolicy() {
        return policy != null ? policy.replaceAll(System.lineSeparator(), " ").replaceAll("\t", " ").trim().replaceAll(" ", "") : policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    @Override
    public boolean refresh() {
        Ec2Client client = createClient(Ec2Client.class);

        Filter serviceNameFilter = Filter.builder().name("service-name").values(getServiceName()).build();
        Filter vpcIdFilter = Filter.builder().name("vpc-id").values(getVpcId()).build();
        DescribeVpcEndpointsResponse response = client.describeVpcEndpoints(r -> r.maxResults(1).filters(serviceNameFilter, vpcIdFilter));

        if (!response.vpcEndpoints().isEmpty()) {
            VpcEndpoint endpoint = response.vpcEndpoints().get(0);
            setSecurityGroupIds(endpoint.groups().stream().map(SecurityGroupIdentifier::groupId).collect(Collectors.toList()));
            setEndpointId(endpoint.vpcEndpointId());
            setTypeInterface(endpoint.vpcEndpointType().equals(VpcEndpointType.INTERFACE));
            setEnablePrivateDns(endpoint.privateDnsEnabled());
            setRouteTableIds(new ArrayList<>(endpoint.routeTableIds()));
            setSubnetIds(new ArrayList<>(endpoint.subnetIds()));
            setPolicy(endpoint.policyDocument());

            return true;
        }

        return false;
    }

    @Override
    public void create() {

        validate();

        CreateVpcEndpointRequest.Builder builder = CreateVpcEndpointRequest.builder();

        builder.vpcId(getVpcId());
        builder.vpcEndpointType(getTypeInterface() ? VpcEndpointType.INTERFACE : VpcEndpointType.GATEWAY);
        builder.privateDnsEnabled(getEnablePrivateDns());
        builder.serviceName(getServiceName());

        if (getTypeInterface()) {
            builder.subnetIds(getSubnetIds().isEmpty() ? null : getSubnetIds());
            builder.securityGroupIds(getSecurityGroupIds().isEmpty() ? null : getSecurityGroupIds());
        } else {
            builder.routeTableIds(getRouteTableIds().isEmpty() ? null : getRouteTableIds());
            builder.policyDocument(getPolicy());
        }

        Ec2Client client = createClient(Ec2Client.class);

        CreateVpcEndpointResponse response = client.createVpcEndpoint(builder.build());

        VpcEndpoint endpoint = response.vpcEndpoint();

        setEndpointId(endpoint.vpcEndpointId());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

        validate();

        ModifyVpcEndpointRequest.Builder builder = ModifyVpcEndpointRequest.builder();
        builder.vpcEndpointId(getEndpointId());

        EndpointResource oldEndpoint = (EndpointResource) current;
        EndpointResource newEndpoint = this;


        if (changedProperties.contains("route-table-ids")) {
            List<String> removeRouteTableIds = oldEndpoint.getRouteTableIds().stream()
                .filter(f -> !newEndpoint.getRouteTableIds().contains(f)).collect(Collectors.toList());
            List<String> addRouteTableIds = newEndpoint.getRouteTableIds().stream()
                .filter(f -> !oldEndpoint.getRouteTableIds().contains(f)).collect(Collectors.toList());

            if (!addRouteTableIds.isEmpty()) {
                builder.addRouteTableIds(addRouteTableIds);
            }

            if (!removeRouteTableIds.isEmpty()) {
                builder.removeRouteTableIds(removeRouteTableIds);
            }
        }

        if (changedProperties.contains("subnet-ids")) {
            List<String> removeSubnetIds = oldEndpoint.getSubnetIds().stream()
                .filter(f -> !newEndpoint.getSubnetIds().contains(f)).collect(Collectors.toList());
            List<String> addSubnetIds = newEndpoint.getSubnetIds().stream()
                .filter(f -> !oldEndpoint.getSubnetIds().contains(f)).collect(Collectors.toList());

            if (!addSubnetIds.isEmpty()) {
                builder.addSubnetIds(addSubnetIds);
            }

            if (!removeSubnetIds.isEmpty()) {
                builder.removeSubnetIds(removeSubnetIds);
            }
        }

        if (changedProperties.contains("security-group-ids")) {
            List<String> removeSecurityGroupIds = oldEndpoint.getSecurityGroupIds().stream()
                .filter(f -> !newEndpoint.getSecurityGroupIds().contains(f)).collect(Collectors.toList());
            List<String> addSecurityGroupIds = newEndpoint.getSecurityGroupIds().stream()
                .filter(f -> !oldEndpoint.getSecurityGroupIds().contains(f)).collect(Collectors.toList());

            if (!addSecurityGroupIds.isEmpty()) {
                builder.addSecurityGroupIds(addSecurityGroupIds);
            }

            if (!removeSecurityGroupIds.isEmpty()) {
                builder.removeSecurityGroupIds(removeSecurityGroupIds);
            }
        }

        if (changedProperties.contains("policy-doc-path")) {
            setPolicyFromPath();
            builder.policyDocument(getPolicy());
        }

        if (changedProperties.contains("policy")) {
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
        try {
            String dir = scope().getFileScope().getFile().substring(0, scope().getFileScope().getFile().lastIndexOf(File.separator));
            setPolicy(new String(Files.readAllBytes(Paths.get(dir + File.separator + getPolicyDocPath())), StandardCharsets.UTF_8));
        } catch (IOException ioex) {
            throw new BeamException(MessageFormat
                .format("Endpoint - {0} policy error. Unable to read policy from path [{1}]", getServiceName(), getPolicyDocPath()));
        }
    }

    private void validate() {
        if (getTypeInterface()) {
            if (!getRouteTableIds().isEmpty()) {
                throw new BeamException("The param 'route-table-ids' cannot be set when the param 'type-interface' is set to 'True'");
            }
        } else {
            if (!getSecurityGroupIds().isEmpty()) {
                throw new BeamException("The param 'security-group-ids' cannot be set when the param 'type-interface' is set to 'False'");
            }

            if (!getSubnetIds().isEmpty()) {
                throw new BeamException("The param 'subnet-ids' cannot be set when the param 'type-interface' is set to 'False'");
            }
        }
    }
}
