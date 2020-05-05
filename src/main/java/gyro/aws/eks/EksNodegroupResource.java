/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.eks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.ec2.SubnetResource;
import gyro.aws.iam.RoleResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.AMITypes;
import software.amazon.awssdk.services.eks.model.CreateNodegroupRequest;
import software.amazon.awssdk.services.eks.model.CreateNodegroupResponse;
import software.amazon.awssdk.services.eks.model.DeleteNodegroupRequest;
import software.amazon.awssdk.services.eks.model.DescribeNodegroupRequest;
import software.amazon.awssdk.services.eks.model.EksException;
import software.amazon.awssdk.services.eks.model.Nodegroup;
import software.amazon.awssdk.services.eks.model.NodegroupResources;
import software.amazon.awssdk.services.eks.model.NodegroupStatus;
import software.amazon.awssdk.services.eks.model.TagResourceRequest;
import software.amazon.awssdk.services.eks.model.UntagResourceRequest;
import software.amazon.awssdk.services.eks.model.UpdateLabelsPayload;
import software.amazon.awssdk.services.eks.model.UpdateNodegroupConfigRequest;
import software.amazon.awssdk.services.eks.model.UpdateNodegroupVersionRequest;

/**
 * Creates an eks nodegroup.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::eks-nodegroup eks-nodegroup-example
 *         name: "example-eks-nodegroup"
 *         cluster: $(aws::eks-cluster ex)
 *         node-role: "arn:aws:iam::242040583208:role/EKS_NODEGROUP_ROLE"
 *
 *         subnets: [
 *             $(aws::subnet "subnet-example-us-east-1a"),
 *             $(aws::subnet "subnet-example-us-east-1b")
 *         ]
 *
 *         labels: {
 *             "example-label-key": "example-label-value"
 *         }
 *     end
 */
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

    /**
     * The name of the nodegroup. (Required)
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The name of the cluster for which to manage the nodegroup. (Required)
     */
    @Required
    public EksClusterResource getCluster() {
        return cluster;
    }

    public void setCluster(EksClusterResource cluster) {
        this.cluster = cluster;
    }

    /**
     * The Kubernetes version to use for your managed nodes. Defaults to ``1.15``.
     */
    @Updatable
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * The AMI version of the Amazon EKS-optimized AMI to use with your node group. Defaults to ``1.15.10-20200228``.
     */
    @Updatable
    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    /**
     * The scaling configuration details for the Auto Scaling group that is created for your node group.
     */
    @Updatable
    public EksNodegroupScalingConfig getScalingConfig() {
        return scalingConfig;
    }

    public void setScalingConfig(EksNodegroupScalingConfig scalingConfig) {
        this.scalingConfig = scalingConfig;
    }

    /**
     * The instance types to use for your node group. Defaults to ``t3.medium``.
     */
    public List<String> getInstanceTypes() {
        if (instanceTypes == null) {
            instanceTypes = new ArrayList<>();
        }

        return instanceTypes;
    }

    public void setInstanceTypes(List<String> instanceTypes) {
        this.instanceTypes = instanceTypes;
    }

    /**
     * The subnets to use for the Auto Scaling group that is created for your node group. (Required)
     */
    @Required
    public List<SubnetResource> getSubnets() {
        if (subnets == null) {
            subnets = new ArrayList<>();
        }

        return subnets;
    }

    public void setSubnets(List<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    /**
     * The remote access (SSH) configuration for the node group.
     */
    public EksNodegroupRemoteAccessConfig getRemoteAccess() {
        return remoteAccess;
    }

    public void setRemoteAccess(EksNodegroupRemoteAccessConfig remoteAccess) {
        this.remoteAccess = remoteAccess;
    }

    /**
     * The Ami type of the node group.
     */
    public AMITypes getAmiType() {
        return amiType;
    }

    public void setAmiType(AMITypes amiType) {
        this.amiType = amiType;
    }

    /**
     * The IAM role to use for the nodegroup. (Required)
     */
    @Required
    public RoleResource getNodeRole() {
        return nodeRole;
    }

    public void setNodeRole(RoleResource nodeRole) {
        this.nodeRole = nodeRole;
    }

    /**
     * The Kubernetes labels to be applied to the nodes in the node group when they are created.
     */
    @Updatable
    public Map<String, String> getLabels() {
        if (labels == null) {
            labels = new HashMap<>();
        }

        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    /**
     * The root device disk size in GiB for the node group instances. Defaults to ``20``.
     */
    public Integer getDiskSize() {
        return diskSize;
    }

    public void setDiskSize(Integer diskSize) {
        this.diskSize = diskSize;
    }

    /**
     * The tags to attach to the nodegroup.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The Amazon Resource Number (ARN) of the nodegroup.
     */
    @Output
    @Id
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

        if (model.scalingConfig() != null) {
            EksNodegroupScalingConfig scalingConfig = newSubresource(EksNodegroupScalingConfig.class);
            scalingConfig.copyFrom(model.scalingConfig());
            setScalingConfig(scalingConfig);
        }

        if (model.remoteAccess() != null) {
            EksNodegroupRemoteAccessConfig remoteAccessConfig = newSubresource(EksNodegroupRemoteAccessConfig.class);
            remoteAccessConfig.copyFrom(model.remoteAccess());
            setRemoteAccess(remoteAccessConfig);
        }
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

        CreateNodegroupRequest.Builder builder = CreateNodegroupRequest.builder()
            .nodegroupName(getName())
            .clusterName(getCluster().getName())
            .version(getVersion())
            .releaseVersion(getReleaseVersion())
            .instanceTypes(getInstanceTypes())
            .subnets(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList()))
            .amiType(getAmiType())
            .nodeRole(getNodeRole().getArn())
            .labels(getLabels())
            .diskSize(getDiskSize())
            .tags(getTags());

        if (getScalingConfig() != null) {
            builder = builder.scalingConfig(getScalingConfig().toNodegroupScalingConfig());
        }

        if (getRemoteAccess() != null) {
            builder = builder.remoteAccess(getRemoteAccess().toRemoteAccessConfig());
        }

        CreateNodegroupResponse response = client.createNodegroup(builder.build());

        copyFrom(response.nodegroup());

        waitForActiveState(client);
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        EksClient client = createClient(EksClient.class);
        EksNodegroupResource currentResource = (EksNodegroupResource) current;

        if (changedFieldNames.contains("release-version")) {
            client.updateNodegroupVersion(UpdateNodegroupVersionRequest.builder()
                    .clusterName(getCluster().getName())
                    .nodegroupName(getName())
                    .releaseVersion(getReleaseVersion())
                    .build());
        }

        if (changedFieldNames.contains("version")) {
            client.updateNodegroupVersion(UpdateNodegroupVersionRequest.builder()
                    .clusterName(getCluster().getName())
                    .nodegroupName(getName())
                    .version(getVersion())
                    .build());
        }

        if (changedFieldNames.contains("labels")) {
            if (!currentResource.getLabels().isEmpty()) {
                client.updateNodegroupConfig(UpdateNodegroupConfigRequest.builder()
                        .clusterName(getCluster().getName())
                        .labels(UpdateLabelsPayload.builder()
                                .removeLabels(currentResource.getLabels().keySet())
                                .build())
                        .nodegroupName(getName())
                        .build());
            }

            client.updateNodegroupConfig(UpdateNodegroupConfigRequest.builder()
                    .clusterName(getCluster().getName())
                    .labels(UpdateLabelsPayload.builder()
                            .addOrUpdateLabels(currentResource.getLabels())
                            .build())
                    .nodegroupName(getName())
                    .build());

            waitForActiveState(client);
        }

        if (changedFieldNames.contains("scaling-config")) {
            client.updateNodegroupConfig(UpdateNodegroupConfigRequest.builder()
                    .clusterName(getCluster().getName())
                    .nodegroupName(getName())
                    .scalingConfig(getScalingConfig() == null ? null : getScalingConfig().toNodegroupScalingConfig())
                    .build());
        }

        if (changedFieldNames.contains("tags")) {
            if (!currentResource.getTags().isEmpty()) {
                client.untagResource(UntagResourceRequest.builder()
                        .resourceArn(getArn())
                        .tagKeys(currentResource.getTags().keySet())
                        .build());
            }

            client.tagResource(TagResourceRequest.builder().resourceArn(getArn()).tags(getTags()).build());
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        EksClient client = createClient(EksClient.class);

        client.deleteNodegroup(DeleteNodegroupRequest.builder()
            .clusterName(getCluster().getName())
            .nodegroupName(getName())
            .build());

        Wait.atMost(15, TimeUnit.MINUTES)
            .prompt(false)
            .checkEvery(5, TimeUnit.MINUTES)
            .until(() -> getNodegroup(client) == null);
    }

    private Nodegroup getNodegroup(EksClient client) {
        Nodegroup nodegroup = null;

        try {
            nodegroup = client.describeNodegroup(DescribeNodegroupRequest.builder()
                .clusterName(getCluster().getName())
                .nodegroupName(getName())
                .build()).nodegroup();

        } catch (EksException ex) {
            if (!ex.awsErrorDetails().errorCode().equals("ResourceNotFoundException")) {
                throw ex;
            }
        }

        return nodegroup;
    }

    private void waitForActiveState(EksClient client) {
        Wait.atMost(15, TimeUnit.MINUTES)
                .prompt(false)
                .checkEvery(5, TimeUnit.MINUTES)
                .until(() -> {
                    Nodegroup nodegroup = getNodegroup(client);
                    return nodegroup != null && nodegroup.status().equals(NodegroupStatus.ACTIVE);
                });
    }
}
