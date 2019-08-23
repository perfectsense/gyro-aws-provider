package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.elbv2.LoadBalancerResource;
import gyro.aws.elbv2.NetworkLoadBalancerResource;
import gyro.aws.iam.RoleResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AllowedPrincipal;
import software.amazon.awssdk.services.ec2.model.CreateVpcEndpointServiceConfigurationResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpcEndpointServiceConfigurationsResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpcEndpointServicePermissionsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.ModifyVpcEndpointServiceConfigurationRequest;
import software.amazon.awssdk.services.ec2.model.ModifyVpcEndpointServicePermissionsRequest;
import software.amazon.awssdk.services.ec2.model.ServiceConfiguration;
import software.amazon.awssdk.services.ec2.model.ServiceTypeDetail;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Create a vpc endpoint service.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::vpc-endpoint-service endpoint-service-example
 *         network-load-balancers: [
 *             $(aws::network-load-balancer nlb-example)
 *         ]
 *     end
 */
@Type("vpc-endpoint-service")
public class EndpointServiceResource extends Ec2TaggableResource<ServiceConfiguration> implements Copyable<ServiceConfiguration> {
    private Boolean acceptanceRequired;
    private Set<NetworkLoadBalancerResource> networkLoadBalancers;
    private Set<RoleResource> principals;

    private String id;
    private String name;
    private Set<String> availabilityZones;
    private Set<String> baseEndpointDnsNames;
    private String privateDnsName;
    private String state;
    private Boolean manageVpcEndpoints;
    private Set<EndpointServiceTypeDetail> serviceType;

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
     * A list of Network load Balancers. At least one is Required.
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
     * A list of IAM Roles.
     */
    @Updatable
    public Set<RoleResource> getPrincipals() {
        if (principals == null) {
            principals = new HashSet<>();
        }

        return principals;
    }

    public void setPrincipals(Set<RoleResource> principals) {
        this.principals = principals;
    }

    /**
     * The id of the endpoint service.
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
     * The name of the endpoint service.
     */
    @Output
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * A list of Availability zones of the endpoint service.
     */
    @Output
    public Set<String> getAvailabilityZones() {
        return availabilityZones;
    }

    public void setAvailabilityZones(Set<String> availabilityZones) {
        this.availabilityZones = availabilityZones;
    }

    /**
     * A list of base endpoint dns names.
     */
    @Output
    public Set<String> getBaseEndpointDnsNames() {
        return baseEndpointDnsNames;
    }

    public void setBaseEndpointDnsNames(Set<String> baseEndpointDnsNames) {
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

    /**
     * Is vpc endpoints managed.
     */
    @Output
    public Boolean getManageVpcEndpoints() {
        return manageVpcEndpoints;
    }

    public void setManageVpcEndpoints(Boolean manageVpcEndpoints) {
        this.manageVpcEndpoints = manageVpcEndpoints;
    }

    /**
     * A Set of service type.
     */
    @Output
    public Set<EndpointServiceTypeDetail> getServiceType() {
        if (serviceType == null) {
            serviceType = new HashSet<>();
        }

        return serviceType;
    }

    public void setServiceType(Set<EndpointServiceTypeDetail> serviceType) {
        this.serviceType = serviceType;
    }

    @Override
    protected String getResourceId() {
        return getId();
    }

    @Override
    public void copyFrom(ServiceConfiguration serviceConfiguration) {
        setId(serviceConfiguration.serviceId());
        setAcceptanceRequired(serviceConfiguration.acceptanceRequired());
        setName(serviceConfiguration.serviceName());
        setAvailabilityZones(serviceConfiguration.availabilityZones() != null ? new HashSet<>(serviceConfiguration.availabilityZones()) : null);
        setBaseEndpointDnsNames(serviceConfiguration.baseEndpointDnsNames() != null ? new HashSet<>(serviceConfiguration.baseEndpointDnsNames()) : null);
        setPrivateDnsName(serviceConfiguration.privateDnsName());
        setState(serviceConfiguration.serviceStateAsString());
        setManageVpcEndpoints(serviceConfiguration.managesVpcEndpoints());

        getServiceType().clear();
        for (ServiceTypeDetail serviceTypeDetail: serviceConfiguration.serviceType()) {
            EndpointServiceTypeDetail endpointServiceTypeDetail = newSubresource(EndpointServiceTypeDetail.class);
            endpointServiceTypeDetail.copyFrom(serviceTypeDetail);
            getServiceType().add(endpointServiceTypeDetail);
        }

        setNetworkLoadBalancers(
            serviceConfiguration.networkLoadBalancerArns()
                .stream()
                .filter(o -> !ObjectUtils.isBlank(o))
                .map(o -> findById(NetworkLoadBalancerResource.class, o))
                .collect(Collectors.toSet())
        );

        Ec2Client client = createClient(Ec2Client.class);

        DescribeVpcEndpointServicePermissionsResponse response = client.describeVpcEndpointServicePermissions(r -> r.serviceId(getId()));

        getPrincipals().clear();

        for (AllowedPrincipal allowedPrincipal: response.allowedPrincipals()) {
            getPrincipals().add(findById(RoleResource.class,allowedPrincipal.principal()));
        }

        refreshTags();
    }

    @Override
    public boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        ServiceConfiguration serviceConfiguration = getServiceConfiguration(client);

        if (serviceConfiguration == null) {
            return false;
        }

        copyFrom(serviceConfiguration);

        return true;
    }

    @Override
    public void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        CreateVpcEndpointServiceConfigurationResponse response = client.createVpcEndpointServiceConfiguration(
            r -> r.acceptanceRequired(getAcceptanceRequired())
                .networkLoadBalancerArns(getNetworkLoadBalancers().stream().map(LoadBalancerResource::getArn).collect(Collectors.toList()))
        );

        setId(response.serviceConfiguration().serviceId());

        if (!getPrincipals().isEmpty()) {
            client.modifyVpcEndpointServicePermissions(
                r -> r.serviceId(getId())
                    .addAllowedPrincipals(getPrincipals().stream().map(RoleResource::getArn).collect(Collectors.toList()))
            );
        }

        copyFrom(response.serviceConfiguration());
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        EndpointServiceResource currentEndpointService = (EndpointServiceResource) config;

        if (changedProperties.contains("acceptance-required") || changedProperties.contains("network-load-balancers")) {

            ModifyVpcEndpointServiceConfigurationRequest.Builder builder = ModifyVpcEndpointServiceConfigurationRequest.builder()
                .serviceId(getId());

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

        if (changedProperties.contains("principals")) {
            ModifyVpcEndpointServicePermissionsRequest.Builder builder = ModifyVpcEndpointServicePermissionsRequest.builder()
                .serviceId(getId());

            Set<String> currentIamArns = currentEndpointService.getPrincipals().stream().map(RoleResource::getArn).collect(Collectors.toSet());
            Set<String> pendingIamArns = getPrincipals().stream().map(RoleResource::getArn).collect(Collectors.toSet());

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
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteVpcEndpointServiceConfigurations(
            r -> r.serviceIds(getId())
        );
    }

    private ServiceConfiguration getServiceConfiguration(Ec2Client client) {
        ServiceConfiguration serviceConfiguration = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load endpoint service.");
        }

        try {
            DescribeVpcEndpointServiceConfigurationsResponse response = client.describeVpcEndpointServiceConfigurations(r -> r.serviceIds(getId()));

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
