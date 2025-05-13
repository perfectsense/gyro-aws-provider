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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancerNotFoundException;

public abstract class LoadBalancerFinder<R extends LoadBalancerResource>
    extends AwsFinder<ElasticLoadBalancingV2Client, LoadBalancer, R> {

    private String arn;
    private String name;

    /**
     *  The arn of the load balancer to find.
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     *  The name of the load balancer to find.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<LoadBalancer> findAws(ElasticLoadBalancingV2Client client, Map<String, String> filters) {
        List<LoadBalancer> loadBalancers = new ArrayList<>();

        if (!filters.containsKey("arn") && !filters.containsKey("name")) {
            throw new IllegalArgumentException("'name' or 'arn' is required.");
        } else {
            try {
                if (filters.containsKey("arn")) {
                    loadBalancers.addAll(
                        client.describeLoadBalancers(r -> r.loadBalancerArns(filters.get("arn"))).loadBalancers());
                } else {
                    loadBalancers.addAll(client.describeLoadBalancersPaginator(r -> r.names(filters.get("name")))
                        .loadBalancers().stream().collect(Collectors.toList()));
                }
            } catch (LoadBalancerNotFoundException ex) {
                // Ignore
            }
        }
        return loadBalancers;
    }

    @Override
    public List<LoadBalancer> findAllAws(ElasticLoadBalancingV2Client client) {
        return client.describeLoadBalancersPaginator().loadBalancers().stream().collect(Collectors.toList());
    }
}
