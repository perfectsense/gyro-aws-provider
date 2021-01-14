/*
 * Copyright 2021, Perfect Sense, Inc.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
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
import software.amazon.awssdk.services.eks.model.Addon;
import software.amazon.awssdk.services.eks.model.AddonStatus;
import software.amazon.awssdk.services.eks.model.CreateAddonResponse;
import software.amazon.awssdk.services.eks.model.ResourceNotFoundException;
import software.amazon.awssdk.services.eks.model.TagResourceRequest;
import software.amazon.awssdk.services.eks.model.UntagResourceRequest;

/**
 * Creates an eks addon.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::eks-addon example-addon
 *         addon-name: vpc-cni
 *         cluster: $(aws::eks-cluster ex)
 *
 *         tags : {
 *             "example-tag-key": "example-key-value"
 *         }
 *     end
 */
@Type("eks-addon")
public class EksAddonResource extends AwsResource implements Copyable<Addon> {

    private String addonName;
    private String addonVersion;
    private EksClusterResource cluster;
    private RoleResource serviceAccountRole;
    private Map<String, String> tags;

    // Read-only

    private String arn;

    /**
     * The name of the add-on.
     */
    @Required
    @Id
    public String getAddonName() {
        return addonName;
    }

    public void setAddonName(String addonName) {
        this.addonName = addonName;
    }

    /**
     * The version of the add-on.
     */
    @Updatable
    public String getAddonVersion() {
        return addonVersion;
    }

    public void setAddonVersion(String addonVersion) {
        this.addonVersion = addonVersion;
    }

    /**
     * The name of the cluster to create the add-on for.
     */
    @Required
    public EksClusterResource getCluster() {
        return cluster;
    }

    public void setCluster(EksClusterResource cluster) {
        this.cluster = cluster;
    }

    /**
     * The existing IAM role to bind to the add-on's service account.
     */
    @Updatable
    public RoleResource getServiceAccountRole() {
        return serviceAccountRole;
    }

    public void setServiceAccountRole(RoleResource serviceAccountRole) {
        this.serviceAccountRole = serviceAccountRole;
    }

    /**
     * The metadata to apply to the cluster to assist with categorization and organization.
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
     * The ARN of the addon.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public void copyFrom(Addon model) {
        setAddonName(model.addonName());
        setAddonVersion(model.addonVersion());
        setCluster(findById(EksClusterResource.class, model.clusterName()));
        setServiceAccountRole(findById(RoleResource.class, model.serviceAccountRoleArn()));
        setArn(model.addonArn());

        getTags().clear();
        if (model.hasTags()) {
            setTags(model.tags());
        }
    }

    @Override
    public boolean refresh() {
        EksClient client = createClient(EksClient.class);

        Addon addon = getAddon(client);

        if (addon == null) {
            return false;
        }

        copyFrom(addon);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        EksClient client = createClient(EksClient.class);

        CreateAddonResponse response = client.createAddon(r -> r.addonName(getAddonName())
            .addonVersion(getAddonVersion())
            .clusterName(getCluster().getName())
            .serviceAccountRoleArn(getServiceAccountRole() == null ? null : getServiceAccountRole().getArn())
            .tags(getTags()));

        setArn(response.addon().addonArn());

        waitForActiveStatus(client);

        copyFrom(response.addon());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        EksClient client = createClient(EksClient.class);

        if (changedFieldNames.contains("tags")) {
            EksAddonResource currentResource = (EksAddonResource) current;

            if (!currentResource.getTags().isEmpty()) {
                client.untagResource(UntagResourceRequest.builder()
                    .resourceArn(getArn())
                    .tagKeys(currentResource.getTags().keySet())
                    .build());
            }

            client.tagResource(TagResourceRequest.builder().resourceArn(getArn()).tags(getTags()).build());
        }

        client.updateAddon(r -> r.addonName(getAddonName())
            .addonVersion(getAddonVersion())
            .clusterName(getCluster().getName())
            .serviceAccountRoleArn(getServiceAccountRole() == null ? null : getServiceAccountRole().getArn()));

        waitForActiveStatus(client);
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        EksClient client = createClient(EksClient.class);

        client.deleteAddon(r -> r.addonName(getAddonName()).clusterName(getCluster().getName()));

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> getAddon(client) == null);
    }

    private Addon getAddon(EksClient client) {
        Addon addon = null;

        try {
            addon = client.describeAddon(r -> r.addonName(getAddonName()).clusterName(getCluster().getName())).addon();

        } catch (ResourceNotFoundException ex) {
            // ignore
        }

        return addon;
    }

    private void waitForActiveStatus(EksClient client) {
        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> {
                Addon addon = getAddon(client);
                return addon != null && addon.status().equals(AddonStatus.ACTIVE);
            });
    }
}
