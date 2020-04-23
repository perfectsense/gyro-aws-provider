package gyro.aws.eks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.RoleResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.Cluster;
import software.amazon.awssdk.services.eks.model.CreateClusterRequest;
import software.amazon.awssdk.services.eks.model.CreateClusterResponse;
import software.amazon.awssdk.services.eks.model.DeleteClusterRequest;
import software.amazon.awssdk.services.eks.model.DescribeClusterRequest;
import software.amazon.awssdk.services.eks.model.TagResourceRequest;
import software.amazon.awssdk.services.eks.model.UntagResourceRequest;
import software.amazon.awssdk.services.eks.model.UpdateClusterConfigRequest;
import software.amazon.awssdk.services.eks.model.UpdateClusterVersionRequest;

@Type("eks-cluster")
public class EksClusterResource extends AwsResource implements Copyable<Cluster> {

    private String name;
    private RoleResource role;
    private String version;
    private EksVpcConfig vpcConfig;
    private EksLogging logging;
    private List<EksEncryptionConfig> encryptionConfig;
    private Map<String, String> tags;

    // Read-only
    private String arn;

    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoleResource getRole() {
        return role;
    }

    public void setRole(RoleResource role) {
        this.role = role;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public EksVpcConfig getVpcConfig() {
        return vpcConfig;
    }

    public void setVpcConfig(EksVpcConfig vpcConfig) {
        this.vpcConfig = vpcConfig;
    }

    public EksLogging getLogging() {
        return logging;
    }

    public void setLogging(EksLogging logging) {
        this.logging = logging;
    }

    public List<EksEncryptionConfig> getEncryptionConfig() {
        if (encryptionConfig == null) {
            encryptionConfig = new ArrayList<>();
        }

        return encryptionConfig;
    }

    public void setEncryptionConfig(List<EksEncryptionConfig> encryptionConfig) {
        this.encryptionConfig = encryptionConfig;
    }

    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public void copyFrom(Cluster model) {
        setName(model.name());
        setRole(findById(RoleResource.class, model.roleArn()));
        setVersion(model.version());
        setArn(model.arn());

        EksVpcConfig eksVpcConfig = newSubresource(EksVpcConfig.class);
        eksVpcConfig.copyFrom(model.resourcesVpcConfig());
        setVpcConfig(eksVpcConfig);

        EksLogging eksLogging = newSubresource(EksLogging.class);
        eksLogging.copyFrom(model.logging());
        setLogging(eksLogging);

    }

    @Override
    public boolean refresh() {
        EksClient client = createClient(EksClient.class);

        Cluster cluster = getCluster(client);

        if (cluster == null) {
            return false;
        }

        copyFrom(cluster);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        EksClient client = createClient(EksClient.class);

        CreateClusterResponse cluster = client.createCluster(CreateClusterRequest.builder()
            .name(getName())
            .roleArn(getRole().getArn())
            .resourcesVpcConfig(getVpcConfig().toVpcConfigRequest())
            .version(getVersion())
            .logging(getLogging().toLogging())
            .encryptionConfig(getEncryptionConfig().stream()
                .map(EksEncryptionConfig::toEncryptionConfig)
                .collect(Collectors.toList()))
            .tags(getTags())
            .build());

        copyFrom(cluster.cluster());
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        EksClient client = createClient(EksClient.class);

        if (changedFieldNames.contains("version")) {
            client.updateClusterVersion(UpdateClusterVersionRequest.builder()
                .name(getName())
                .version(getVersion())
                .build());
        }

        if (changedFieldNames.contains("tags")) {
            Cluster fargateProfile = getCluster(client);
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

        if (changedFieldNames.contains("logging") || changedFieldNames.contains("vpc-config")) {
            client.updateClusterConfig(UpdateClusterConfigRequest.builder()
                .name(getName())
                .logging(getLogging().toLogging())
                .resourcesVpcConfig(getVpcConfig().toVpcConfigRequest())
                .build());
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        EksClient client = createClient(EksClient.class);

        client.deleteCluster(DeleteClusterRequest.builder().name(getName()).build());
    }

    private Cluster getCluster(EksClient client) {
        return client.describeCluster(DescribeClusterRequest.builder().name(getName()).build()).cluster();
    }
}
