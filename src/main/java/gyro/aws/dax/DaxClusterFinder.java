package gyro.aws.dax;

import java.util.List;
import java.util.Map;

import gyro.aws.AwsFinder;
import software.amazon.awssdk.services.dax.DaxClient;
import software.amazon.awssdk.services.dax.model.Cluster;

public class DaxClusterFinder extends AwsFinder<DaxClient, Cluster, DaxClusterResource> {

    private List<String> clusterNames;

    /**
     * The names of the DAX clusters.
     */
    public List<String> getClusterNames() {
        return clusterNames;
    }

    public void setCluserNames(List<String> clusterNames) {
        this.clusterNames = clusterNames;
    }

    @Override
    protected List<Cluster> findAllAws(DaxClient client) {
        return client.describeClusters().clusters();
    }

    @Override
    protected List<Cluster> findAws(DaxClient client, Map<String, String> filters) {
        return client.describeClusters(r -> r.clusterNames(filters.get("cluster-names"))).clusters();
    }
}
