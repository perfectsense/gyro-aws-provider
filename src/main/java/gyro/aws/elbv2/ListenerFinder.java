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

import gyro.aws.AwsFinder;

import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Listener;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.LoadBalancer;
import software.amazon.awssdk.services.elasticloadbalancingv2.paginators.DescribeListenersIterable;
import software.amazon.awssdk.services.elasticloadbalancingv2.paginators.DescribeLoadBalancersIterable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class ListenerFinder<R extends ListenerResource> extends AwsFinder<ElasticLoadBalancingV2Client, Listener, R> {

    private String arn;

    /**
     *  The arn of the listener to find.
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public List<Listener> findAws(ElasticLoadBalancingV2Client client, Map<String, String> filters) {
        return client.describeListeners(r -> r.listenerArns(filters.get("arn"))).listeners();
    }

    @Override
    public List<Listener> findAllAws(ElasticLoadBalancingV2Client client) {
        List<Listener> listeners = new ArrayList<>();

        List<LoadBalancer> loadBalancers = new ArrayList<>();
        DescribeLoadBalancersIterable iterable = client.describeLoadBalancersPaginator();
        iterable.stream().forEach(r -> loadBalancers.addAll(r.loadBalancers()));

        for (LoadBalancer loadBalancer : loadBalancers) {
            DescribeListenersIterable listenerIterable = client.describeListenersPaginator(r -> r.loadBalancerArn(loadBalancer.loadBalancerArn()));
            listenerIterable.stream().forEach(r -> listeners.addAll(r.listeners()));
        }

        return listeners;
    }
}
