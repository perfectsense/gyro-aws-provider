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
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.ConfigStatus;
import software.amazon.awssdk.services.eks.model.DescribeIdentityProviderConfigResponse;
import software.amazon.awssdk.services.eks.model.IdentityProviderConfigResponse;
import software.amazon.awssdk.services.eks.model.ResourceNotFoundException;

/**
 * Creates an eks authentication.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::eks-authentication eks-authentication-example
 *         cluster: "cluster-example"
 *         name: "onelogin"
 *         type: "oidc"
 *
 *         config
 *             client-id: "valid client id"
 *             groups-claim: "groups"
 *             groups-prefix: "onelogin-group:"
 *             config-name: "onelogin"
 *             issuer-url: "valid issuer url"
 *             username-prefix: "onelogin-user:"
 *         end
 *
 *         tags: {
 *             Name: "eks-authentication-example"
 *         }
 *     end
 */
@Type("eks-authentication")
public class EksAuthentication extends AwsResource implements Copyable<IdentityProviderConfigResponse> {

    private EksClusterResource cluster;
    private String name;
    private String type;
    private OidcIdentityProviderConfiguration config;
    private Map<String, String> tags;

    /**
     * The cluster to associate the identity provider with.
     */
    @Required
    public EksClusterResource getCluster() {
        return cluster;
    }

    public void setCluster(EksClusterResource cluster) {
        this.cluster = cluster;
    }

    /**
     * The name of the identity provider configuration
     */
    @Required
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The type of the identity provider being associated.
     */
    @Required
    @ValidStrings("oidc")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The identity provider configuration.
     *
     * @subresource gyro.aws.eks.OidcIdentityProviderConfiguration
     */
    @Required
    public OidcIdentityProviderConfiguration getConfig() {
        return config;
    }

    public void setConfig(OidcIdentityProviderConfiguration config) {
        this.config = config;
    }

    /**
     * Tags for the identity provider configuration.
     */
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    @Override
    public void copyFrom(IdentityProviderConfigResponse model) {
        setTags(model.oidc().tags());
        setCluster(findById(EksClusterResource.class, model.oidc().clusterName()));

        String arn = model.oidc().identityProviderConfigArn();
        String[] split = arn.split("/");
        setName(split[3]);
        setType(split[2]);

        OidcIdentityProviderConfiguration configuration = newSubresource(OidcIdentityProviderConfiguration.class);
        configuration.copyFrom(model.oidc());
        setConfig(configuration);
    }

    @Override
    public boolean refresh() {
        EksClient client = createClient(EksClient.class);

        IdentityProviderConfigResponse response = getIdentityProviderConfigResponse(client);

        if (response == null) {
            return false;
        }

        copyFrom(response);
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        EksClient client = createClient(EksClient.class);

        client.associateIdentityProviderConfig(r -> r.clusterName(getCluster().getName()).oidc(getConfig().toOidcIdentityProviderConfig()).tags(getTags()));

        Wait.atMost(60, TimeUnit.MINUTES)
            .checkEvery(5, TimeUnit.MINUTES)
            .prompt(false)
            .until(() -> {
                IdentityProviderConfigResponse response = getIdentityProviderConfigResponse(client);
                return response != null && response.oidc().status() == ConfigStatus.ACTIVE;
            });
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        EksClient client = createClient(EksClient.class);

        client.disassociateIdentityProviderConfig(r -> r.clusterName(getCluster().getName()).identityProviderConfig(i -> i.name(getName()).type(getType())));

        Wait.atMost(60, TimeUnit.MINUTES)
            .checkEvery(5, TimeUnit.MINUTES)
            .prompt(false)
            .until(() -> getIdentityProviderConfigResponse(client) == null);
    }

    private IdentityProviderConfigResponse getIdentityProviderConfigResponse(EksClient client) {
        IdentityProviderConfigResponse identityProviderConfigResponse = null;
        try {
            DescribeIdentityProviderConfigResponse response = client.describeIdentityProviderConfig(
                r -> r.clusterName(getCluster().getName())
                    .identityProviderConfig(i -> i.name(getName()).type(getType())));

            identityProviderConfigResponse = response.identityProviderConfig();

        } catch (ResourceNotFoundException ex) {
            // ignore
        }

        return identityProviderConfigResponse;
    }
}
