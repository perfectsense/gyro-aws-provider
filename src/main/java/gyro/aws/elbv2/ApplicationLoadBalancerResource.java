package gyro.aws.elbv2;

import gyro.core.diff.ResourceName;
import gyro.lang.Resource;

import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.AvailabilityZone;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateLoadBalancerResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerTypeEnum;

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
 *     aws::alb alb-example
 *         name: "alb-example"
 *         ip-address-type: "ipv4"
 *         scheme: "internal"
 *         security-groups: [
 *                 $(aws::security-group security-group | group-id)
 *             ]
 *         subnet-ids: [
 *                 $(aws::subnet subnet-us-east-2a | subnet-id),
 *                 $(aws::subnet subnet-us-east-2b | subnet-id)
 *             ]
 *         tags: {
 *                 Name: "alb-example"
 *             }
 *     end
 */

@ResourceName("alb")
public class ApplicationLoadBalancerResource extends LoadBalancerResource {

    private List<String> securityGroups;
    private List<String> subnetIds;

    /**
     *  List of security groups associated with the alb (Optional)
     */
    public List<String> getSecurityGroups() {
        if (securityGroups == null) {
            securityGroups = new ArrayList<>();
        }

        return securityGroups;
    }

    public void setSecurityGroups(List<String> securityGroups) {
        this.securityGroups = securityGroups;
    }

    /**
     *  List of subnets associated with the alb (Optional)
     */
    public List<String> getSubnetIds() {
        if (subnetIds == null) {
            subnetIds = new ArrayList<>();
        }

        return subnetIds;
    }

    public void setSubnetIds(List<String> subnetIds) {
        this.subnetIds = subnetIds;
    }

    @Override
    public boolean refresh() {
        LoadBalancer loadBalancer = super.internalRefresh();

        if (loadBalancer != null) {
            setSecurityGroups(loadBalancer.securityGroups());

            getSubnetIds().clear();
            for (AvailabilityZone az: loadBalancer.availabilityZones()) {
                getSubnetIds().add(az.subnetId());
            }

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
                .securityGroups(getSecurityGroups())
                .subnets(getSubnetIds())
                .type(LoadBalancerTypeEnum.APPLICATION)
        );

        setArn(response.loadBalancers().get(0).loadBalancerArn());
        setDnsName(response.loadBalancers().get(0).dnsName());

        super.create();
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        super.update(current, changedProperties);
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
