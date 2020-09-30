/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.ecs;

import gyro.aws.Copyable;
import gyro.aws.elbv2.TargetGroupResource;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.ecs.model.LoadBalancer;

public class EcsLoadBalancer extends Diffable implements Copyable<LoadBalancer> {

    private TargetGroupResource targetGroup;
    private String loadBalancer;
    private String container;
    private Integer containerPort;

    /**
     * The Elastic Load Balancing target group or groups associated with a service or task set.
     */
    public TargetGroupResource getTargetGroup() {
        return targetGroup;
    }

    public void setTargetGroup(TargetGroupResource targetGroup) {
        this.targetGroup = targetGroup;
    }

    /**
     * The name of the load balancer to associate with the Amazon ECS service or task set.
     */
    public String getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(String loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    /**
     * The name of the container to associate with the load balancer.
     */
    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    /**
     * The port on the container to associate with the load balancer.
     */
    public Integer getContainerPort() {
        return containerPort;
    }

    public void setContainerPort(Integer containerPort) {
        this.containerPort = containerPort;
    }

    @Override
    public String primaryKey() {
        StringBuilder sb = new StringBuilder("Ecs Load Balancer - ");

        if (getLoadBalancer() != null) {
            sb.append("Name: ").append(getLoadBalancer()).append(" ");
        }

        if (getTargetGroup() != null) {
            sb.append("Target Group: ").append(getTargetGroup().getArn()).append(" ");
        }

        if (getContainer() != null) {
            sb.append("Container: ").append(getContainer()).append(" ");
        }

        if (getContainerPort() != null) {
            sb.append("Port: ").append(getContainerPort());
        }

        return sb.toString();
    }

    @Override
    public void copyFrom(LoadBalancer model) {
        setContainer(model.containerName());
        setTargetGroup(findById(TargetGroupResource.class, model.targetGroupArn()));
        setContainer(model.containerName());
        setContainerPort(model.containerPort());
    }

    public LoadBalancer toLoadBalancer() {
        LoadBalancer.Builder builder = LoadBalancer.builder();

        if (getContainer() != null) {
            builder.containerName(getContainer());
        }

        if (getContainerPort() != null) {
            builder.containerPort(getContainerPort());
        }

        if (getLoadBalancer() != null) {
            builder.loadBalancerName(getLoadBalancer());
        }

        if (getTargetGroup() != null) {
            builder.targetGroupArn(getTargetGroup().getArn());
        }

        return builder.build();
    }
}
