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
        return String.format(
            "Ecs Load Balancer - Name: %s, Target Group: %s, Container: %s, Port: %s",
            getLoadBalancer(),
            getTargetGroup().getArn(),
            getContainer(),
            getContainerPort());
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
