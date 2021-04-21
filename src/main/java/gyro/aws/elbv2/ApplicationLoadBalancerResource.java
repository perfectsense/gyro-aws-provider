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

import gyro.aws.Copyable;
import gyro.aws.ec2.SecurityGroupResource;
import gyro.aws.ec2.SubnetResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;

import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateLoadBalancerResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerTypeEnum;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::application-load-balancer alb-example
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
@Type("application-load-balancer")
public class ApplicationLoadBalancerResource extends LoadBalancerResource implements Copyable<LoadBalancer> {

    private Set<SecurityGroupResource> securityGroups;
    private Set<SubnetResource> subnets;

    /**
     *  List of security groups associated with the alb.
     */
    @Updatable
    public Set<SecurityGroupResource> getSecurityGroups() {
        if (securityGroups == null) {
            securityGroups = new HashSet<>();
        }

        return securityGroups;
    }

    public void setSecurityGroups(Set<SecurityGroupResource> securityGroups) {
        this.securityGroups = securityGroups;
    }

    /**
     *  List of subnets associated with the alb.
     */
    @Required
    @Updatable
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
    public void copyFrom(LoadBalancer loadBalancer) {
        getSecurityGroups().clear();
        loadBalancer.securityGroups().forEach(r -> getSecurityGroups().add(findById(SecurityGroupResource.class, r)));

        getSubnets().clear();
        loadBalancer.availabilityZones().forEach(az -> getSubnets().add(findById(SubnetResource.class, az.subnetId())));

        super.copyFrom(loadBalancer);
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
    public void create(GyroUI ui, State state) {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);

        CreateLoadBalancerResponse response = client.createLoadBalancer(r -> r.ipAddressType(getIpAddressType())
                .name(getName())
                .scheme(getScheme())
                .securityGroups(getSecurityGroups().stream().map(SecurityGroupResource::getId).collect(Collectors.toList()))
                .subnets(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList()))
                .type(LoadBalancerTypeEnum.APPLICATION)
        );

        setArn(response.loadBalancers().get(0).loadBalancerArn());
        setDnsName(response.loadBalancers().get(0).dnsName());

        super.create(ui, state);
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);

        client.setSecurityGroups(r -> r.loadBalancerArn(getArn())
                .securityGroups(getSecurityGroups().stream().map(SecurityGroupResource::getId).collect(Collectors.toList())));
        client.setSubnets(r -> r.loadBalancerArn(getArn())
                .subnets(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList())));

        super.update(ui, state, current, changedFieldNames);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        super.delete(ui, state);
    }

}
