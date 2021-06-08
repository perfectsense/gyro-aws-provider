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
import software.amazon.awssdk.services.eks.model.Cluster;
import software.amazon.awssdk.services.eks.model.ClusterStatus;
import software.amazon.awssdk.services.eks.model.CreateClusterRequest;
import software.amazon.awssdk.services.eks.model.CreateClusterResponse;
import software.amazon.awssdk.services.eks.model.DeleteClusterRequest;
import software.amazon.awssdk.services.eks.model.DescribeClusterRequest;
import software.amazon.awssdk.services.eks.model.EksException;
import software.amazon.awssdk.services.eks.model.ListAddonsResponse;
import software.amazon.awssdk.services.eks.model.IdentityProviderConfig;
import software.amazon.awssdk.services.eks.model.IdentityProviderConfigResponse;
import software.amazon.awssdk.services.eks.model.ListIdentityProviderConfigsResponse;
import software.amazon.awssdk.services.eks.model.LogSetup;
import software.amazon.awssdk.services.eks.model.LogType;
import software.amazon.awssdk.services.eks.model.Logging;
import software.amazon.awssdk.services.eks.model.NotFoundException;
import software.amazon.awssdk.services.eks.model.TagResourceRequest;
import software.amazon.awssdk.services.eks.model.UntagResourceRequest;
import software.amazon.awssdk.services.eks.model.UpdateClusterConfigRequest;
import software.amazon.awssdk.services.eks.model.UpdateClusterVersionRequest;

/**
 * Creates an eks cluster.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::eks-cluster ex
 *         name: "example-eks-gyro"
 *         role: "arn:aws:iam::242040583208:role/EXAMPLE_EKS_ROLE"
 *         version: 1.15
 *
 *         vpc-config
 *             enable-endpoint-private-access: true
 *             enable-endpoint-public-access: true
 *
 *             subnets: [
 *                 $(aws::subnet "subnet-example-us-east-1a"),
 *                 $(aws::subnet "subnet-example-us-east-1b")
 *             ]
 *
 *             security-groups: [
 *                 $(aws::security-group security-group-example)
 *             ]
 *
 *             public-access-cidrs: [
 *                 "0.0.0.0/0"
 *             ]
 *         end
 *
 *         logging
 *             enabled-log-types
 *                 log-types: [audit, api]
 *             end
 *         end
 *
 *         encryption-config
 *             provider
 *                 key: $(external-query aws::kms-key { key-id: "c5245825-8526-4032-a67c-21656f220312"})
 *             end
 *         end
 *
 *         tags: {
 *             "example-tag-key": "example-tag-value"
 *         }
 *     end
 */
@Type("eks-cluster")
public class EksClusterResource extends AwsResource implements Copyable<Cluster> {

    private String name;
    private RoleResource role;
    private String version;
    private EksVpcConfig vpcConfig;
    private EksLogging logging;
    private List<EksEncryptionConfig> encryptionConfig;
    private Map<String, String> tags;
    private List<EksAddonResource> addon;
    private EksAuthentication authentication;

    // Read-only
    private String arn;
    private String oidcProviderUrl;

    /**
     * The name of the EKS cluster.
     */
    @Id
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The IAM role that provides permissions for the EKS.
     */
    @Required
    public RoleResource getRole() {
        return role;
    }

    public void setRole(RoleResource role) {
        this.role = role;
    }

    /**
     * The desired Kubernetes version for your cluster. Defaults to ``1.15``
     */
    @Updatable
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * The VPC configuration used by the cluster.
     *
     * @subresource gyro.aws.eks.EksVpcConfig
     */
    @Updatable
    @Required
    public EksVpcConfig getVpcConfig() {
        return vpcConfig;
    }

    public void setVpcConfig(EksVpcConfig vpcConfig) {
        this.vpcConfig = vpcConfig;
    }

    /**
     * The logging configuration used by the cluster.
     *
     * @subresource gyro.aws.eks.EksLogging
     */
    @Updatable
    public EksLogging getLogging() {
        return logging;
    }

    public void setLogging(EksLogging logging) {
        this.logging = logging;
    }

    /**
     * The encryption configuration used by the cluster.
     *
     * @subresource gyro.aws.eks.EksEncryptionConfig
     */
    @Updatable
    public List<EksEncryptionConfig> getEncryptionConfig() {
        if (encryptionConfig == null) {
            encryptionConfig = new ArrayList<>();
        }

        return encryptionConfig;
    }

    public void setEncryptionConfig(List<EksEncryptionConfig> encryptionConfig) {
        this.encryptionConfig = encryptionConfig;
    }

    /**
     * The addon configuration for the cluster.
     *
     * @subresource gyro.aws.eks.EksAddonResource
     */
    @Updatable
    public List<EksAddonResource> getAddon() {
        if (addon == null) {
            addon = new ArrayList<>();
        }

        return addon;
    }

    public void setAddon(List<EksAddonResource> addon) {
        this.addon = addon;
    }

    /**
     * The tags to attach to the cluster.
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
     * The authentication config for the cluster.
     *
     * @subresource gyro.aws.eks.EksAuthentication
     */
    @Updatable
    public EksAuthentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(EksAuthentication authentication) {
        this.authentication = authentication;
    }

    /**
     * The Amazon Resource Number (ARN) of the cluster.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The issuer URL for the OIDC identity provider.
     */
    @Output
    public String getOidcProviderUrl() {
        return oidcProviderUrl;
    }

    public void setOidcProviderUrl(String oidcProviderUrl) {
        this.oidcProviderUrl = oidcProviderUrl;
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

        if (model.identity() != null && model.identity().oidc() != null) {
            setOidcProviderUrl(model.identity().oidc().issuer());
        }

        EksClient client = createClient(EksClient.class);

        // load addon
        setAddon(null);
        try {
            ListAddonsResponse response = client.listAddons(r -> r.clusterName(getName()));

            if (response.hasAddons()) {
                response.addons().forEach(a -> {
                    Addon addon = EksAddonResource.getAddon(client, getName(), a);
                    if (addon != null) {
                        EksAddonResource addonResource = newSubresource(EksAddonResource.class);
                        addonResource.copyFrom(addon);
                        getAddon().add(addonResource);
                    }
                });
            }
        } catch (NotFoundException ex) {
            // Ignore
        }

        // load eks authentication
        setAuthentication(null);
        try {
            ListIdentityProviderConfigsResponse response = client.listIdentityProviderConfigs(r -> r
                .clusterName(getName()));
            if (response.hasIdentityProviderConfigs() && !response.identityProviderConfigs().isEmpty()) {
                IdentityProviderConfig providerConfig = response.identityProviderConfigs().get(0);

                IdentityProviderConfigResponse auth = EksAuthentication.getIdentityProviderConfigResponse(
                    client,
                    getName(),
                    providerConfig.name(),
                    providerConfig.type());

                if (auth != null) {
                    EksAuthentication authentication = newSubresource(EksAuthentication.class);
                    authentication.copyFrom(auth);
                    setAuthentication(authentication);
                }
            }
        } catch (NotFoundException ex) {
            // Ignore
        }
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

        CreateClusterRequest.Builder builder = CreateClusterRequest.builder()
                .name(getName())
                .roleArn(getRole().getArn())
                .resourcesVpcConfig(getVpcConfig().toVpcConfigRequest())
                .version(getVersion());

        if (getLogging() != null) {
            builder = builder.logging(getLogging().toLogging());
        }

        if (!getEncryptionConfig().isEmpty()) {
            builder = builder.encryptionConfig(getEncryptionConfig().stream()
                .map(EksEncryptionConfig::toEncryptionConfig)
                .collect(Collectors.toList()));
        }

        CreateClusterResponse response = client.createCluster(builder.tags(getTags()).build());

        waitForActiveStatus(client);

        copyFrom(getCluster(client));
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

            waitForActiveStatus(client);
        }

        if (changedFieldNames.contains("tags")) {
            EksClusterResource currentResource = (EksClusterResource) current;

            if (!currentResource.getTags().isEmpty()) {
                client.untagResource(UntagResourceRequest.builder()
                        .resourceArn(getArn())
                        .tagKeys(currentResource.getTags().keySet())
                        .build());
            }

            client.tagResource(TagResourceRequest.builder().resourceArn(getArn()).tags(getTags()).build());
        }

        if (changedFieldNames.contains("vpc-config")) {
            client.updateClusterConfig(UpdateClusterConfigRequest.builder()
                    .name(getName())
                    .resourcesVpcConfig(getVpcConfig().updatedConfig())
                    .build());

            waitForActiveStatus(client);
        }

        if (changedFieldNames.contains("logging")) {
            if (getLogging() != null) {
                client.updateClusterConfig(UpdateClusterConfigRequest.builder()
                        .name(getName())
                        .logging(getLogging().toLogging())
                        .build());

            } else {
                client.updateClusterConfig(UpdateClusterConfigRequest.builder()
                        .name(getName())
                        .logging(Logging.builder().clusterLogging(
                                LogSetup.builder().enabled(Boolean.FALSE).types(LogType.knownValues()).build())
                                .build())
                        .build());
            }

            waitForActiveStatus(client);
        }

        if (changedFieldNames.contains("encryption-config") && !getEncryptionConfig().isEmpty()) {
            client.associateEncryptionConfig(r -> r.clusterName(getName())
                .encryptionConfig(getEncryptionConfig().stream()
                    .map(EksEncryptionConfig::toEncryptionConfig)
                    .collect(Collectors.toList())
            ));

            waitForActiveStatus(client);
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        EksClient client = createClient(EksClient.class);

        client.deleteCluster(DeleteClusterRequest.builder().name(getName()).build());

        Wait.atMost(20, TimeUnit.MINUTES)
            .prompt(false)
            .checkEvery(30, TimeUnit.SECONDS)
            .until(() -> getCluster(client) == null);
    }

    private Cluster getCluster(EksClient client) {
        Cluster cluster = null;

        try {
            cluster = client.describeCluster(DescribeClusterRequest.builder().name(getName()).build()).cluster();

        } catch (EksException ex) {
            if (!ex.awsErrorDetails().errorCode().equals("ResourceNotFoundException")) {
                throw ex;
            }
        }

        return cluster;
    }

    private void waitForActiveStatus(EksClient client) {
        Wait.atMost(20, TimeUnit.MINUTES)
            .checkEvery(30, TimeUnit.SECONDS)
                .prompt(false)
                .until(() -> {
                    Cluster cluster = getCluster(client);
                    return cluster != null && cluster.status().equals(ClusterStatus.ACTIVE);
                });
    }
}
