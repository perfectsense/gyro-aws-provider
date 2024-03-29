/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.elb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
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
import software.amazon.awssdk.services.elasticloadbalancing.model.Tag;
import software.amazon.awssdk.services.elasticloadbalancing.model.TagKeyOnly;

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
    private Map<String, String> tags;
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
     *
     * @subresource gyro.aws.elb.HealthCheckResource
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
     * The listeners to associate with this load balancer.
     *
     * @subresource gyro.aws.elb.ListenerResource
     */
    @Required
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
     * The load balancer name.
     */
    @Required
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The scheme of the load balancer. Defaults to ``internet-facing``.
     */
    @ValidStrings({ "internet-facing", "internal" })
    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    /**
     * The security groups to associate with this load balancer.
     */
    @Required
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
     * Subnet IDs to associate with this load balancer.
     */
    @Required
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
     *  List of tags associated with the load balancer.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The attributes for the Load Balancer.
     *
     * @subresource gyro.aws.elb.LoadBalancerAttributes
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
        List<InstanceState> instanceStates = client.describeInstanceHealth(r -> r.loadBalancerName(getName()))
            .instanceStates();
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

        if (getListener().stream().anyMatch(o -> o.getPolicy() != null)) {
            for (ListenerResource listener : getListener().stream().filter(o -> o.getPolicy() != null).collect(
                Collectors.toList())) {
                state.save();
                listener.savePolicy(client);
            }
        }

        if (!getInstances().isEmpty()) {
            client.registerInstancesWithLoadBalancer(r -> r.instances(toInstances())
                .loadBalancerName(getName()));
        }

        // modify connection timeout with enabled set to true, then set to what is actually configured.
        client.modifyLoadBalancerAttributes(r -> r.loadBalancerAttributes(getAttribute().toLoadBalancerAttributes(true))
            .loadBalancerName(getName()));
        client.modifyLoadBalancerAttributes(r -> r.loadBalancerAttributes(getAttribute().toLoadBalancerAttributes(false))
            .loadBalancerName(getName()));

        if (!getTags().isEmpty()) {
            client.addTags(r -> r.loadBalancerNames(getName())
                .tags(getTags().entrySet()
                    .stream()
                    .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
                    .collect(Collectors.toList())));
        }

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
                .loadBalancerName(getName()));
        }

        if (!instanceSubtractions.isEmpty()) {
            client.deregisterInstancesFromLoadBalancer(r -> r.instances(instanceSubtractions)
                .loadBalancerName(getName()));
        }

        //-- Subnets

        List<String> pendingSubnetIds = getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList());
        List<String> currentSubnetIds = currentResource.getSubnets()
            .stream()
            .map(SubnetResource::getId)
            .collect(Collectors.toList());

        List<String> subnetAdditions = new ArrayList<>(pendingSubnetIds);
        subnetAdditions.removeAll(currentSubnetIds);

        List<String> subnetSubtractions = new ArrayList<>(currentSubnetIds);
        subnetSubtractions.removeAll(pendingSubnetIds);

        client.attachLoadBalancerToSubnets(r -> r.subnets(subnetAdditions)
            .loadBalancerName(getName()));

        client.detachLoadBalancerFromSubnets(r -> r.subnets(subnetSubtractions)
            .loadBalancerName(getName()));

        //-- Security Groups

        if (changedFieldNames.contains("security-groups")) {
            client.applySecurityGroupsToLoadBalancer(r -> r.securityGroups(getSecurityGroups().stream()
                .map(SecurityGroupResource::getId).collect(
                    Collectors.toList()))
                .loadBalancerName(getName()));
        }

        //-- Attributes

        // modify connection timeout with enabled set to true, then set to what is actually configured.
        client.modifyLoadBalancerAttributes(r -> r.loadBalancerAttributes(getAttribute().toLoadBalancerAttributes(true))
            .loadBalancerName(getName()));
        client.modifyLoadBalancerAttributes(r -> r.loadBalancerAttributes(getAttribute().toLoadBalancerAttributes(false))
            .loadBalancerName(getName()));

        //-- tags
        if (changedFieldNames.contains("tags")) {
            if (!currentResource.getTags().isEmpty()) {
                client.removeTags(r -> r.loadBalancerNames(currentResource.getName())
                    .tags(currentResource.getTags().entrySet()
                        .stream()
                        .map(e -> TagKeyOnly.builder().key(e.getKey()).build())
                        .collect(Collectors.toList())));
            }

            if (!getTags().isEmpty()) {
                client.addTags(r -> r.loadBalancerNames(currentResource.getName())
                    .tags(getTags().entrySet()
                        .stream()
                        .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
                        .collect(Collectors.toList())));
            }
        }
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
            ListenerResource listenerResource = newSubresource(ListenerResource.class);
            listenerResource.copyFrom(listenerDescription);
            getListener().add(listenerResource);
            listenerResource.parent();
        }

        ElasticLoadBalancingClient client = createClient(ElasticLoadBalancingClient.class);

        DescribeLoadBalancerAttributesResponse response = client.describeLoadBalancerAttributes(r -> r.loadBalancerName(
            getName()));
        LoadBalancerAttributes loadBalancerAttributes = newSubresource(LoadBalancerAttributes.class);
        loadBalancerAttributes.copyFrom(response.loadBalancerAttributes());
        setAttribute(loadBalancerAttributes);

        getTags().clear();
        client.describeTags(r -> r.loadBalancerNames(getName()))
            .tagDescriptions().forEach(d -> d.tags()
            .forEach(t -> getTags().put(t.key(), t.value())));
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
