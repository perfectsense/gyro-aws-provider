package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import gyro.core.diff.ResourceOutput;
import gyro.lang.Resource;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.AllowedPrincipal;
import software.amazon.awssdk.services.ec2.model.CreateVpcEndpointServiceConfigurationResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpcEndpointServiceConfigurationsResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpcEndpointServicePermissionsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.ServiceConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::vpc-endpoint-service vpc-endpoint-service-example
 *         acceptance-required: "false"
 *         network-load-balancer-arns: [
 *             $(aws::nlb nlb-example | load-balancer-arn)
 *         ]
 *         principals: [
 *             $(aws::iam-role example-role | role-arn)
 *         ]
 *     end
 */

@ResourceName("vpc-endpoint-service")
public class VpcEndpointServiceResource extends AwsResource {

    private Boolean acceptanceRequired;
    private List<String> networkLoadBalancerArns;
    private List<String> principals;
    private String serviceId;
    private String serviceName;

    /**
     *  Determines whether requests to create an endpoint to this service must be accepted
     */
    @ResourceDiffProperty(updatable = true)
    public Boolean getAcceptanceRequired() {
        return acceptanceRequired;
    }

    public void setAcceptanceRequired(Boolean acceptanceRequired) {
        this.acceptanceRequired = acceptanceRequired;
    }

    /**
     *  The load balancer arns connected to the service
     */
    @ResourceDiffProperty(updatable = true)
    public List<String> getNetworkLoadBalancerArns() {
        if (networkLoadBalancerArns == null) {
            networkLoadBalancerArns = new ArrayList<>();
        }

        return networkLoadBalancerArns;
    }

    public void setNetworkLoadBalancerArns(List<String> networkLoadBalancerArns) {
        this.networkLoadBalancerArns = networkLoadBalancerArns;
    }

    /**
     *  The service consumers connected to the service
     */
    @ResourceDiffProperty(updatable = true)
    public List<String> getPrincipals() {
        if (principals == null) {
            principals = new ArrayList<>();
        }

        return principals;
    }

    public void setPrincipals(List<String> principals) {
        this.principals = principals;
    }

    @ResourceOutput
    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public boolean refresh() {
        try {
            Ec2Client client = createClient(Ec2Client.class);
            DescribeVpcEndpointServiceConfigurationsResponse configurationResponse =
                    client.describeVpcEndpointServiceConfigurations(r -> r.serviceIds(getServiceId()));

            ServiceConfiguration config = configurationResponse.serviceConfigurations().get(0);
            setAcceptanceRequired(config.acceptanceRequired());
            setNetworkLoadBalancerArns(config.networkLoadBalancerArns());
            setServiceId(config.serviceId());

            DescribeVpcEndpointServicePermissionsResponse permissionsResponse =
                    client.describeVpcEndpointServicePermissions(r -> r.serviceId(getServiceId()));

            if (permissionsResponse != null) {
                getPrincipals().clear();
                for (AllowedPrincipal ap : permissionsResponse.allowedPrincipals()) {
                    getPrincipals().add(ap.principal());
                }
            }

            return true;

        } catch (Ec2Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public void create() {
        Ec2Client client = createClient(Ec2Client.class);
        CreateVpcEndpointServiceConfigurationResponse createResponse =
                client.createVpcEndpointServiceConfiguration(r -> r.acceptanceRequired(getAcceptanceRequired())
                                                            .networkLoadBalancerArns(getNetworkLoadBalancerArns()));

        setServiceId(createResponse.serviceConfiguration().serviceId());
        setServiceName(createResponse.serviceConfiguration().serviceName());

        client.modifyVpcEndpointServicePermissions(r -> r.addAllowedPrincipals(getPrincipals())
                                                        .serviceId(getServiceId()));
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        VpcEndpointServiceResource currentResource = (VpcEndpointServiceResource) current;

        List<String> associateLoadBalancers = new ArrayList<>(getNetworkLoadBalancerArns());
        associateLoadBalancers.removeAll(currentResource.getNetworkLoadBalancerArns());

        List<String> dissociateLoadBalancers = new ArrayList<>(currentResource.getNetworkLoadBalancerArns());
        dissociateLoadBalancers.removeAll(getNetworkLoadBalancerArns());

        if (!associateLoadBalancers.isEmpty()) {
            client.modifyVpcEndpointServiceConfiguration(r -> r.addNetworkLoadBalancerArns(associateLoadBalancers)
                    .serviceId(getServiceId())
                    .acceptanceRequired(getAcceptanceRequired()));
        }

        if (!dissociateLoadBalancers.isEmpty()) {
            client.modifyVpcEndpointServiceConfiguration(r -> r.removeNetworkLoadBalancerArns(dissociateLoadBalancers)
                    .serviceId(getServiceId())
                    .acceptanceRequired(getAcceptanceRequired()));
        }

        List<String> associatePrincipals = new ArrayList<>(getPrincipals());
        associatePrincipals.removeAll(currentResource.getPrincipals());

        List<String> dissociatePrincipals = new ArrayList<>(currentResource.getPrincipals());
        dissociatePrincipals.removeAll(getPrincipals());

        if (!associatePrincipals.isEmpty()) {
            client.modifyVpcEndpointServicePermissions(r -> r.addAllowedPrincipals(associatePrincipals)
                    .serviceId(getServiceId()));
        }

        if (!dissociatePrincipals.isEmpty()) {
            client.modifyVpcEndpointServicePermissions(r -> r.removeAllowedPrincipals(dissociatePrincipals)
                    .serviceId(getServiceId()));
        }
    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);
        client.deleteVpcEndpointServiceConfigurations(r -> r.serviceIds(getServiceId()));
    }

    @Override
    public String toDisplayString() {
        return "vpc endpoint service ";
    }
}
