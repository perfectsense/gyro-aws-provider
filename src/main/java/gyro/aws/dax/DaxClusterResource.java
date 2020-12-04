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
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.RoleResource;
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
 * Creates a DAX cluster.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::dax-cluster cluster-example
 *         name: "cluster-gyro-example"
 *         iam-role: $(aws::iam-role iam-role-dax-cluster-example)
 *         node-type: "dax.r4.large"
 *         replication-factor: 1
 *         description: "dax-cluster-example-description"
 *         parameter-group: $(aws::dax-parameter-group parameter-group)
 *         subnet-group: $(aws::dax-subnet-group dax-cluster-subnet-group)
 *
 *         security-group
 *             security-group: $(aws::security-group security-group-dax-cluster-example-1)
 *         end
 *     end
 */
@Type("dax-cluster")
public class DaxClusterResource extends AwsResource implements Copyable<Cluster> {

    private Integer activeNodes;
    private String clusterArn;
    private DaxEndpoint clusterDiscoveryEndpoint;
    private String name;
    private String description;
    private RoleResource iamRole;
    private List<String> nodeIdsToRemove;
    private List<DaxNode> nodes;
    private String nodeType;
    private Integer replicationFactor;
    private List<String> availabilityZones;
    private DaxNotificationConfiguration notificationConfiguration;
    private DaxParameterGroupResource parameterGroup;
    private String preferredMaintenanceWindow;
    private List<DaxSecurityGroupMembership> securityGroup;
    private DaxSSEDescription sseDescription;
    private DaxSSESpecification sseSpecification;
    private String status;
    private DaxSubnetGroupResource subnetGroup;
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
    @Id
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
     * The IAM role being used for the cluster.
     */
    @Required
    public RoleResource getIamRole() {
        return iamRole;
    }

    public void setIamRole(RoleResource iamRole) {
        this.iamRole = iamRole;
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
     * The number of nodes in the cluster.
     */
    @Required
    public Integer getReplicationFactor() {
        return replicationFactor;
    }

    public void setReplicationFactor(Integer replicationFactor) {
        this.replicationFactor = replicationFactor;
    }

    /**
     * The list of availability zones of the cluster.
     */
    public List<String> getAvailabilityZones() {
        if (availabilityZones == null) {
            availabilityZones = new ArrayList<>();
        }

        return availabilityZones;
    }

    public void setAvailabilityZones(List<String> availabilityZones) {
        this.availabilityZones = availabilityZones;
    }

    /**
     * The notification topic and status of a cluster.
     *
     * @subresource gyro.aws.dax.DaxNotificationConfiguration
     */
    public DaxNotificationConfiguration getNotificationConfiguration() {
        return notificationConfiguration;
    }

    public void setNotificationConfiguration(DaxNotificationConfiguration notificationConfiguration) {
        this.notificationConfiguration = notificationConfiguration;
    }

    /**
     * The parameter group used by the nodes in the cluster.
     */
    @Updatable
    public DaxParameterGroupResource getParameterGroup() {
        return parameterGroup;
    }

    public void setParameterGroup(DaxParameterGroupResource parameterGroup) {
        this.parameterGroup = parameterGroup;
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
    @Updatable
    public List<DaxSecurityGroupMembership> getSecurityGroup() {
        if (securityGroup == null) {
            securityGroup = new ArrayList<>();
        }

        return securityGroup;
    }

    public void setSecurityGroup(List<DaxSecurityGroupMembership> securityGroups) {
        this.securityGroup = securityGroups;
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
     */
    public DaxSubnetGroupResource getSubnetGroup() {
        return subnetGroup;
    }

    public void setSubnetGroup(DaxSubnetGroupResource subnetGroup) {
        this.subnetGroup = subnetGroup;
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
        setIamRole(!ObjectUtils.isBlank(getIamRole()) ? getIamRole() : null);
        setNodeIdsToRemove(model.nodeIdsToRemove());
        setNodeType(model.nodeType());
        setParameterGroup(findById(DaxParameterGroupResource.class, model.parameterGroup().parameterGroupName()));
        setPreferredMaintenanceWindow(model.preferredMaintenanceWindow());
        setStatus(model.status());
        setSubnetGroup(findById(DaxSubnetGroupResource.class, model.subnetGroup()));
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

        getSecurityGroup().clear();
        if (model.securityGroups() != null) {
            model.securityGroups().forEach(membership -> {
                DaxSecurityGroupMembership securityGroupMembership = newSubresource(DaxSecurityGroupMembership.class);
                securityGroupMembership.copyFrom(membership);
                getSecurityGroup().add(securityGroupMembership);
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
            .availabilityZones(getAvailabilityZones())
            .clusterName(getName())
            .description(getDescription())
            .iamRoleArn(getIamRole().getArn())
            .nodeType(getNodeType())
            .notificationTopicArn(getNotificationConfiguration() != null ? getNotificationConfiguration().getTopic().getArn() : null)
            .parameterGroupName(getParameterGroup() != null ? getParameterGroup().getName() : null)
            .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
            .replicationFactor(getReplicationFactor())
            .securityGroupIds(getSecurityGroup().stream().map(o -> o.getSecurityGroup().getId()).collect(Collectors.toList()))
            .sseSpecification(getSseSpecification() != null ? getSseSpecification().toSseSpecification() : null)
            .subnetGroupName(getSubnetGroup() != null ? getSubnetGroup().getName() : null)
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
            .notificationTopicArn(getNotificationConfiguration() != null ? getNotificationConfiguration().getTopic().getArn() : null)
            .notificationTopicStatus(
                getNotificationConfiguration() != null ? getNotificationConfiguration().getTopicStatus() : null)
            .parameterGroupName(getParameterGroup() != null ? getParameterGroup().getName() : null)
            .preferredMaintenanceWindow(getPreferredMaintenanceWindow())
            .securityGroupIds(getSecurityGroup().stream().map(o -> o.getSecurityGroup().getId()).collect(Collectors.toList()))
        );
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        DaxClient client = createClient(DaxClient.class);

        client.deleteCluster(r -> r.clusterName(getName()));
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if ((configuredFields.contains("availability-zones") && configuredFields.contains("replication-factor"))
            && getReplicationFactor() != getAvailabilityZones().size()) {
            errors.add(new ValidationError(
                this,
                null,
                "If 'availability-zones' and 'replication-factor' are present, the value of 'replication-factor' "
                    + "must equal the size of 'availability-zones"
            ));
        }

        return errors;
    }
}
