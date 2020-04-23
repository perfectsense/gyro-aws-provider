package gyro.aws.eks;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.Cluster;
import software.amazon.awssdk.services.eks.model.DescribeClusterRequest;

@Type("eks-cluster")
public class EksClusterFinder extends AwsFinder<EksClient, Cluster, EksClusterResource> {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<Cluster> findAllAws(EksClient client) {
        return client.listClustersPaginator()
            .clusters()
            .stream()
            .map(s -> client.describeCluster(DescribeClusterRequest.builder().name(s).build()).cluster())
            .collect(Collectors.toList());
    }

    @Override
    protected List<Cluster> findAws(EksClient client, Map<String, String> filters) {
        return client.listClustersPaginator()
            .clusters()
            .stream()
            .filter(s -> s.equals(getName()))
            .map(s -> client.describeCluster(DescribeClusterRequest.builder().name(s).build()).cluster())
            .collect(Collectors.toList());
    }
}
