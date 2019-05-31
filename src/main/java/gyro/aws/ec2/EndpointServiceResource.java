package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.elbv2.LoadBalancerResource;
import gyro.aws.elbv2.NetworkLoadBalancerResource;
import gyro.aws.iam.IamRoleResource;
import gyro.core.GyroException;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AllowedPrincipal;
import software.amazon.awssdk.services.ec2.model.CreateVpcEndpointServiceConfigurationResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpcEndpointServiceConfigurationsResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpcEndpointServicePermissionsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.ModifyVpcEndpointServiceConfigurationRequest;
import software.amazon.awssdk.services.ec2.model.ModifyVpcEndpointServicePermissionsRequest;
import software.amazon.awssdk.services.ec2.model.ServiceConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Create an endpoint service.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::endpoint-service endpoint-service-example
 *         network-load-balancers: [
 *             $(aws::nlb nlb-example)
 *         ]
 *     end
 */
@Type("endpoint-service")
public class EndpointServiceResource extends AwsResource implements Copyable<ServiceConfiguration> {
    private Boolean acceptanceRequired;
    private Set<NetworkLoadBalancerResource> networkLoadBalancers;
    private Set<IamRoleResource> principlals;

    private String serviceId;
    private String serviceName;
    private List<String> availablityZones;
    private List<String> baseEndpointDnsNames;
    private String privateDnsName;
    private String state;

    /**
     * Require acceptance. Defaults to true.
     */
    @Updatable
    public Boolean getAcceptanceRequired() {
        if (acceptanceRequired == null) {
            acceptanceRequired = true;
        }

        return acceptanceRequired;
    }

    public void setAcceptanceRequired(Boolean acceptanceRequired) {
        this.acceptanceRequired = acceptanceRequired;
    }

    /**
     * A list of Network load Balancer's. At least one is Required.
     */
    @Updatable
    public Set<NetworkLoadBalancerResource> getNetworkLoadBalancers() {
        if (networkLoadBalancers == null) {
            networkLoadBalancers = new HashSet<>();
        }

        return networkLoadBalancers;
    }

    public void setNetworkLoadBalancers(Set<NetworkLoadBalancerResource> networkLoadBalancers) {
        this.networkLoadBalancers = networkLoadBalancers;
    }

    /**
     * A list of IAM Role's.
     */
    @Updatable
    public Set<IamRoleResource> getPrinciplals() {
        if (principlals == null) {
            principlals = new HashSet<>();
        }

        return principlals;
    }

    public void setPrinciplals(Set<IamRoleResource> principlals) {
        this.principlals = principlals;
    }

    /**
     * The id of the endpoint service.
     */
    @Id
    @Output
    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * The name of the endpoint service.
     */
    @Output
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * A list of Availability zones of the endpoint service.
     */
    @Output
    public List<String> getAvailablityZones() {
        return availablityZones;
    }

    public void setAvailablityZones(List<String> availablityZones) {
        this.availablityZones = availablityZones;
    }

    /**
     * A list of base endpoint dns names.
     */
    @Output
    public List<String> getBaseEndpointDnsNames() {
        return baseEndpointDnsNames;
    }

    public void setBaseEndpointDnsNames(List<String> baseEndpointDnsNames) {
        this.baseEndpointDnsNames = baseEndpointDnsNames;
    }

    /**
     * The private dns name of the endpoint service.
     */
    @Output
    public String getPrivateDnsName() {
        return privateDnsName;
    }

    public void setPrivateDnsName(String privateDnsName) {
        this.privateDnsName = privateDnsName;
    }

    /**
     * The state of the endpoint service.
     */
    @Output
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public boolean refresh() {
        Ec2Client client = createClient(Ec2Client.class);

        ServiceConfiguration serviceConfiguration = getServiceConfiguration(client);

        if (serviceConfiguration == null) {
            return false;
        }

        copyFrom(serviceConfiguration);

        return true;
    }

    @Override
    public void create() {
        Ec2Client client = createClient(Ec2Client.class);

        CreateVpcEndpointServiceConfigurationResponse response = client.createVpcEndpointServiceConfiguration(
            r -> r.acceptanceRequired(getAcceptanceRequired())
                .networkLoadBalancerArns(getNetworkLoadBalancers().stream().map(LoadBalancerResource::getArn).collect(Collectors.toList()))
        );

        setServiceId(response.serviceConfiguration().serviceId());

        if (!getPrinciplals().isEmpty()) {
            client.modifyVpcEndpointServicePermissions(
                r -> r.serviceId(getServiceId())
                    .addAllowedPrincipals(getPrinciplals().stream().map(IamRoleResource::getRoleArn).collect(Collectors.toList()))
            );
        }
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        Ec2Client client = createClient(Ec2Client.class);

        EndpointServiceResource currentEndpointService = (EndpointServiceResource) current;

        if (changedFieldNames.contains("acceptance-required") || changedFieldNames.contains("network-load-balancers")) {

            ModifyVpcEndpointServiceConfigurationRequest.Builder builder = ModifyVpcEndpointServiceConfigurationRequest.builder()
                .serviceId(getServiceId());

            builder.acceptanceRequired(getAcceptanceRequired());

            Set<String> currentNlbArns = currentEndpointService.getNetworkLoadBalancers().stream().map(LoadBalancerResource::getArn).collect(Collectors.toSet());
            Set<String> pendingNlbArns = getNetworkLoadBalancers().stream().map(LoadBalancerResource::getArn).collect(Collectors.toSet());

            Set<String> deleteNlbArns = new HashSet<>(currentNlbArns);
            deleteNlbArns.removeAll(pendingNlbArns);

            if (!deleteNlbArns.isEmpty()) {
                builder.removeNetworkLoadBalancerArns(deleteNlbArns);
            }

            Set<String> addNlbArns = new HashSet<>(pendingNlbArns);
            addNlbArns.removeAll(currentNlbArns);

            if (!currentNlbArns.isEmpty()) {
                builder.addNetworkLoadBalancerArns(addNlbArns);
            }

            client.modifyVpcEndpointServiceConfiguration(builder.build());
        }

        if (changedFieldNames.contains("principals")) {
            ModifyVpcEndpointServicePermissionsRequest.Builder builder = ModifyVpcEndpointServicePermissionsRequest.builder()
                .serviceId(getServiceId());

            Set<String> currentIamArns = currentEndpointService.getPrinciplals().stream().map(IamRoleResource::getRoleArn).collect(Collectors.toSet());
            Set<String> pendingIamArns = getPrinciplals().stream().map(IamRoleResource::getRoleArn).collect(Collectors.toSet());

            Set<String> deleteIamRoleArns = new HashSet<>(currentIamArns);
            deleteIamRoleArns.removeAll(pendingIamArns);

            boolean doUpdate = false;

            if (!deleteIamRoleArns.isEmpty()) {
                builder.removeAllowedPrincipals(deleteIamRoleArns);
                doUpdate = true;
            }

            Set<String> addIamRoleArns = new HashSet<>(pendingIamArns);
            addIamRoleArns.removeAll(currentIamArns);

            if (!addIamRoleArns.isEmpty()) {
                builder.addAllowedPrincipals(addIamRoleArns);
                doUpdate = true;
            }

            if (doUpdate) {
                client.modifyVpcEndpointServicePermissions(builder.build());
            }
        }
    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteVpcEndpointServiceConfigurations(
            r -> r.serviceIds(getServiceId())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("service endpoint");

        if (!ObjectUtils.isBlank(getServiceId())) {
            sb.append(" - ").append(getServiceId());
        }

        return sb.toString();
    }

    @Override
    public void copyFrom(ServiceConfiguration serviceConfiguration) {
        setServiceId(serviceConfiguration.serviceId());
        setAcceptanceRequired(serviceConfiguration.acceptanceRequired());
        setServiceName(serviceConfiguration.serviceName());
        setAvailablityZones(serviceConfiguration.availabilityZones());
        setBaseEndpointDnsNames(serviceConfiguration.baseEndpointDnsNames());
        setPrivateDnsName(serviceConfiguration.privateDnsName());
        setState(serviceConfiguration.serviceStateAsString());

        setNetworkLoadBalancers(
            serviceConfiguration.networkLoadBalancerArns()
                .stream()
                .map(o -> findById(NetworkLoadBalancerResource.class, o))
                .collect(Collectors.toSet())
        );

        Ec2Client client = createClient(Ec2Client.class);

        DescribeVpcEndpointServicePermissionsResponse response = client.describeVpcEndpointServicePermissions(r -> r.serviceId(getServiceId()));

        getPrinciplals().clear();

        for (AllowedPrincipal allowedPrincipal: response.allowedPrincipals()) {
            getPrinciplals().add(findById(IamRoleResource.class,allowedPrincipal.principal()));
        }
    }

    private ServiceConfiguration getServiceConfiguration(Ec2Client client) {
        ServiceConfiguration serviceConfiguration = null;

        if (ObjectUtils.isBlank(getServiceId())) {
            throw new GyroException("service-id is missing, unable to load endpoint service.");
        }

        try {
            DescribeVpcEndpointServiceConfigurationsResponse response = client.describeVpcEndpointServiceConfigurations(r -> r.serviceIds(getServiceId()));

            if (!response.serviceConfigurations().isEmpty()) {
                serviceConfiguration = response.serviceConfigurations().get(0);
            }
        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return serviceConfiguration;
    }
}
