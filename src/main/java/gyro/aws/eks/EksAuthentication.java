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
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;
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
 *     aws::eks-cluster eks-authentication-example
 *         .
 *         .
 *         .
 *         authentication
 *              name: "onelogin"
 *              type: "oidc"
 *
 *              config
 *                  client-id: "valid client id"
 *                  groups-claim: "groups"
 *                  groups-prefix: "onelogin-group:"
 *                  issuer-url: "valid issuer url"
 *                  username-prefix: "onelogin-user:"
 *              end
 *
 *              tags: {
 *                  Name: "eks-authentication-example"
 *              }
 *          end
 *     end
 */
public class EksAuthentication extends AwsResource implements Copyable<IdentityProviderConfigResponse> {

    private static final String IDENTITY_PROVIDER_TYPE = "oidc";
    private String name;
    private OidcIdentityProviderConfiguration config;
    private Map<String, String> tags;

    /**
     * The name of the identity provider configuration.
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
     * The tags for the identity provider configuration.
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
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(IdentityProviderConfigResponse model) {
        setTags(model.oidc().tags());

        String arn = model.oidc().identityProviderConfigArn();
        String[] split = arn.split("/");
        setName(split[3]);

        OidcIdentityProviderConfiguration configuration = newSubresource(OidcIdentityProviderConfiguration.class);
        configuration.copyFrom(model.oidc());
        setConfig(configuration);
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        EksClient client = createClient(EksClient.class);

        EksClusterResource parent = (EksClusterResource) parent();

        client.associateIdentityProviderConfig(r -> r.clusterName(parent.getName())
            .oidc(getConfig().toOidcIdentityProviderConfig())
            .tags(getTags()));

        Wait.atMost(30, TimeUnit.MINUTES)
            .checkEvery(2, TimeUnit.MINUTES)
            .prompt(false)
            .until(() -> {
                IdentityProviderConfigResponse response = getIdentityProviderConfigResponse(
                    client, parent.getName(), getName(), IDENTITY_PROVIDER_TYPE);
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

        EksClusterResource parent = (EksClusterResource) parent();

        client.disassociateIdentityProviderConfig(r -> r.clusterName(parent.getName())
            .identityProviderConfig(i -> i.name(getName()).type(IDENTITY_PROVIDER_TYPE)));

        Wait.atMost(30, TimeUnit.MINUTES)
            .checkEvery(2, TimeUnit.MINUTES)
            .prompt(false)
            .until(() -> getIdentityProviderConfigResponse(client, parent.getName(), getName(), IDENTITY_PROVIDER_TYPE)
                == null);
    }

    protected static IdentityProviderConfigResponse getIdentityProviderConfigResponse(EksClient client, String clusterName, String name, String type) {
        IdentityProviderConfigResponse identityProviderConfigResponse = null;
        try {
            DescribeIdentityProviderConfigResponse response = client.describeIdentityProviderConfig(
                r -> r.clusterName(clusterName)
                    .identityProviderConfig(i -> i.name(name).type(type)));

            identityProviderConfigResponse = response.identityProviderConfig();

        } catch (ResourceNotFoundException ex) {
            // ignore
        }

        return identityProviderConfigResponse;
    }
}
