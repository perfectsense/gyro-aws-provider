package gyro.aws.ecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.DescribeServicesRequest;
import software.amazon.awssdk.services.ecs.model.Service;

/**
 * Query ECS service.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    ecs-service: $(external-query aws::service { cluster : "ecs-cluster-example", name : "example-service"})
 */
@Type("service")
public class EcsServiceFinder extends AwsFinder<EcsClient, Service, EcsServiceResource> {

    private String name;
    private String cluster;

    /**
     * The name of the service.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The name of the cluster.
     */
    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    @Override
    protected List<Service> findAllAws(EcsClient client) {
        List<Service> services = new ArrayList<>();
        client.listClusters()
            .clusterArns()
            .forEach(c -> {
                List<String> serviceArns = client.listServices(r -> r.cluster(c)).serviceArns();
                if (!serviceArns.isEmpty()) {
                    services.addAll(client.describeServices(DescribeServicesRequest.builder()
                        .cluster(c)
                        .services(serviceArns)
                        .build()).services());
                }
            });

        return services;
    }

    @Override
    protected List<Service> findAws(EcsClient client, Map<String, String> filters) {
        List<Service> services = new ArrayList<>();
        client.listClusters()
            .clusterArns()
            .stream()
            .filter(c -> !(filters.containsKey("cluster") && !c.split("/")[c.split("/").length - 1]
                .equals(filters.get("cluster"))))
            .forEach(c -> {
                List<String> serviceArns = client.listServices(r -> r.cluster(c)).serviceArns();
                if (!serviceArns.isEmpty()) {
                    services.addAll(client.describeServices(DescribeServicesRequest.builder()
                        .cluster(c)
                        .services(serviceArns)
                        .build())
                        .services()
                        .stream()
                        .filter(s -> !(filters.containsKey("name") && !s.serviceName().equals(filters.get("name"))))
                        .collect(Collectors.toList()));
                }
            });

        return services;
    }
}
