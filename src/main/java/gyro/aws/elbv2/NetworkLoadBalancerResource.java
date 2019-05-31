package gyro.aws.elbv2;

import gyro.aws.Copyable;
import gyro.aws.ec2.SubnetResource;
import gyro.core.Type;
import gyro.core.resource.Resource;

import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.AvailabilityZone;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateLoadBalancerResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerAddress;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerTypeEnum;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.SubnetMapping;

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
 *     aws::nlb nlb-example
 *         name: "nlb-example"
 *         ip-address-type: "ipv4"
 *         scheme: "internet-facing"
 *
 *         subnet-mapping
 *             subnet-id: $(aws::subnet subnet-us-east-2a | subnet-id)
 *         end
 *
 *         subnet-mapping
 *             subnet-id: $(aws::subnet subnet-us-east-2b | subnet-id)
 *         end
 *
 *         tags: {
 *                 Name: "nlb-example"
 *             }
 *     end
 */

@Type("nlb")
public class NetworkLoadBalancerResource extends LoadBalancerResource implements Copyable<LoadBalancer> {

    private List<SubnetMappings> subnetMapping;

    /**
     * The list of subnet mappings associated with the nlb (Required)
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
                subnet.setAllocationId(address.allocationId());
                subnet.setIpAddress(address.ipAddress());
            }

            getSubnetMapping().add(subnet);
        }
    }

    @Override
    public boolean refresh() {
        LoadBalancer loadBalancer = super.internalRefresh();

        if (loadBalancer != null) {

            super.copyFrom(loadBalancer);
            this.copyFrom(loadBalancer);

            return true;
        }

        return false;
    }

    @Override
    public void create() {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        
        CreateLoadBalancerResponse response = client.createLoadBalancer(r -> r.ipAddressType(getIpAddressType())
                .name(getName())
                .scheme(getScheme())
                .subnetMappings(toSubnetMappings())
                .type(LoadBalancerTypeEnum.NETWORK)
        );

        setArn(response.loadBalancers().get(0).loadBalancerArn());
        setDnsName(response.loadBalancers().get(0).dnsName());

        super.create();
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        super.update(current, changedFieldNames);
    }

    @Override
    public void delete() {
        super.delete();
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
}
