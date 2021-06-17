/*
 * Copyright 2021, Brightspot.
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
 *     aws::eks-cluster example-addon
 *         .
 *         .
 *         .
 *         addon
 *              addon-name: vpc-cni
 *
 *              tags : {
 *                  Name: "example-key-value"
 *              }
 *         end
 *     end
 */
public class EksAddonResource extends AwsResource implements Copyable<Addon> {

    private String addonName;
    private String addonVersion;
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
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(Addon model) {
        setAddonName(model.addonName());
        setAddonVersion(model.addonVersion());
        setServiceAccountRole(findById(RoleResource.class, model.serviceAccountRoleArn()));
        setArn(model.addonArn());
        setTags(model.tags());
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        EksClient client = createClient(EksClient.class);

        EksClusterResource parent = (EksClusterResource) parent();

        CreateAddonResponse response = client.createAddon(r -> r.addonName(getAddonName())
            .addonVersion(getAddonVersion())
            .clusterName(parent.getName())
            .serviceAccountRoleArn(getServiceAccountRole() == null ? null : getServiceAccountRole().getArn())
            .tags(getTags()));

        setArn(response.addon().addonArn());

        waitForActiveStatus(client, parent.getName(), getAddonName());

        copyFrom(response.addon());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        EksClient client = createClient(EksClient.class);

        EksClusterResource parent = (EksClusterResource) parent();

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

        if (changedFieldNames.contains("addon-version") || changedFieldNames.contains("service-account-role")) {
            client.updateAddon(r -> r.addonName(getAddonName())
                .addonVersion(getAddonVersion())
                .clusterName(parent.getName())
                .serviceAccountRoleArn(getServiceAccountRole() == null ? null : getServiceAccountRole().getArn()));

            waitForActiveStatus(client, parent.getName(), getAddonName());
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        EksClient client = createClient(EksClient.class);

        EksClusterResource parent = (EksClusterResource) parent();

        client.deleteAddon(r -> r.addonName(getAddonName()).clusterName(parent.getName()));

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> getAddon(client, parent.getName(), getAddonName()) == null);
    }

    protected static Addon getAddon(EksClient client, String clusterName, String name) {
        Addon addon = null;

        try {
            addon = client.describeAddon(r -> r.addonName(name).clusterName(clusterName)).addon();

        } catch (ResourceNotFoundException ex) {
            // ignore
        }

        return addon;
    }

    private void waitForActiveStatus(EksClient client, String clusterName, String name) {
        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> {
                Addon addon = getAddon(client, clusterName, name);
                return addon != null && addon.status().equals(AddonStatus.ACTIVE);
            });
    }
}
