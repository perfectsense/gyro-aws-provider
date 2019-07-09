package gyro.aws.elb;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.ec2.InstanceResource;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.ec2.SubnetResource;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;

import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.elasticloadbalancing.ElasticLoadBalancingClient;
import software.amazon.awssdk.services.elasticloadbalancing.model.CreateLoadBalancerResponse;
import software.amazon.awssdk.services.elasticloadbalancing.model.DescribeLoadBalancerAttributesResponse;
import software.amazon.awssdk.services.elasticloadbalancing.model.DescribeLoadBalancersResponse;
import software.amazon.awssdk.services.elasticloadbalancing.model.Instance;
import software.amazon.awssdk.services.elasticloadbalancing.model.Listener;
import software.amazon.awssdk.services.elasticloadbalancing.model.ListenerDescription;
import software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerDescription;
import software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerNotFoundException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Create a Load Balancer.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::load-balancer elb
 *         load-balancer-name: "elb"
 *         security-groups: [
 *             $(aws::security-group security-group)
 *         ]
 *         subnets: [
 *             $(aws::subnet subnet-us-east-2a)
 *         ]
 *         instances: [
 *             $(aws::instance instance-us-east-2a),
 *             $(aws::instance instance-us-east-2b)
 *         ]
 *
 *         listener
 *             instance-port: "443"
 *             instance-protocol: "HTTP"
 *             load-balancer-port: "443"
 *             protocol: "HTTP"
 *         end
 *     end
 */
@Type("load-balancer")
public class LoadBalancerResource extends AwsResource implements Copyable<LoadBalancerDescription> {

    private String dnsName;
    private HealthCheckResource healthCheck;
    private Set<InstanceResource> instances;
    private Set<ListenerResource> listener;
    private String loadBalancerName;
    private String scheme;
    private Set<SecurityGroupResource> securityGroups;
    private Set<SubnetResource> subnets;
    private LoadBalancerAttributes attribute;

    /**
     * The public DNS name of this load balancer.
     */
    @Output
    public String getDnsName() {
        return dnsName;
    }

    public void setDnsName(String dnsName) {
        this.dnsName = dnsName;
    }

    /**
     * The HealthCheck subresource for this load balancer.
     */
    public HealthCheckResource getHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(HealthCheckResource healthCheck) {
        this.healthCheck = healthCheck;
    }

    /**
     * The instances to associate with this load balancer.
     */
    @Updatable
    public Set<InstanceResource> getInstances() {
        if (instances == null) {
            instances = new LinkedHashSet<>();
        }

        return instances;
    }

    public void setInstances(Set<InstanceResource> instances) {
        this.instances = instances;
    }

    /**
     * The listeners to associate with this load balancer. (Required)
     */
    public Set<ListenerResource> getListener() {
        if (listener == null) {
            listener = new LinkedHashSet<>();
        }

        return listener;
    }

    public void setListener(Set<ListenerResource> listener) {
        this.listener = listener;
    }

    /**
     * The load balancer name. (Required)
     */
    @Id
    public String getLoadBalancerName() {
        return loadBalancerName;
    }

    public void setLoadBalancerName(String loadBalancerName) {
        this.loadBalancerName = loadBalancerName;
    }

    /**
     * The scheme - either internal or internet-facing. (Required)
     */
    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    /**
     * The security groups to associate with this load balancer. (Required)
     */
    @Updatable
    public Set<SecurityGroupResource> getSecurityGroups() {
        if (securityGroups == null) {
            securityGroups = new LinkedHashSet<>();
        }

        return securityGroups;
    }

    public void setSecurityGroups(Set<SecurityGroupResource> securityGroups) {
        this.securityGroups = securityGroups;
    }

    /**
     * Subnet IDs to associate with this load balancer. (Required)
     */
    @Updatable
    public Set<SubnetResource> getSubnets() {
        if (subnets == null) {
            subnets = new LinkedHashSet<>();
        }

        return subnets;
    }

    public void setSubnets(Set<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    /**
     * The attributes for the Load Balancer.
     */
    @Updatable
    public LoadBalancerAttributes getAttribute() {
        if (attribute == null) {
            attribute = newSubresource(LoadBalancerAttributes.class);
        }

        return attribute;
    }

    public void setAttribute(LoadBalancerAttributes attribute) {
        this.attribute = attribute;
    }

    @Override
    public boolean refresh() {
        ElasticLoadBalancingClient client = createClient(ElasticLoadBalancingClient.class);

        LoadBalancerDescription loadBalancer = getLoadBalancer(client);

        if (loadBalancer == null) {
            return false;
        }

        copyFrom(loadBalancer);

        return true;
    }

    @Override
    public void create() {
        ElasticLoadBalancingClient client = createClient(ElasticLoadBalancingClient.class);

        CreateLoadBalancerResponse response = client.createLoadBalancer(r -> r.listeners(toListeners())
                .securityGroups(getSecurityGroups().stream().map(SecurityGroupResource::getGroupId).collect(Collectors.toList()))
                .subnets(getSubnets().stream().map(SubnetResource::getSubnetId).collect(Collectors.toList()))
                .loadBalancerName(getLoadBalancerName())
                .scheme(getScheme())
        );

        setDnsName(response.dnsName());

        if (!getInstances().isEmpty()) {
            client.registerInstancesWithLoadBalancer(r -> r.instances(toInstances())
                .loadBalancerName(getLoadBalancerName()));
        }

        client.modifyLoadBalancerAttributes(r -> r.loadBalancerAttributes(getAttribute().toLoadBalancerAttributes()).loadBalancerName(getLoadBalancerName()));
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        ElasticLoadBalancingClient client = createClient(ElasticLoadBalancingClient.class);

        LoadBalancerResource currentResource = (LoadBalancerResource) current;

        //-- Instances

        List<Instance> instanceAdditions = new ArrayList<>(toInstances());
        instanceAdditions.removeAll(currentResource.toInstances());

        List<Instance> instanceSubtractions = new ArrayList<>(currentResource.toInstances());
        instanceSubtractions.removeAll(toInstances());

        if (!instanceAdditions.isEmpty()) {
            client.registerInstancesWithLoadBalancer(r -> r.instances(instanceAdditions)
                .loadBalancerName(getLoadBalancerName()));
        }

        if (!instanceSubtractions.isEmpty()) {
            client.deregisterInstancesFromLoadBalancer(r -> r.instances(instanceSubtractions)
                .loadBalancerName(getLoadBalancerName()));
        }

        //-- Subnets

        List<String> pendingSubnetIds = getSubnets().stream().map(SubnetResource::getSubnetId).collect(Collectors.toList());
        List<String> currentSubnetIds = currentResource.getSubnets().stream().map(SubnetResource::getSubnetId).collect(Collectors.toList());

        List<String> subnetAdditions = new ArrayList<>(pendingSubnetIds);
        subnetAdditions.removeAll(currentSubnetIds);

        List<String> subnetSubtractions = new ArrayList<>(currentSubnetIds);
        subnetSubtractions.removeAll(pendingSubnetIds);

        client.attachLoadBalancerToSubnets(r -> r.subnets(subnetAdditions)
            .loadBalancerName(getLoadBalancerName()));

        client.detachLoadBalancerFromSubnets(r -> r.subnets(subnetSubtractions)
            .loadBalancerName(getLoadBalancerName()));

        //-- Security Groups

        List<String> pendingSecurityGroupIds = getSecurityGroups().stream()
            .map(SecurityGroupResource::getGroupId)
            .collect(Collectors.toList());

        List<String> currentSecurityGroupIds = currentResource.getSecurityGroups().stream()
            .map(SecurityGroupResource::getGroupId)
            .collect(Collectors.toList());

        List<String> sgAdditions = new ArrayList<>(pendingSecurityGroupIds);
        sgAdditions.removeAll(currentSecurityGroupIds);

        if (!sgAdditions.isEmpty()) {
            client.applySecurityGroupsToLoadBalancer(r -> r.securityGroups(sgAdditions)
                    .loadBalancerName(getLoadBalancerName()));
        }

        //-- Attributes
        client.modifyLoadBalancerAttributes(r -> r.loadBalancerAttributes(getAttribute().toLoadBalancerAttributes()).loadBalancerName(getLoadBalancerName()));

    }

    @Override
    public void delete() {
        ElasticLoadBalancingClient client = createClient(ElasticLoadBalancingClient.class);
        client.deleteLoadBalancer(r -> r.loadBalancerName(getLoadBalancerName()));
    }

    @Override
    public void copyFrom(LoadBalancerDescription description) {
        setLoadBalancerName(description.loadBalancerName());
        setDnsName(description.dnsName());
        setScheme(description.scheme());

        getInstances().clear();
        description.instances().forEach(i -> getInstances().add(findById(InstanceResource.class, i.instanceId())));

        getSecurityGroups().clear();
        description.securityGroups().forEach(r -> getSecurityGroups().add(findById(SecurityGroupResource.class, r)));

        getSubnets().clear();
        description.subnets().forEach(r -> getSubnets().add(findById(SubnetResource.class, r)));

        HealthCheckResource healthCheckResource = new HealthCheckResource();
        healthCheckResource.setHealthyThreshold(description.healthCheck().healthyThreshold());
        healthCheckResource.setInterval(description.healthCheck().interval());
        healthCheckResource.setTarget(description.healthCheck().target());
        healthCheckResource.setTimeout(description.healthCheck().timeout());
        healthCheckResource.setUnhealthyThreshold(description.healthCheck().unhealthyThreshold());
        setHealthCheck(healthCheckResource);

        getListener().clear();
        for (ListenerDescription listenerDescription : description.listenerDescriptions()) {
            Listener listener = listenerDescription.listener();
            ListenerResource listenerResource = new ListenerResource();
            listenerResource.setInstancePort(listener.instancePort());
            listenerResource.setInstanceProtocol(listener.instanceProtocol());
            listenerResource.setLoadBalancerPort(listener.loadBalancerPort());
            listenerResource.setProtocol(listener.protocol());
            listenerResource.setSslCertificateId(listener.sslCertificateId());
            getListener().add(listenerResource);
        }

        ElasticLoadBalancingClient client = createClient(ElasticLoadBalancingClient.class);

        DescribeLoadBalancerAttributesResponse response = client.describeLoadBalancerAttributes(r -> r.loadBalancerName(getLoadBalancerName()));
        LoadBalancerAttributes loadBalancerAttributes = newSubresource(LoadBalancerAttributes.class);
        loadBalancerAttributes.copyFrom(response.loadBalancerAttributes());
        setAttribute(loadBalancerAttributes);
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        if (getLoadBalancerName() != null) {
            sb.append("load balancer " + getLoadBalancerName());

        } else {
            sb.append("load balancer ");
        }

        return sb.toString();
    }

    private LoadBalancerDescription getLoadBalancer(ElasticLoadBalancingClient client) {
        LoadBalancerDescription loadBalancerDescription = null;
        try {
            DescribeLoadBalancersResponse response = client.describeLoadBalancers(r -> r.loadBalancerNames(getLoadBalancerName()));

            if (!response.loadBalancerDescriptions().isEmpty()) {
                loadBalancerDescription = response.loadBalancerDescriptions().get(0);
            }
        } catch (LoadBalancerNotFoundException ignore) {
            // ignore
        }

        return loadBalancerDescription;
    }

    private Set<Instance> toInstances() {
        Set<Instance> instance = new LinkedHashSet<>();
        for (InstanceResource instanceResource : getInstances()) {
            instance.add(Instance.builder().instanceId(instanceResource.getInstanceId()).build());
        }

        return instance;
    }

    private List<Listener> toListeners() {
        List<Listener> listeners = new ArrayList<>();
        for (ListenerResource resource : getListener()) {
            Listener newListener = Listener.builder()
                    .instancePort(resource.getInstancePort())
                    .instanceProtocol(resource.getInstanceProtocol())
                    .loadBalancerPort(resource.getLoadBalancerPort())
                    .protocol(resource.getProtocol())
                    .sslCertificateId(resource.getSslCertificateId())
                    .build();
            listeners.add(newListener);
        }

        return listeners;
    }

}
