package gyro.aws.elbv2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.aws.ec2.ElasticIpResource;
import gyro.aws.ec2.SubnetResource;
import gyro.core.GyroException;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Resource;

import gyro.core.scope.State;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.AvailabilityZone;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateLoadBalancerResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeLoadBalancersResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerAddress;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerNotFoundException;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerTypeEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.SubnetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::network-load-balancer nlb-example
 *         name: "nlb-example"
 *         ip-address-type: "ipv4"
 *         scheme: "internet-facing"
 *
 *         subnet-mapping
 *             subnet: $(aws::subnet subnet-us-east-2a)
 *         end
 *
 *         subnet-mapping
 *             subnet: $(aws::subnet subnet-us-east-2b)
 *         end
 *
 *         tags: {
 *             Name: "nlb-example"
 *         }
 *     end
 */
@Type("network-load-balancer")
public class NetworkLoadBalancerResource extends LoadBalancerResource implements Copyable<LoadBalancer> {

    private List<SubnetMappings> subnetMapping;

    /**
     * The list of subnet mappings associated with the nlb. (Required)
     *
     * @subresource gyro.aws.elbv2.SubnetMappingResource
     */
    public List<SubnetMappings> getSubnetMapping() {
        if (subnetMapping == null) {
            subnetMapping = new ArrayList<>();
        }

        return subnetMapping;
    }

    public void setSubnetMapping(List<SubnetMappings> subnetMapping) {
        this.subnetMapping = subnetMapping;
    }

    @Override
    public void copyFrom(LoadBalancer loadBalancer) {
        getSubnetMapping().clear();
        for (AvailabilityZone zone : loadBalancer.availabilityZones()) {
            SubnetMappings subnet = newSubresource(SubnetMappings.class);
            subnet.setSubnet(findById(SubnetResource.class, zone.subnetId()));

            for (LoadBalancerAddress address : zone.loadBalancerAddresses()) {
                subnet.setIpAddress(address.allocationId() != null ? findById(ElasticIpResource.class, address.allocationId()) : null);
            }

            getSubnetMapping().add(subnet);
        }
    }

    @Override
    public boolean refresh() {
        LoadBalancer loadBalancer = super.internalRefresh();

        if (loadBalancer != null) {

            this.copyFrom(loadBalancer);

            return true;
        }

        return false;
    }

    @Override
    public void create(State state) {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        
        CreateLoadBalancerResponse response = client.createLoadBalancer(r -> r.ipAddressType(getIpAddressType())
                .name(getName())
                .scheme(getScheme())
                .subnetMappings(toSubnetMappings())
                .type(LoadBalancerTypeEnum.NETWORK)
        );

        setArn(response.loadBalancers().get(0).loadBalancerArn());
        setDnsName(response.loadBalancers().get(0).dnsName());

        Wait.atMost(3, TimeUnit.MINUTES)
                .checkEvery(10, TimeUnit.SECONDS)
                .prompt(true)
                .until(() -> isActiveState(client));

        super.create(state);
    }

    @Override
    public void update(State state, Resource current, Set<String> changedFieldNames) {
        super.update(state, current, changedFieldNames);
    }

    @Override
    public void delete(State state) {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);

        super.delete(state);

        Wait.atMost(3, TimeUnit.MINUTES)
                .checkEvery(10, TimeUnit.SECONDS)
                .prompt(true)
                .until(() -> isDeleted(client));
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append("network load balancer " + getName());
        return sb.toString();
    }

    private List<SubnetMapping> toSubnetMappings() {
        List<SubnetMapping> subnetMappings = new ArrayList<>();

        for (SubnetMappings subMap : getSubnetMapping()) {
            subnetMappings.add(subMap.toSubnetMappings());
        }

        return subnetMappings;
    }

    private boolean isActiveState(ElasticLoadBalancingV2Client client) {
        String result = client.describeLoadBalancers(r -> r.loadBalancerArns(getArn())).loadBalancers().get(0).state().codeAsString();
        return result.equalsIgnoreCase("Active");
    }

    private boolean isDeleted(ElasticLoadBalancingV2Client client) {
        LoadBalancer loadBalancer = null;

        if (ObjectUtils.isBlank(getArn())) {
            throw new GyroException("the arn is missing, unable to load the load balancer.");
        }

        try {
            DescribeLoadBalancersResponse describeLoadBalancersResponse =
                    client.describeLoadBalancers(r -> r.loadBalancerArns(getArn()));

            if (!describeLoadBalancersResponse.loadBalancers().isEmpty()) {
                loadBalancer = describeLoadBalancersResponse.loadBalancers().get(0);
            }
        } catch (LoadBalancerNotFoundException ex) {
            // Ignore
        }

        return loadBalancer == null;
    }
}
