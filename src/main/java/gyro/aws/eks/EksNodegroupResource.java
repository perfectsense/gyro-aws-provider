package gyro.aws.eks;

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
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.AMITypes;
import software.amazon.awssdk.services.eks.model.CreateNodegroupRequest;
import software.amazon.awssdk.services.eks.model.CreateNodegroupResponse;
import software.amazon.awssdk.services.eks.model.DeleteNodegroupRequest;
import software.amazon.awssdk.services.eks.model.DescribeNodegroupRequest;
import software.amazon.awssdk.services.eks.model.Nodegroup;
import software.amazon.awssdk.services.eks.model.TagResourceRequest;
import software.amazon.awssdk.services.eks.model.UntagResourceRequest;
import software.amazon.awssdk.services.eks.model.UpdateLabelsPayload;
import software.amazon.awssdk.services.eks.model.UpdateNodegroupConfigRequest;
import software.amazon.awssdk.services.eks.model.UpdateNodegroupVersionRequest;

@Type("eks-nodegroup")
public class EksNodegroupResource extends AwsResource implements Copyable<Nodegroup> {

    private String name;
    private EksClusterResource cluster;
    private String version;
    private String releaseVersion;
    private EksNodegroupScalingConfig scalingConfig;
    private List<String> instanceTypes;
    private List<SubnetResource> subnets;
    private EksNodegroupRemoteAccessConfig remoteAccess;
    private AMITypes amiType;
    private RoleResource nodeRole;
    private Map<String, String> labels;
    private Integer diskSize;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public EksNodegroupScalingConfig getScalingConfig() {
        return scalingConfig;
    }

    public void setScalingConfig(EksNodegroupScalingConfig scalingConfig) {
        this.scalingConfig = scalingConfig;
    }

    public List<String> getInstanceTypes() {
        return instanceTypes;
    }

    public void setInstanceTypes(List<String> instanceTypes) {
        this.instanceTypes = instanceTypes;
    }

    public List<SubnetResource> getSubnets() {
        return subnets;
    }

    public void setSubnets(List<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    public EksNodegroupRemoteAccessConfig getRemoteAccess() {
        return remoteAccess;
    }

    public void setRemoteAccess(EksNodegroupRemoteAccessConfig remoteAccess) {
        this.remoteAccess = remoteAccess;
    }

    public AMITypes getAmiType() {
        return amiType;
    }

    public void setAmiType(AMITypes amiType) {
        this.amiType = amiType;
    }

    public RoleResource getNodeRole() {
        return nodeRole;
    }

    public void setNodeRole(RoleResource nodeRole) {
        this.nodeRole = nodeRole;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public Integer getDiskSize() {
        return diskSize;
    }

    public void setDiskSize(Integer diskSize) {
        this.diskSize = diskSize;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public void copyFrom(Nodegroup model) {
        setArn((model.nodegroupArn()));
        setName((model.nodegroupName()));
        setCluster(findById(EksClusterResource.class, model.clusterName()));
        setVersion((model.version()));
        setReleaseVersion((model.releaseVersion()));
        setInstanceTypes((model.instanceTypes()));
        setAmiType((model.amiType()));
        setLabels((model.labels()));
        setDiskSize((model.diskSize()));
        setTags((model.tags()));
        setSubnets((model.subnets().stream().map(s -> findById(SubnetResource.class, s)).collect(Collectors.toList())));
        setNodeRole(findById(RoleResource.class, model.nodeRole()));

        EksNodegroupScalingConfig scalingConfig = newSubresource(EksNodegroupScalingConfig.class);
        scalingConfig.copyFrom(model.scalingConfig());
        setScalingConfig(scalingConfig);

        EksNodegroupRemoteAccessConfig remoteAccessConfig = newSubresource(EksNodegroupRemoteAccessConfig.class);
        remoteAccessConfig.copyFrom(model.remoteAccess());
        setRemoteAccess(remoteAccessConfig);
    }

    @Override
    public boolean refresh() {
        EksClient client = createClient(EksClient.class);

        Nodegroup nodegroup = getNodegroup(client);

        if (nodegroup == null) {
            return false;
        }

        copyFrom(nodegroup);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        EksClient client = createClient(EksClient.class);

        CreateNodegroupResponse nodegroup = client.createNodegroup(CreateNodegroupRequest.builder()
            .nodegroupName(getName())
            .clusterName(getCluster().getName())
            .version(getVersion())
            .releaseVersion(getReleaseVersion())
            .scalingConfig(getScalingConfig().toNodegroupScalingConfig())
            .instanceTypes(getInstanceTypes())
            .subnets(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList()))
            .remoteAccess(getRemoteAccess().toRemoteAccessConfig())
            .amiType(getAmiType())
            .nodeRole(getNodeRole().getArn())
            .labels(getLabels())
            .diskSize(getDiskSize())
            .tags(getTags())
            .build());

        copyFrom(nodegroup.nodegroup());
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        EksClient client = createClient(EksClient.class);

        if (changedFieldNames.contains("version") || changedFieldNames.contains("release-version")) {
            client.updateNodegroupVersion(UpdateNodegroupVersionRequest.builder()
                .clusterName(getCluster().getName())
                .nodegroupName(getName())
                .releaseVersion(getReleaseVersion())
                .version(getVersion())
                .build());
        }

        if (changedFieldNames.contains("scaling-config") || changedFieldNames.contains("labels")) {
            Nodegroup nodegroup = getNodegroup(client);
            Map<String, String> currentLabels = nodegroup.labels();
            Map<String, String> labelsToAdd = getLabels().entrySet()
                .stream()
                .filter(e -> !currentLabels.containsKey(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            Map<String, String> labelsToRemove = currentLabels.entrySet()
                .stream()
                .filter(e -> !getLabels().containsKey(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            client.updateNodegroupConfig(UpdateNodegroupConfigRequest.builder()
                .clusterName(getCluster().getName())
                .labels(UpdateLabelsPayload.builder()
                    .addOrUpdateLabels(labelsToAdd)
                    .removeLabels(labelsToRemove.keySet())
                    .build())
                .nodegroupName(getName())
                .scalingConfig(getScalingConfig().toNodegroupScalingConfig())
                .build());
        }

        if (changedFieldNames.contains("tags")) {
            Nodegroup nodegroup = getNodegroup(client);
            Map<String, String> currentTags = nodegroup.tags();
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

        client.deleteNodegroup(DeleteNodegroupRequest.builder()
            .clusterName(getCluster().getName())
            .nodegroupName(getName())
            .build());
    }

    private Nodegroup getNodegroup(EksClient client) {
        return client.describeNodegroup(DescribeNodegroupRequest.builder()
            .clusterName(getCluster().getName())
            .nodegroupName(getName())
            .build()).nodegroup();
    }
}
