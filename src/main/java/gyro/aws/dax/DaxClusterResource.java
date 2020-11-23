/*
 * Copyright 2020, Brightspot.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.dax;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.dax.DaxClient;
import software.amazon.awssdk.services.dax.model.Cluster;
import software.amazon.awssdk.services.dax.model.DescribeClustersResponse;

/**
 * Creates a DAX cluster with the specified Name, Description, IAM Role, Node Type, and Replication Factor.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::dax-cluster cluster-example
 *        name: "kenny-cluster-gyro"
 *        iam-role-arn: "arn:aws:iam::242040583208:role/service-role/DAXtoDynamoDB"
 *        node-type: "dax.r4.large"
 *        replication-factor: 3
 *        description: "test-description"
 *    end
 */
@Type("dax-cluster")
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

    /**
     * The number of active nodes in the cluster.
     */
    @Output
    public Integer getActiveNodes() {
        return activeNodes;
    }

    public void setActiveNodes(Integer activeNodes) {
        this.activeNodes = activeNodes;
    }

    /**
     * The ARN of the cluster.
     */
    @Output
    public String getClusterArn() {
        return clusterArn;
    }

    public void setClusterArn(String clusterArn) {
        this.clusterArn = clusterArn;
    }

    /**
     * The configuration endpoint for the cluster.
     */
    @Output
    public DaxEndpoint getClusterDiscoveryEndpoint() {
        return clusterDiscoveryEndpoint;
    }

    public void setClusterDiscoveryEndpoint(DaxEndpoint clusterDiscoveryEndpoint) {
        this.clusterDiscoveryEndpoint = clusterDiscoveryEndpoint;
    }

    /**
     * The name of the cluster.
     */
    @Id
    @Required
    @Regex(value = "^[a-zA-Z]((?!.*--)[-a-zA-Z0-9]{0,18}[a-z0-9]$)?", message = "a string 1-20 characters long containing letters, numbers, or hyphens. May not contain two consecutive hyphens. The first character must be a letter, and the last may not be a hyphen.")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The description of the cluster.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The ARN of the IAM role being used for the cluster.
     */
    @Required
    public String getIamRoleArn() {
        return iamRoleArn;
    }

    public void setIamRoleArn(String iamRoleArn) {
        this.iamRoleArn = iamRoleArn;
    }

    /**
     * The list of nodes to be removed from the cluster.
     */
    @Output
    public List<String> getNodeIdsToRemove() {
        if (nodeIdsToRemove == null) {
            nodeIdsToRemove = new ArrayList<>();
        }

        return nodeIdsToRemove;
    }

    public void setNodeIdsToRemove(List<String> nodeIdsToRemove) {
        this.nodeIdsToRemove = nodeIdsToRemove;
    }

    /**
     * The list of nodes in the cluster.
     */
    @Output
    public List<DaxNode> getNodes() {
        if (nodes == null) {
            nodes = new ArrayList<>();
        }

        return nodes;
    }

    public void setNodes(List<DaxNode> nodes) {
        this.nodes = nodes;
    }

    /**
     * The compute and memory capacity of the nodes in the cluster.
     */
    @Required
    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * The notification topic and status in a cluster.
     *
     * @subresource gyro.aws.dax.DaxNotificationConfiguration
     */
    @Output
    public DaxNotificationConfiguration getNotificationConfiguration() {
        return notificationConfiguration;
    }

    public void setNotificationConfiguration(DaxNotificationConfiguration notificationConfiguration) {
        this.notificationConfiguration = notificationConfiguration;
    }

    /**
     * The ARN that identifies the notification topic in a cluster.
     */
    @Updatable
    public String getNotificationTopicArn() {
        return notificationTopicArn;
    }

    public void setNotificationTopicArn(String notificationTopicArn) {
        this.notificationTopicArn = notificationTopicArn;
    }

    /**
     * The parameter group used by the nodes in the cluster.
     *
     * @subresource gyro.aws.dax.DaxParameterGroupResource
     */
    @Output
    public DaxParameterGroupResource getParameterGroup() {
        return parameterGroup;
    }

    public void setParameterGroup(DaxParameterGroupResource parameterGroup) {
        this.parameterGroup = parameterGroup;
    }

    /**
     * The parameter group name to be associated with the cluster.
     */
    @Updatable
    public String getParameterGroupName() {
        return parameterGroupName;
    }

    public void setParameterGroupName(String parameterGroupName) {
        this.parameterGroupName = parameterGroupName;
    }

    /**
     * The range of time when maintenance of the cluster will be performed.
     */
    @Updatable
    public String getPreferredMaintenanceWindow() {
        return preferredMaintenanceWindow;
    }

    public void setPreferredMaintenanceWindow(String preferredMaintenanceWindow) {
        this.preferredMaintenanceWindow = preferredMaintenanceWindow;
    }

    /**
     * The list of security groups for the nodes in the cluster.
     *
     * @subresource gyro.aws.dax.DaxSecurityGroupMembership
     */
    @Output
    public List<DaxSecurityGroupMembership> getSecurityGroups() {
        if (securityGroups == null) {
            securityGroups = new ArrayList<>();
        }

        return securityGroups;
    }

    public void setSecurityGroups(List<DaxSecurityGroupMembership> securityGroups) {
        this.securityGroups = securityGroups;
    }

    /**
     * The list of security group IDs assigned to each node in the cluster.
     */
    @Updatable
    public List<String> getSecurityGroupIds() {
        return securityGroupIds;
    }

    public void setSecurityGroupIds(List<String> securityGroupIds) {
        this.securityGroupIds = securityGroupIds;
    }

    /**
     * The description of the server-side encryption status of the cluster.
     */
    @Output
    public DaxSSEDescription getSseDescription() {
        return sseDescription;
    }

    public void setSseDescription(DaxSSEDescription sseDescription) {
        this.sseDescription = sseDescription;
    }

    /**
     * The settings used to enable server-side encryption of the cluster.
     *
     * @subresource gyro.aws.dax.DaxSSESpecification
     */
    public DaxSSESpecification getSseSpecification() {
        return sseSpecification;
    }

    public void setSseSpecification(DaxSSESpecification sseSpecification) {
        this.sseSpecification = sseSpecification;
    }

    /**
     * The current status of the cluster.
     */
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * The subnet group of the cluster.
     *
     * @subresource gyro.aws.dax.DaxSubnetGroupResource
     */
    @Output
    public DaxSubnetGroupResource getSubnetGroup() {
        return subnetGroup;
    }

    public void setSubnetGroup(DaxSubnetGroupResource subnetGroup) {
        this.subnetGroup = subnetGroup;
    }

    /**
     * The name of the subnet group of the cluster.
     */
    public String getSubnetGroupName() {
        return subnetGroupName;
    }

    public void setSubnetGroupName(String subnetGroupName) {
        this.subnetGroupName = subnetGroupName;
    }

    /**
     * The list of tags of the cluster.
     *
     * @subresource gyro.aws.dax.DaxTag
     */
    public List<DaxTag> getTags() {
        if (tags == null) {
            tags = new ArrayList<>();
        }

        return tags;
    }

    public void setTags(List<DaxTag> tags) {
        this.tags = tags;
    }

    /**
     * The total number of nodes in the cluster.
     */
    @Output
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
