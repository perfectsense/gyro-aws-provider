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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.GyroException;
import gyro.core.Type;
import software.amazon.awssdk.services.eks.EksClient;
import software.amazon.awssdk.services.eks.model.DescribeIdentityProviderConfigResponse;
import software.amazon.awssdk.services.eks.model.IdentityProviderConfig;
import software.amazon.awssdk.services.eks.model.IdentityProviderConfigResponse;
import software.amazon.awssdk.services.eks.model.ResourceNotFoundException;

/**
 * Query eks authentication.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    eks-authentication: $(external-query aws::eks-authentication {cluster-name: "cluster-prod", name: "onelogin", type: "oidc"})
 */
@Type("eks-authentication")
public class EksStandaloneAuthenticationFinder extends AwsFinder<EksClient, IdentityProviderConfigResponse, EksStandaloneAuthenticationResource> {

    private String name;
    private String type;
    private String clusterName;

    /**
     * The name of the authentication.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The type of the authentication.
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The name of the cluster that the authentication belongs to.
     */
    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @Override
    protected List<IdentityProviderConfigResponse> findAllAws(EksClient client) {
        List<IdentityProviderConfigResponse> configResponses = new ArrayList<>();
        List<String> clusters = client.listClusters().clusters();

        clusters.forEach(c -> configResponses.addAll(client.listIdentityProviderConfigs(r -> r.clusterName(c))
            .identityProviderConfigs()
            .stream()
            .map(a -> client.describeIdentityProviderConfig(d -> d.clusterName(c)
                .identityProviderConfig(i -> i.name(a.name()).type(a.type()))).identityProviderConfig())
            .collect(Collectors.toList())));

        return configResponses;
    }

    @Override
    protected List<IdentityProviderConfigResponse> findAws(EksClient client, Map<String, String> filters) {
        List<IdentityProviderConfigResponse> configResponses = new ArrayList<>();
        List<String> clusters = new ArrayList<>();

        if (!filters.containsKey("cluster-name")) {
            clusters = client.listClusters().clusters();

        } else {
            clusters.add(filters.get("cluster-name"));
        }

        IdentityProviderConfig.Builder builder = IdentityProviderConfig.builder();

        if (filters.containsKey("name")) {
            builder = builder.name(filters.get("name"));
        } else {
            throw new GyroException("'name' is required.");
        }

        builder = builder.type(filters.getOrDefault("type", EksAuthentication.IDENTITY_PROVIDER_TYPE));

        IdentityProviderConfig identityProviderConfig = builder.build();

        clusters.forEach(cl -> {
            try {
                DescribeIdentityProviderConfigResponse response = client.describeIdentityProviderConfig(
                    r -> r.clusterName(cl)
                        .identityProviderConfig(identityProviderConfig));

                configResponses.add(response.identityProviderConfig());
            } catch (ResourceNotFoundException ex) {
                // Ignore
            }
        });

        return configResponses;
    }
}
