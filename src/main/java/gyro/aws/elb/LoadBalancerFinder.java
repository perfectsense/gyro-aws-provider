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

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.elasticloadbalancing.ElasticLoadBalancingClient;
import software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerDescription;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query for classic load balancers.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    load-balancer: $(external-query aws::load-balancer { name: 'my elb'})
 */
@Type("load-balancer")
public class LoadBalancerFinder extends AwsFinder<ElasticLoadBalancingClient, LoadBalancerDescription, LoadBalancerResource> {

    private String name;

    /**
     * The name of the load balancer to find.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<LoadBalancerDescription> findAws(ElasticLoadBalancingClient client, Map<String, String> filters) {
        if (!filters.containsKey("name")) {
            throw new IllegalArgumentException("'name' is required.");
        }

        return client.describeLoadBalancers(r -> r.loadBalancerNames(filters.get("name"))).loadBalancerDescriptions();
    }

    @Override
    protected List<LoadBalancerDescription> findAllAws(ElasticLoadBalancingClient client) {
        return client.describeLoadBalancersPaginator().loadBalancerDescriptions().stream().collect(Collectors.toList());
    }

}
