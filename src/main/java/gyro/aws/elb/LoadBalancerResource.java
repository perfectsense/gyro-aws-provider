package gyro.aws.elb;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.ec2.InstanceResource;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.ec2.SubnetResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;

import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.elasticloadbalancing.ElasticLoadBalancingClient;
import software.amazon.awssdk.services.elasticloadbalancing.model.CreateLoadBalancerResponse;
import software.amazon.awssdk.services.elasticloadbalancing.model.DescribeLoadBalancerAttributesResponse;
import software.amazon.awssdk.services.elasticloadbalancing.model.DescribeLoadBalancersResponse;
import software.amazon.awssdk.services.elasticloadbalancing.model.Instance;
import software.amazon.awssdk.services.elasticloadbalancing.model.InstanceState;
import software.amazon.awssdk.services.elasticloadbalancing.model.Listener;
import software.amazon.awssdk.services.elasticloadbalancing.model.ListenerDescription;
import software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerDescription;
import software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
 *         name: "elb"
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
 *
 *         attributes
 *             connection-draining
 *                 enabled: false
 *                 timeout: 300
 *             end
 *         end
 *     end
 */
@Type("load-balancer")
public class LoadBalancerResource extends AwsResource implements Copyable<LoadBalancerDescription> {

    private String dnsName;
    private HealthCheckResource healthCheck;
    private Set<InstanceResource> instances;
    private Set<ListenerResource> listener;
    private String name;
    private String scheme;
    private Set<SecurityGroupResource> securityGroups;
    private Set<SubnetResource> subnets;
    private LoadBalancerAttributes attribute;
    private String hostedZoneId;

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
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    /**
     * The hosted zone ID for the Load Balancer.
     */
    @Output
    public String getHostedZoneId() {
        return hostedZoneId;
    }

    public void setHostedZoneId(String hostedZoneId) {
        this.hostedZoneId = hostedZoneId;
    }

    public Map<String, Integer> instanceHealth() {
        Map<String, Integer> healthMap = new HashMap<>();

        ElasticLoadBalancingClient client = createClient(ElasticLoadBalancingClient.class);
        List<InstanceState> instanceStates = client.describeInstanceHealth(r -> r.loadBalancerName(getName())).instanceStates();
        for (InstanceState is : instanceStates) {
            int count = healthMap.getOrDefault(is.state(), 0);
            healthMap.put(is.state(), count + 1);
        }

        healthMap.put("Total", instanceStates.size());

        return healthMap;
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
    public void create(GyroUI ui, State state) {
        ElasticLoadBalancingClient client = createClient(ElasticLoadBalancingClient.class);

        if (getLoadBalancer(client) != null) {
            throw new GyroException(String.format("A load balancer with the name '%s' exists.", getName()));
        }

        CreateLoadBalancerResponse response = client.createLoadBalancer(r -> r.listeners(toListeners())
                .securityGroups(getSecurityGroups().stream().map(SecurityGroupResource::getId).collect(Collectors.toList()))
                .subnets(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList()))
                .loadBalancerName(getName())
                .scheme(getScheme())
        );

        setDnsName(response.dnsName());

        if (!getInstances().isEmpty()) {
            client.registerInstancesWithLoadBalancer(r -> r.instances(toInstances())
                .loadBalancerName(getName()));
        }

        // modify connection timeout with enabled set to true, then set to what is actually configured.
        client.modifyLoadBalancerAttributes(r -> r.loadBalancerAttributes(getAttribute().toLoadBalancerAttributes(true)).loadBalancerName(getName()));
        client.modifyLoadBalancerAttributes(r -> r.loadBalancerAttributes(getAttribute().toLoadBalancerAttributes(false)).loadBalancerName(getName()));

        LoadBalancerDescription loadBalancer = getLoadBalancer(client);
        copyFrom(loadBalancer);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        ElasticLoadBalancingClient client = createClient(ElasticLoadBalancingClient.class);

        LoadBalancerResource currentResource = (LoadBalancerResource) current;

        //-- Instances

        List<Instance> instanceAdditions = new ArrayList<>(toInstances());
        instanceAdditions.removeAll(currentResource.toInstances());

        List<Instance> instanceSubtractions = new ArrayList<>(currentResource.toInstances());
        instanceSubtractions.removeAll(toInstances());

        if (!instanceAdditions.isEmpty()) {
            client.registerInstancesWithLoadBalancer(r -> r.instances(instanceAdditions)
                .loadBalancerName(currentResource.getName()));
        }

        if (!instanceSubtractions.isEmpty()) {
            client.deregisterInstancesFromLoadBalancer(r -> r.instances(instanceSubtractions)
                .loadBalancerName(currentResource.getName()));
        }

        //-- Subnets

        List<String> pendingSubnetIds = getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList());
        List<String> currentSubnetIds = currentResource.getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList());

        List<String> subnetAdditions = new ArrayList<>(pendingSubnetIds);
        subnetAdditions.removeAll(currentSubnetIds);

        List<String> subnetSubtractions = new ArrayList<>(currentSubnetIds);
        subnetSubtractions.removeAll(pendingSubnetIds);

        client.attachLoadBalancerToSubnets(r -> r.subnets(subnetAdditions)
            .loadBalancerName(currentResource.getName()));

        client.detachLoadBalancerFromSubnets(r -> r.subnets(subnetSubtractions)
            .loadBalancerName(currentResource.getName()));

        //-- Security Groups

        List<String> pendingSecurityGroupIds = getSecurityGroups().stream()
            .map(SecurityGroupResource::getId)
            .collect(Collectors.toList());

        List<String> currentSecurityGroupIds = currentResource.getSecurityGroups().stream()
            .map(SecurityGroupResource::getId)
            .collect(Collectors.toList());

        List<String> sgAdditions = new ArrayList<>(pendingSecurityGroupIds);
        sgAdditions.removeAll(currentSecurityGroupIds);

        if (!sgAdditions.isEmpty()) {
            client.applySecurityGroupsToLoadBalancer(r -> r.securityGroups(sgAdditions)
                    .loadBalancerName(currentResource.getName()));
        }

        //-- Attributes

        // modify connection timeout with enabled set to true, then set to what is actually configured.
        client.modifyLoadBalancerAttributes(r -> r.loadBalancerAttributes(getAttribute().toLoadBalancerAttributes(true)).loadBalancerName(currentResource.getName()));
        client.modifyLoadBalancerAttributes(r -> r.loadBalancerAttributes(getAttribute().toLoadBalancerAttributes(false)).loadBalancerName(currentResource.getName()));
    }

    @Override
    public void delete(GyroUI ui, State state) {
        ElasticLoadBalancingClient client = createClient(ElasticLoadBalancingClient.class);
        client.deleteLoadBalancer(r -> r.loadBalancerName(getName()));
    }

    @Override
    public void copyFrom(LoadBalancerDescription description) {
        setName(description.loadBalancerName());
        setDnsName(description.dnsName());
        setScheme(description.scheme());
        setHostedZoneId(description.canonicalHostedZoneNameID());

        getInstances().clear();
        description.instances().forEach(i -> getInstances().add(findById(InstanceResource.class, i.instanceId())));

        getSecurityGroups().clear();
        description.securityGroups().forEach(r -> getSecurityGroups().add(findById(SecurityGroupResource.class, r)));

        getSubnets().clear();
        description.subnets().forEach(r -> getSubnets().add(findById(SubnetResource.class, r)));

        HealthCheckResource healthCheckResource = newSubresource(HealthCheckResource.class);
        healthCheckResource.setHealthyThreshold(description.healthCheck().healthyThreshold());
        healthCheckResource.setInterval(description.healthCheck().interval());
        healthCheckResource.setTarget(description.healthCheck().target());
        healthCheckResource.setTimeout(description.healthCheck().timeout());
        healthCheckResource.setUnhealthyThreshold(description.healthCheck().unhealthyThreshold());
        setHealthCheck(healthCheckResource);

        getListener().clear();
        for (ListenerDescription listenerDescription : description.listenerDescriptions()) {
            Listener listener = listenerDescription.listener();
            ListenerResource listenerResource = newSubresource(ListenerResource.class);
            listenerResource.setInstancePort(listener.instancePort());
            listenerResource.setInstanceProtocol(listener.instanceProtocol());
            listenerResource.setLoadBalancerPort(listener.loadBalancerPort());
            listenerResource.setProtocol(listener.protocol());
            listenerResource.setSslCertificateId(listener.sslCertificateId());
            getListener().add(listenerResource);
        }

        ElasticLoadBalancingClient client = createClient(ElasticLoadBalancingClient.class);

        DescribeLoadBalancerAttributesResponse response = client.describeLoadBalancerAttributes(r -> r.loadBalancerName(getName()));
        LoadBalancerAttributes loadBalancerAttributes = newSubresource(LoadBalancerAttributes.class);
        loadBalancerAttributes.copyFrom(response.loadBalancerAttributes());
        setAttribute(loadBalancerAttributes);
    }

    private LoadBalancerDescription getLoadBalancer(ElasticLoadBalancingClient client) {
        LoadBalancerDescription loadBalancerDescription = null;
        try {
            DescribeLoadBalancersResponse response = client.describeLoadBalancers(r -> r.loadBalancerNames(getName()));

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
            instance.add(Instance.builder().instanceId(instanceResource.getId()).build());
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
