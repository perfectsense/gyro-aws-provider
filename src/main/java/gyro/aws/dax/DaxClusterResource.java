package gyro.aws.dax;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.dax.DaxClient;
import software.amazon.awssdk.services.dax.model.Cluster;
import software.amazon.awssdk.services.dax.model.DescribeClustersResponse;

public class DaxClusterResource extends AwsResource implements Copyable<Cluster> {

    private Integer activeNodes;
    private String clusterArn;
    private DaxEndpoint clusterDiscoveryEndpoint;
    private String name;
    private String description;
    private String iamRoleArn;
    private List<String> nodeIdsToRemove;
    private List<DaxNode> nodes;
    private String nodeType;
    private DaxNotificationConfiguration notificationConfiguration;
    private String notificationTopicArn;
    private DaxParameterGroupResource parameterGroup;
    private String parameterGroupName;
    private String preferredMaintenanceWindow;
    private List<DaxSecurityGroupMembership> securityGroups;
    private List<String> securityGroupIds;
    private DaxSSEDescription sseDescription;
    private DaxSSESpecification sseSpecification;
    private String status;
    private DaxSubnetGroupResource subnetGroup;
    private String subnetGroupName;
    private List<DaxTag> tags;
    private Integer totalNodes;

    public Integer getActiveNodes() {
        return activeNodes;
    }

    public void setActiveNodes(Integer activeNodes) {
        this.activeNodes = activeNodes;
    }

    public String getClusterArn() {
        return clusterArn;
    }

    public void setClusterArn(String clusterArn) {
        this.clusterArn = clusterArn;
    }

    public DaxEndpoint getClusterDiscoveryEndpoint() {
        return clusterDiscoveryEndpoint;
    }

    public void setClusterDiscoveryEndpoint(DaxEndpoint clusterDiscoveryEndpoint) {
        this.clusterDiscoveryEndpoint = clusterDiscoveryEndpoint;
    }

    @Id
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIamRoleArn() {
        return iamRoleArn;
    }

    public void setIamRoleArn(String iamRoleArn) {
        this.iamRoleArn = iamRoleArn;
    }

    public List<String> getNodeIdsToRemove() {
        if (nodeIdsToRemove == null) {
            nodeIdsToRemove = new ArrayList<>();
        }

        return nodeIdsToRemove;
    }

    public void setNodeIdsToRemove(List<String> nodeIdsToRemove) {
        this.nodeIdsToRemove = nodeIdsToRemove;
    }

    public List<DaxNode> getNodes() {
        if (nodes == null) {
            nodes = new ArrayList<>();
        }

        return nodes;
    }

    public void setNodes(List<DaxNode> nodes) {
        this.nodes = nodes;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public DaxNotificationConfiguration getNotificationConfiguration() {
        return notificationConfiguration;
    }

    public void setNotificationConfiguration(DaxNotificationConfiguration notificationConfiguration) {
        this.notificationConfiguration = notificationConfiguration;
    }

    public String getNotificationTopicArn() {
        return notificationTopicArn;
    }

    public void setNotificationTopicArn(String notificationTopicArn) {
        this.notificationTopicArn = notificationTopicArn;
    }

    public DaxParameterGroupResource getParameterGroup() {
        return parameterGroup;
    }

    public void setParameterGroup(DaxParameterGroupResource parameterGroup) {
        this.parameterGroup = parameterGroup;
    }

    public String getParameterGroupName() {
        return parameterGroupName;
    }

    public void setParameterGroupName(String parameterGroupName) {
        this.parameterGroupName = parameterGroupName;
    }

    public String getPreferredMaintenanceWindow() {
        return preferredMaintenanceWindow;
    }

    public void setPreferredMaintenanceWindow(String preferredMaintenanceWindow) {
        this.preferredMaintenanceWindow = preferredMaintenanceWindow;
    }

    public List<DaxSecurityGroupMembership> getSecurityGroups() {
        if (securityGroups == null) {
            securityGroups = new ArrayList<>();
        }

        return securityGroups;
    }

    public void setSecurityGroups(List<DaxSecurityGroupMembership> securityGroups) {
        this.securityGroups = securityGroups;
    }

    public List<String> getSecurityGroupIds() {
        return securityGroupIds;
    }

    public void setSecurityGroupIds(List<String> securityGroupIds) {
        this.securityGroupIds = securityGroupIds;
    }

    public DaxSSEDescription getSseDescription() {
        return sseDescription;
    }

    public void setSseDescription(DaxSSEDescription sseDescription) {
        this.sseDescription = sseDescription;
    }

    public DaxSSESpecification getSseSpecification() {
        return sseSpecification;
    }

    public void setSseSpecification(DaxSSESpecification sseSpecification) {
        this.sseSpecification = sseSpecification;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public DaxSubnetGroupResource getSubnetGroup() {
        return subnetGroup;
    }

    public void setSubnetGroup(DaxSubnetGroupResource subnetGroup) {
        this.subnetGroup = subnetGroup;
    }

    public String getSubnetGroupName() {
        return subnetGroupName;
    }

    public void setSubnetGroupName(String subnetGroupName) {
        this.subnetGroupName = subnetGroupName;
    }

    public List<DaxTag> getTags() {
        if (tags == null) {
            tags = new ArrayList<>();
        }

        return tags;
    }

    public void setTags(List<DaxTag> tags) {
        this.tags = tags;
    }

    public Integer getTotalNodes() {
        return totalNodes;
    }

    public void setTotalNodes(Integer totalNodes) {
        this.totalNodes = totalNodes;
    }

    @Override
    public void copyFrom(Cluster model) {
        setActiveNodes(model.activeNodes());
        setClusterArn(model.clusterArn());
        setName(model.clusterName());
        setDescription(model.description());
        setIamRoleArn(model.iamRoleArn());
        setNodeIdsToRemove(model.nodeIdsToRemove());
        setNodeType(model.nodeType());
        setParameterGroup(findById(DaxParameterGroupResource.class, model.parameterGroup()));
        setParameterGroupName(model.parameterGroup() != null ? model.parameterGroup().parameterGroupName() : null);
        setPreferredMaintenanceWindow(model.preferredMaintenanceWindow());
        setStatus(model.status());
        setSubnetGroup(findById(DaxSubnetGroupResource.class, model.subnetGroup()));
        setSubnetGroupName(model.subnetGroup());
        setTotalNodes(model.totalNodes());

        setClusterDiscoveryEndpoint(null);
        if (model.clusterDiscoveryEndpoint() != null) {
            DaxEndpoint endpoint = newSubresource(DaxEndpoint.class);
            endpoint.copyFrom(model.clusterDiscoveryEndpoint());
            setClusterDiscoveryEndpoint(endpoint);
        }

        getNodes().clear();
        if (model.nodes() != null) {
            model.nodes().forEach(node -> {
                DaxNode daxNode = newSubresource(DaxNode.class);
                daxNode.copyFrom(node);
                getNodes().add(daxNode);
            });
        }

        setNotificationConfiguration(null);
        if (model.notificationConfiguration() != null) {
            DaxNotificationConfiguration notificationConfiguration = newSubresource(DaxNotificationConfiguration.class);
            notificationConfiguration.copyFrom(model.notificationConfiguration());
            setNotificationConfiguration(notificationConfiguration);
        }

        getSecurityGroups().clear();
        getSecurityGroupIds().clear();
        if (model.securityGroups() != null) {
            model.securityGroups().forEach(membership -> {
                DaxSecurityGroupMembership securityGroupMembership = newSubresource(DaxSecurityGroupMembership.class);
                securityGroupMembership.copyFrom(membership);
                getSecurityGroups().add(securityGroupMembership);
                getSecurityGroupIds().add(securityGroupMembership.getSecurityGroupIdentifier());
            });
        }

        setSseDescription(null);
        if (model.sseDescription() != null) {
            DaxSSEDescription sseDescription = newSubresource(DaxSSEDescription.class);
            sseDescription.copyFrom(model.sseDescription());
            setSseDescription(sseDescription);
        }
    }

    @Override
    public boolean refresh() {
        DaxClient client = createClient(DaxClient.class);
        DescribeClustersResponse response;

        response = client.describeClusters(r -> r.clusterNames(getName()));

        if (response == null || response.clusters().isEmpty()) {
            return false;
        }

        copyFrom(response.clusters().get(0));
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        DaxClient client = createClient(DaxClient.class);

        client.createCluster(r -> r
            .clusterName(getName())
            .availabilityZones(getNodes().stream().map(DaxNode::getAvailabilityZone).collect(Collectors.toList()))
            .description(getDescription())
            .notificationTopicArn(getNotificationTopicArn())
            .parameterGroupName(getParameterGroupName())
            .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
            .securityGroupIds(getSecurityGroupIds())
            .sseSpecification(getSseSpecification() != null ? getSseSpecification().toSseSpecification() : null)
            .subnetGroupName(getSubnetGroupName())
            .tags(DaxTag.toTags(getTags()))
            .build()
        );

        refresh();
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        DaxClient client = createClient(DaxClient.class);

        client.updateCluster(r -> r
            .clusterName(getName())
            .description(getDescription())
            .notificationTopicArn(getNotificationTopicArn())
            .notificationTopicStatus(
                getNotificationConfiguration() != null ? getNotificationConfiguration().getTopicStatus() : null)
            .parameterGroupName(getParameterGroupName())
            .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
            .securityGroupIds(getSecurityGroupIds())
        );

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        DaxClient client = createClient(DaxClient.class);

        client.deleteCluster(r -> r.clusterName(getName()));
    }
}
