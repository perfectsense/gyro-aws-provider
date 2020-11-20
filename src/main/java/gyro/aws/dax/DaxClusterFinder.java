package gyro.aws.dax;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gyro.aws.AwsFinder;
import software.amazon.awssdk.services.dax.DaxClient;
import software.amazon.awssdk.services.dax.model.Cluster;

public class DaxClusterFinder extends AwsFinder<DaxClient, Cluster, DaxClusterResource> {

    private List<String> names;

    /**
     * The names of the DAX clusters.
     */
    public List<String> getNames() {
        if (names == null) {
            names = new ArrayList<>();
        }

        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    @Override
    protected List<Cluster> findAllAws(DaxClient client) {
        return client.describeClusters().clusters();
    }

    @Override
    protected List<Cluster> findAws(DaxClient client, Map<String, String> filters) {
        return client.describeClusters(r -> r.clusterNames(filters.get("names"))).clusters();
    }
}
