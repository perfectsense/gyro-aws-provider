package gyro.aws.elbv2;

import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.ec2.SubnetResource;
import gyro.core.Type;
import gyro.core.resource.Resource;

import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateLoadBalancerResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerTypeEnum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::alb alb-example
 *         name: "alb-example"
 *         ip-address-type: "ipv4"
 *         scheme: "internal"
 *         security-groups: [
 *                 $(aws::security-group security-group)
 *             ]
 *         subnets: [
 *                 $(aws::subnet subnet-us-east-2a),
 *                 $(aws::subnet subnet-us-east-2b)
 *             ]
 *         tags: {
 *                 Name: "alb-example"
 *             }
 *     end
 */

@Type("alb")
public class ApplicationLoadBalancerResource extends LoadBalancerResource {

    private List<SecurityGroupResource> securityGroups;
    private Set<SubnetResource> subnets;

    /**
     *  List of security groups associated with the alb (Optional)
     */
    public List<SecurityGroupResource> getSecurityGroups() {
        if (securityGroups == null) {
            securityGroups = new ArrayList<>();
        }

        return securityGroups;
    }

    public void setSecurityGroups(List<SecurityGroupResource> securityGroups) {
        this.securityGroups = securityGroups;
    }

    /**
     *  List of subnets associated with the alb (Optional)
     */
    public Set<SubnetResource> getSubnets() {
        if (subnets == null) {
            subnets = new HashSet<>();
        }

        return subnets;
    }

    public void setSubnets(Set<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    @Override
    public boolean refresh() {
        LoadBalancer loadBalancer = super.internalRefresh();

        if (loadBalancer != null) {
            getSecurityGroups().clear();
            loadBalancer.securityGroups().forEach(r -> getSecurityGroups().add(findById(SecurityGroupResource.class, r)));

            getSubnets().clear();
            loadBalancer.availabilityZones().forEach(az -> getSubnets().add(findById(SubnetResource.class, az.subnetId())));

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
                .securityGroups(getSecurityGroups().stream().map(SecurityGroupResource::getGroupId).collect(Collectors.toList()))
                .subnets(getSubnets().stream().map(SubnetResource::getSubnetId).collect(Collectors.toList()))
                .type(LoadBalancerTypeEnum.APPLICATION)
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
        sb.append("application load balancer - " + getName());
        return sb.toString();
    }
}
