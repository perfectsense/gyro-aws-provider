package gyro.aws.eks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.DescribeNodegroupRequest;
import software.amazon.awssdk.services.eks.model.ListNodegroupsRequest;
import software.amazon.awssdk.services.eks.model.Nodegroup;

@Type("eks-nodegroup")
public class EksNodegroupFinder extends AwsFinder<EksClient, Nodegroup, EksNodegroupResource> {

    private String clusterName;
    private String name;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<Nodegroup> findAllAws(EksClient client) {
        List<Nodegroup> profiles = new ArrayList<>();

        client.listClustersPaginator()
            .clusters()
            .stream()
            .forEach(c -> profiles.addAll(client.listNodegroups(ListNodegroupsRequest.builder()
                .clusterName(c)
                .build())
                .nodegroups()
                .stream()
                .map(f -> client.describeNodegroup(DescribeNodegroupRequest.builder()
                    .clusterName(c)
                    .nodegroupName(f)
                    .build()).nodegroup())
                .collect(Collectors.toList())));

        return profiles;
    }

    @Override
    protected List<Nodegroup> findAws(EksClient client, Map<String, String> filters) {
        List<Nodegroup> profiles = new ArrayList<>();

        if (filters.containsKey("cluster-name") && filters.containsKey("name")) {
            profiles.add(client.describeNodegroup(DescribeNodegroupRequest.builder()
                .nodegroupName(filters.get("name"))
                .clusterName(filters.get("cluster-name"))
                .build()).nodegroup());

        } else if (filters.containsKey("cluster-name")) {
            profiles.addAll(client.listNodegroups(ListNodegroupsRequest.builder()
                .clusterName(filters.get("cluster-name"))
                .build())
                .nodegroups()
                .stream()
                .map(f -> client.describeNodegroup(DescribeNodegroupRequest.builder()
                    .clusterName(filters.get("cluster-name"))
                    .nodegroupName(f)
                    .build()).nodegroup())
                .collect(Collectors.toList()));

        } else {
            client.listClustersPaginator()
                .clusters()
                .stream()
                .filter(c -> client.listNodegroups(ListNodegroupsRequest.builder().clusterName(c).build())
                    .nodegroups()
                    .contains(filters.get("name")))
                .findFirst()
                .ifPresent(cluster -> profiles.add(client.describeNodegroup(DescribeNodegroupRequest.builder()
                    .nodegroupName(filters.get("name"))
                    .clusterName(cluster)
                    .build()).nodegroup()));

        }

        return profiles;
    }
}
