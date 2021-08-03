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
import java.util.HashSet;
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
import gyro.core.TimeoutSettings;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.CreateFargateProfileRequest;
import software.amazon.awssdk.services.eks.model.CreateFargateProfileResponse;
import software.amazon.awssdk.services.eks.model.DeleteFargateProfileRequest;
import software.amazon.awssdk.services.eks.model.DescribeFargateProfileRequest;
import software.amazon.awssdk.services.eks.model.EksException;
import software.amazon.awssdk.services.eks.model.FargateProfile;
import software.amazon.awssdk.services.eks.model.FargateProfileStatus;
import software.amazon.awssdk.services.eks.model.TagResourceRequest;
import software.amazon.awssdk.services.eks.model.UntagResourceRequest;

/**
 * Creates an eks fargate profile.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::eks-fargate-profile eks-fargate-example
 *         name: "fargate-profile-example"
 *         cluster: $(aws::eks-cluster ex)
 *         pod-execution-role: "arn:aws:iam::242040583208:role/EKS_FARGATE_POD_EXECUTION_ROLE"
 *
 *         selector
 *             namespace: "example-namespace"
 *
 *             labels: {
 *                 "example-label-key": "example-label-value"
 *             }
 *         end
 *
 *         subnets: [
 *             $(aws::subnet "subnet-example-us-east-1a"),
 *             $(aws::subnet "subnet-example-us-east-1b")
 *         ]
 *
 *         tags: {
 *             "example-tag-key": "example-tag-value"
 *         }
 *     end
 */
@Type("eks-fargate-profile")
public class EksFargateProfileResource extends AwsResource implements Copyable<FargateProfile> {

    private String name;
    private EksClusterResource cluster;
    private RoleResource podExecutionRole;
    private List<EksFargateProfileSelector> selector;
    private Set<SubnetResource> subnets;
    private Map<String, String> tags;

    // Read-only
    private String arn;

    /**
     * The name of the fargate profile.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The cluster for which to manage the fargate profile.
     */
    @Required
    public EksClusterResource getCluster() {
        return cluster;
    }

    public void setCluster(EksClusterResource cluster) {
        this.cluster = cluster;
    }

    /**
     * The pod execution role to use for pods in the fargate profile.
     */
    @Required
    public RoleResource getPodExecutionRole() {
        return podExecutionRole;
    }

    public void setPodExecutionRole(RoleResource podExecutionRole) {
        this.podExecutionRole = podExecutionRole;
    }

    /**
     * The selectors to match for pods to use the fargate profile.
     */
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

    /**
     * The subnets where the pods should be launched.
     */
    @Required
    public Set<SubnetResource> getSubnets() {
        if (subnets == null) {
            subnets = new HashSet<>();
        }

        return subnets;
    }

    public void setSubnets(Set<SubnetResource> subnets) {
        this.subnets = subnets;
    }

    /**
     * The tags to attach to the fargate profile.
     */
    @Updatable
    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The Amazon Resource Number (ARN) of the fargate profile.
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
    public void copyFrom(FargateProfile model) {
        setArn(model.fargateProfileArn());
        setName(model.fargateProfileName());
        setCluster(findById(EksClusterResource.class, model.clusterName()));
        setPodExecutionRole(findById(RoleResource.class, model.podExecutionRoleArn()));
        setTags(model.tags());
        setSubnets(model.subnets().stream().map(s -> findById(SubnetResource.class, s)).collect(Collectors.toSet()));
        setSelector(model.selectors().stream().map(s -> {
            EksFargateProfileSelector fargateProfileSelector = newSubresource(EksFargateProfileSelector.class);
            fargateProfileSelector.copyFrom(s);
            return fargateProfileSelector;
        }).collect(Collectors.toList()));
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

        CreateFargateProfileResponse response = client.createFargateProfile(CreateFargateProfileRequest.builder()
            .clusterName(getCluster().getName())
            .fargateProfileName(getName())
            .podExecutionRoleArn(getPodExecutionRole().getArn())
            .tags(getTags())
            .selectors(getSelector().stream()
                .map(EksFargateProfileSelector::toFargateProfileSelector)
                .collect(Collectors.toList()))
            .subnets(getSubnets().stream().map(SubnetResource::getId).collect(Collectors.toList()))
            .build());

        copyFrom(response.fargateProfile());

        Wait.atMost(15, TimeUnit.MINUTES)
            .prompt(false)
            .checkEvery(30, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.CREATE)
            .until(() -> {
                FargateProfile fargateProfile = getFargateProfile(client);
                return fargateProfile != null && fargateProfile.status().equals(FargateProfileStatus.ACTIVE);
            });
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        EksClient client = createClient(EksClient.class);

        if (changedFieldNames.contains("tags")) {
            EksFargateProfileResource currentResource = (EksFargateProfileResource) current;

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

        client.deleteFargateProfile(DeleteFargateProfileRequest.builder()
            .clusterName(getCluster().getName())
            .fargateProfileName(getName())
            .build());

        Wait.atMost(15, TimeUnit.MINUTES)
            .prompt(false)
            .checkEvery(30, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.DELETE)
            .until(() -> getFargateProfile(client) == null);
    }

    private FargateProfile getFargateProfile(EksClient client) {
        FargateProfile profile = null;

        try {
            profile = client.describeFargateProfile(DescribeFargateProfileRequest.builder()
                .clusterName(getCluster().getName())
                .fargateProfileName(getName())
                .build()).fargateProfile();

        } catch (EksException ex) {
            if (!ex.awsErrorDetails().errorCode().equals("ResourceNotFoundException")) {
                throw ex;
            }
        }

        return profile;
    }
}
