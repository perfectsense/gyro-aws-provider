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

import gyro.core.Type;
import gyro.core.resource.DiffableInternals;
import gyro.core.resource.DiffableType;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.IdentityProviderConfig;
import software.amazon.awssdk.services.eks.model.IdentityProviderConfigResponse;
import software.amazon.awssdk.services.eks.model.ListIdentityProviderConfigsResponse;
import software.amazon.awssdk.services.eks.model.NotFoundException;

/**
 * Creates an eks authentication.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::eks-authentication eks-authentication-example
 *         name: "onelogin"
 *         type: "oidc"
 *         cluster: $(aws::eks-cluster ex)
 *
 *         config
 *             client-id: "valid client id"
 *             groups-claim: "groups"
 *             groups-prefix: "onelogin-group:"
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
public class EksStandaloneAuthenticationResource extends EksAuthentication {

    private EksClusterResource cluster;

    @Required
    public EksClusterResource getCluster() {
        return cluster;
    }

    public void setCluster(EksClusterResource cluster) {
        this.cluster = cluster;
    }

    @Override
    public boolean refresh() {
        EksClient client = createClient(EksClient.class);

        try {
            ListIdentityProviderConfigsResponse response = client.listIdentityProviderConfigs(r -> r
                .clusterName(clusterName()));
            if (response.hasIdentityProviderConfigs() && !response.identityProviderConfigs().isEmpty()) {
                IdentityProviderConfig providerConfig = response.identityProviderConfigs().get(0);

                IdentityProviderConfigResponse auth = EksAuthentication.getIdentityProviderConfigResponse(
                    client,
                    getName(),
                    providerConfig.name(),
                    providerConfig.type());

                if (auth != null) {
                    copyFrom(auth);
                }
            }
        } catch (NotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    public String primaryKey() {
        String name = DiffableInternals.getName(this);
        return String.format("%s::%s", DiffableType.getInstance(getClass()).getName(), name);
    }

    @Override
    protected String clusterName() {
        return cluster.getName();
    }

}
