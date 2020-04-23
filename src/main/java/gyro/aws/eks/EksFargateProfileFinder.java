package gyro.aws.eks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.DescribeFargateProfileRequest;
import software.amazon.awssdk.services.eks.model.FargateProfile;
import software.amazon.awssdk.services.eks.model.ListFargateProfilesRequest;

@Type("eks-fargate-profile")
public class EksFargateProfileFinder extends AwsFinder<EksClient, FargateProfile, EksFargateProfileResource> {

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
    protected List<FargateProfile> findAllAws(EksClient client) {
        List<FargateProfile> profiles = new ArrayList<>();

        client.listClustersPaginator()
            .clusters()
            .stream()
            .forEach(c -> profiles.addAll(client.listFargateProfiles(ListFargateProfilesRequest.builder()
                .clusterName(c)
                .build())
                .fargateProfileNames()
                .stream()
                .map(f -> client.describeFargateProfile(DescribeFargateProfileRequest.builder()
                    .clusterName(c)
                    .fargateProfileName(f)
                    .build()).fargateProfile())
                .collect(Collectors.toList())));

        return profiles;
    }

    @Override
    protected List<FargateProfile> findAws(EksClient client, Map<String, String> filters) {
        List<FargateProfile> profiles = new ArrayList<>();

        if (filters.containsKey("cluster-name") && filters.containsKey("name")) {
            profiles.add(client.describeFargateProfile(DescribeFargateProfileRequest.builder()
                .fargateProfileName(filters.get("name"))
                .clusterName(filters.get("cluster-name"))
                .build()).fargateProfile());

        } else if (filters.containsKey("cluster-name")) {
            profiles.addAll(client.listFargateProfiles(ListFargateProfilesRequest.builder()
                .clusterName(filters.get("cluster-name"))
                .build())
                .fargateProfileNames()
                .stream()
                .map(f -> client.describeFargateProfile(DescribeFargateProfileRequest.builder()
                    .clusterName(filters.get("cluster-name"))
                    .fargateProfileName(f)
                    .build()).fargateProfile())
                .collect(Collectors.toList()));

        } else {
            client.listClustersPaginator()
                .clusters()
                .stream()
                .filter(c -> client.listFargateProfiles(ListFargateProfilesRequest.builder().clusterName(c).build())
                    .fargateProfileNames()
                    .contains(filters.get("name")))
                .findFirst()
                .ifPresent(cluster -> profiles.add(client.describeFargateProfile(DescribeFargateProfileRequest.builder()
                    .fargateProfileName(filters.get("name"))
                    .clusterName(cluster)
                    .build()).fargateProfile()));

        }

        return profiles;
    }
}
