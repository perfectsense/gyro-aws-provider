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

package gyro.aws.elbv2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.psddev.dari.util.CompactMap;
import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.route53.HostedZoneResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.TimeoutSettings;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeLoadBalancersResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeTagsResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerNotFoundException;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Tag;

public abstract class LoadBalancerResource extends AwsResource implements Copyable<LoadBalancer> {

    private String dnsName;
    private String ipAddressType;
    private String arn;
    private String name;
    private String scheme;
    private Map<String, String> tags;
    private HostedZoneResource hostedZone;

    /**
     *  Public DNS name for the alb.
     */
    @Output
    public String getDnsName() {
        return dnsName;
    }

    public void setDnsName(String dnsName) {
        this.dnsName = dnsName;
    }

    /**
     *  Type of IP address used by the subnets of the alb.
     */
    public String getIpAddressType() {
        return ipAddressType;
    }

    public void setIpAddressType(String ipAddressType) {
        this.ipAddressType = ipAddressType;
    }

    /**
     *  The arn of the load balancer.
     */
    @Id
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     *  The name of the load balancer.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     *  Type of nodes used by the alb.
     */
    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    /**
     *  List of tags associated with the alb.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new CompactMap<>();
        }
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        if (this.tags != null && tags != null) {
            this.tags.putAll(tags);

        } else {
            this.tags = tags;
        }
    }

    /**
     *  The hosted zone associated with the load balancer.
     */
    @Output
    public HostedZoneResource getHostedZone() {
        return hostedZone;
    }

    public void setHostedZone(HostedZoneResource hostedZone) {
        this.hostedZone = hostedZone;
    }

    @Override
    public void copyFrom(LoadBalancer loadBalancer) {
        setDnsName(loadBalancer.dnsName());
        setIpAddressType(loadBalancer.ipAddressTypeAsString());
        setArn(loadBalancer.loadBalancerArn());
        setName(loadBalancer.loadBalancerName());
        setScheme(loadBalancer.schemeAsString());

        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);

        DescribeTagsResponse describeTagsResponse = client.describeTags(r -> r.resourceArns(getArn()));
        describeTagsResponse.tagDescriptions().forEach(tag -> tag.tags().forEach(t -> getTags().put(t.key(), t.value())));

        setHostedZone(findById(HostedZoneResource.class, loadBalancer.canonicalHostedZoneId()));
    }

    public LoadBalancer internalRefresh() {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        try {
            DescribeLoadBalancersResponse lbResponse = client.describeLoadBalancers(r -> r.loadBalancerArns(getArn()));

            LoadBalancer loadBalancer = lbResponse.loadBalancers().get(0);

            this.copyFrom(loadBalancer);

            return loadBalancer;

        } catch (LoadBalancerNotFoundException ex) {
            return null;
        }
    }

    @Override
    public void create(GyroUI ui, State state) {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);

        if (!getTags().isEmpty()) {
            List<Tag> tag = new ArrayList<>();
            getTags().forEach((key, value) -> tag.add(Tag.builder().key(key).value(value).build()));
            client.addTags(r -> r.tags(tag)
                    .resourceArns(getArn()));
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        LoadBalancerResource currentResource = (LoadBalancerResource) current;

        Map<String, String> tagAdditions = new HashMap<>(getTags());
        currentResource.getTags().forEach((key, value) -> tagAdditions.remove(key, value));

        Map<String, String> tagSubtractions = new HashMap<>(currentResource.getTags());
        getTags().forEach((key, value) -> tagSubtractions.remove(key, value));

        if (!tagAdditions.isEmpty()) {
            List<Tag> tag = new ArrayList<>();
            tagAdditions.forEach((key, value) -> tag.add(Tag.builder().key(key).value(value).build()));
            client.addTags(r -> r.tags(tag)
                    .resourceArns(getArn()));
        }

        if (!tagSubtractions.isEmpty()) {
            List<String> tag = new ArrayList<>();
            tagSubtractions.forEach((key, value) -> tag.add(key));
            client.removeTags(r -> r.tagKeys(tag)
                    .resourceArns(getArn()));
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.deleteLoadBalancer(r -> r.loadBalancerArn(getArn()));

        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.DELETE)
            .prompt(true)
            .until(() -> getLoadBalancer(client) == null);

        // Delay for resources has this load balancer as a dependency.
        try {
            Thread.sleep(60000);
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
        }
    }

    private LoadBalancer getLoadBalancer(ElasticLoadBalancingV2Client client) {
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

        return loadBalancer;
    }

}
