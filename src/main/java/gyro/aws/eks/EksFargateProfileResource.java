package gyro.aws.eks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.ec2.SubnetResource;
import gyro.aws.iam.RoleResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.CreateFargateProfileRequest;
import software.amazon.awssdk.services.eks.model.CreateFargateProfileResponse;
import software.amazon.awssdk.services.eks.model.DeleteFargateProfileRequest;
import software.amazon.awssdk.services.eks.model.DescribeFargateProfileRequest;
import software.amazon.awssdk.services.eks.model.FargateProfile;
import software.amazon.awssdk.services.eks.model.TagResourceRequest;
import software.amazon.awssdk.services.eks.model.UntagResourceRequest;

@Type("eks-fargate-profile")
public class EksFargateProfileResource extends AwsResource implements Copyable<FargateProfile> {

    private String name;
    private EksClusterResource cluster;
    private RoleResource podExecutionRole;
    private List<EksFargateProfileSelector> selector;
    private List<SubnetResource> subnets;
    private Map<String, String> tags;

    // Read-only
    private String arn;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EksClusterResource getCluster() {
        return cluster;
    }

    public void setCluster(EksClusterResource cluster) {
        this.cluster = cluster;
    }

    public RoleResource getPodExecutionRole() {
        return podExecutionRole;
    }

    public void setPodExecutionRole(RoleResource podExecutionRole) {
        this.podExecutionRole = podExecutionRole;
    }

    @Required
    public List<EksFargateProfileSelector> getSelector() {
        if (selector == null) {
            selector = new ArrayList<>();
        }

        return selector;
    }

    public void setSelector(List<EksFargateProfileSelector> selector) {
        this.selector = selector;
    }

    public List<SubnetResource> getSubnets() {
        if (subnets == null) {
            subnets = new ArrayList<>();
        }

        return subnets;
    }

    public void setSubnets(List<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    @Output
    @Id
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public void copyFrom(FargateProfile model) {
        setArn(model.fargateProfileArn());
        setName(model.fargateProfileName());
        setCluster(findById(EksClusterResource.class, model.clusterName()));
        setPodExecutionRole(findById(RoleResource.class, model.podExecutionRoleArn()));
        setTags(model.tags());
        setSelector(model.selectors().stream().map(s -> {
            EksFargateProfileSelector fargateProfileSelector = newSubresource(EksFargateProfileSelector.class);
            fargateProfileSelector.copyFrom(s);
            return fargateProfileSelector;
        }).collect(Collectors.toList()));
        setSubnets(model.subnets().stream().map(s -> findById(SubnetResource.class, s)).collect(Collectors.toList()));
    }

    @Override
    public boolean refresh() {
        EksClient client = createClient(EksClient.class);

        FargateProfile fargateProfile = getFargateProfile(client);

        if (fargateProfile == null) {
            return false;
        }

        copyFrom(fargateProfile);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        EksClient client = createClient(EksClient.class);

        CreateFargateProfileResponse profile = client.createFargateProfile(CreateFargateProfileRequest.builder()
            .clusterName(getCluster().getName())
            .fargateProfileName(getName())
            .podExecutionRoleArn(getPodExecutionRole().getArn())
            .tags(getTags())
            .selectors(getSelector().stream()
                .map(EksFargateProfileSelector::toFargateProfileSelector)
                .collect(Collectors.toList()))
            .subnets(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList()))
            .build());

        copyFrom(profile.fargateProfile());
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        EksClient client = createClient(EksClient.class);

        if (changedFieldNames.contains("tags")) {
            FargateProfile fargateProfile = getFargateProfile(client);
            Map<String, String> currentTags = fargateProfile.tags();
            Map<String, String> tagsToAdd = getTags().entrySet()
                .stream()
                .filter(e -> !currentTags.containsKey(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            Map<String, String> tagsToRemove = currentTags.entrySet()
                .stream()
                .filter(e -> !getTags().containsKey(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            if (!tagsToAdd.isEmpty()) {
                client.tagResource(TagResourceRequest.builder().resourceArn(getArn()).tags(tagsToAdd).build());
            }

            if (!tagsToRemove.isEmpty()) {
                client.untagResource(UntagResourceRequest.builder()
                    .resourceArn(getArn())
                    .tagKeys(tagsToRemove.keySet())
                    .build());
            }
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        EksClient client = createClient(EksClient.class);

        client.deleteFargateProfile(DeleteFargateProfileRequest.builder()
            .clusterName(getCluster().getName())
            .fargateProfileName(getName())
            .build());
    }

    private FargateProfile getFargateProfile(EksClient client) {
        return client.describeFargateProfile(DescribeFargateProfileRequest.builder()
            .clusterName(getCluster().getName())
            .fargateProfileName(getName())
            .build()).fargateProfile();
    }
}
