/*
 * Copyright 2026, Brightspot.
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
import gyro.core.TimeoutSettings;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.Addon;
import software.amazon.awssdk.services.eks.model.AddonStatus;
import software.amazon.awssdk.services.eks.model.CreateAddonResponse;
import software.amazon.awssdk.services.eks.model.ResolveConflicts;
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
 *              addon-name: coredns
 *              addon-version: "v1.11.4-eksbuild.24"
 *              configuration-values: '{"replicaCount": 1}'
 *              resolve-conflicts: OVERWRITE
 *              tags: {
 *                  Name: "example-eks-gyro-coredns"
 *              }
 *         end
 *     end
 */
public class EksAddonResource extends AwsResource implements Copyable<Addon> {

    private String addonName;
    private String addonVersion;
    private String configurationValues;
    private ResolveConflicts resolveConflicts;
    private RoleResource serviceAccountRole;
    private Map<String, String> tags;

    // Read-only
    private String arn;
    private String createdAt;
    private String modifiedAt;
    private EksAddonHealth health;
    private EksAddonMarketplaceInformation marketplaceInformation;
    private String owner;
    private Set<String> podIdentityAssociations;
    private String publisher;
    private String status;

    /**
     * The name of the add-on.
     */
    @Id
    @Required
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
     * The configuration values for the add-on as a JSON string.
     */
    @Updatable
    public String getConfigurationValues() {
        return configurationValues;
    }

    public void setConfigurationValues(String configurationValues) {
        this.configurationValues = configurationValues;
    }

    /**
     * Overwrites configuration when set to ``OVERWRITE``.
     */
    @Updatable
    @ValidStrings({ "OVERWRITE", "NONE", "PRESERVE" })
    public ResolveConflicts getResolveConflicts() {
        return resolveConflicts;
    }

    public void setResolveConflicts(ResolveConflicts resolveConflicts) {
        this.resolveConflicts = resolveConflicts;
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

    /**
     * The Unix epoch timestamp at object creation.
     */
    @Output
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * The Unix epoch timestamp for the last modification.
     */
    @Output
    public String getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(String modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    /**
     * The health of the addon.
     */
    @Output
    public EksAddonHealth getHealth() {
        return health;
    }

    public void setHealth(EksAddonHealth health) {
        this.health = health;
    }

    /**
     * Information about the addon from AWS Marketplace.
     */
    @Output
    public EksAddonMarketplaceInformation getMarketplaceInformation() {
        return marketplaceInformation;
    }

    public void setMarketplaceInformation(EksAddonMarketplaceInformation marketplaceInformation) {
        this.marketplaceInformation = marketplaceInformation;
    }

    /**
     * The owner of the addon.
     */
    @Output
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * EKS Pod Identity associations owned by the addon.
     */
    @Output
    public Set<String> getPodIdentityAssociations() {
        return podIdentityAssociations;
    }

    public void setPodIdentityAssociations(Set<String> podIdentityAssociations) {
        this.podIdentityAssociations = podIdentityAssociations;
    }

    /**
     * The publisher of the addon.
     */
    @Output
    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    /**
     * The status of the addon.
     */
    @Output
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String primaryKey() {
        return getAddonName();
    }

    @Override
    public void copyFrom(Addon model) {
        setAddonName(model.addonName());
        setAddonVersion(model.addonVersion());
        setConfigurationValues(model.configurationValues());
        setServiceAccountRole(findById(RoleResource.class, model.serviceAccountRoleArn()));
        setArn(model.addonArn());
        setTags(model.tags());

        setCreatedAt(model.createdAt() != null ? model.createdAt().toString() : null);
        setModifiedAt(model.modifiedAt() != null ? model.modifiedAt().toString() : null);
        setOwner(model.owner());
        setPublisher(model.publisher());
        setStatus(model.statusAsString());
        setPodIdentityAssociations(model.podIdentityAssociations() == null
            ? null
            : Set.copyOf(model.podIdentityAssociations()));

        setHealth(null);
        if (model.health() != null) {
            EksAddonHealth health = newSubresource(EksAddonHealth.class);
            health.copyFrom(model.health());
            setHealth(health);
        }

        setMarketplaceInformation(null);
        if (model.marketplaceInformation() != null) {
            EksAddonMarketplaceInformation mi = newSubresource(EksAddonMarketplaceInformation.class);
            mi.copyFrom(model.marketplaceInformation());
            setMarketplaceInformation(mi);
        }
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        EksClient client = createClient(EksClient.class);

        CreateAddonResponse response = client.createAddon(r -> r.addonName(getAddonName())
            .addonVersion(getAddonVersion())
            .configurationValues(getConfigurationValues())
            .clusterName(clusterName())
            .resolveConflicts(getResolveConflicts())
            .serviceAccountRoleArn(getServiceAccountRole() == null ? null : getServiceAccountRole().getArn())
            .tags(getTags()));

        setArn(response.addon().addonArn());

        waitForActiveStatus(client, clusterName(), getAddonName(), TimeoutSettings.Action.CREATE);

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

        if (changedFieldNames.contains("addon-version")
            || changedFieldNames.contains("service-account-role")
            || changedFieldNames.contains("configuration-values")) {

            client.updateAddon(r -> r.addonName(getAddonName())
                .addonVersion(getAddonVersion())
                .configurationValues(getConfigurationValues())
                .clusterName(clusterName())
                .resolveConflicts(getResolveConflicts())
                .serviceAccountRoleArn(getServiceAccountRole() == null ? null : getServiceAccountRole().getArn()));

            waitForActiveStatus(client, clusterName(), getAddonName(), TimeoutSettings.Action.UPDATE);
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        EksClient client = createClient(EksClient.class);

        client.deleteAddon(r -> r.addonName(getAddonName()).clusterName(clusterName()));

        Wait.atMost(1, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .resourceOverrides(this, TimeoutSettings.Action.DELETE)
            .prompt(false)
            .until(() -> getAddon(client, clusterName(), getAddonName()) == null);
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

    private void waitForActiveStatus(EksClient client, String clusterName, String name, TimeoutSettings.Action action) {
        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .resourceOverrides(this, action)
            .prompt(false)
            .until(() -> {
                Addon addon = getAddon(client, clusterName, name);
                return addon != null && addon.status().equals(AddonStatus.ACTIVE);
            });
    }

    protected String clusterName() {
        EksClusterResource parent = (EksClusterResource) parent();
        return parent.getName();
    }
}
