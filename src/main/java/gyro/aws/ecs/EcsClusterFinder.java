package gyro.aws.ecs;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.Cluster;
import software.amazon.awssdk.services.ecs.model.ClusterNotFoundException;
import software.amazon.awssdk.services.ecs.model.ListClustersResponse;

/**
 * Query ecs cluster.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    ecs-cluster: $(external-query aws::ecs-cluster { name: 'ecs-cluster-example' })
 */

@Type("ecs-cluster")
public class EcsClusterFinder extends AwsFinder<EcsClient, Cluster, EcsClusterResource> {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<Cluster> findAllAws(EcsClient client) {
        ListClustersResponse response = client.listClusters();

        if (response.hasClusterArns()) {
            return client.describeClusters(
                r -> r.clusters(response.clusterArns())
                    .includeWithStrings("TAGS", "SETTINGS")
            ).clusters().stream().collect(Collectors.toList());

        } else {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<Cluster> findAws(EcsClient client, Map<String, String> filters) {
        if (!filters.containsKey("name")) {
            throw new IllegalArgumentException("'name' is required.");
        }

        try {
            return client.describeClusters(
                r -> r.clusters(filters.get("name"))
                    .includeWithStrings("TAGS", "SETTINGS")
            ).clusters().stream().collect(Collectors.toList());

        } catch (ClusterNotFoundException ex) {
            return Collections.emptyList();
        }
    }
}
