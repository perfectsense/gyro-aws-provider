/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.apigatewayv2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.AwsCredentials;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.acm.AcmClient;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.apigatewayv2.model.DomainName;
import software.amazon.awssdk.services.apigatewayv2.model.GetDomainNamesResponse;

/**
 * Create a domain name.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::api-gateway-domain-name example-domain-name
 *         name: "vpn.ops-test.psdops.com"
 *
 *         domain-name-configurations
 *             certificate: 'arn:aws:acm:us-east-2:572681481110:certificate/c7de099f-599d-4112-b813-e731fdef0760'
 *             endpoint-type: REGIONAL
 *         end
 *
 *         tags: {
 *             "example-key-change": "example-value"
 *         }
 *     end
 */
@Type("api-gateway-domain-name")
public class DomainNameResource extends AwsResource implements Copyable<DomainName> {

    private String name;
    private List<ApiDomainNameConfiguration> domainNameConfigurations;
    private ApiMutualTlsAuthentication mutualTlsAuthentication;
    private Map<String, String> tags;

    /**
     * The domain name.
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
     * The list of domain name configurations.
     */
    @Updatable
    public List<ApiDomainNameConfiguration> getDomainNameConfigurations() {
        if (domainNameConfigurations == null) {
            domainNameConfigurations = new ArrayList<>();
        }

        return domainNameConfigurations;
    }

    public void setDomainNameConfigurations(List<ApiDomainNameConfiguration> domainNameConfigurations) {
        this.domainNameConfigurations = domainNameConfigurations;
    }

    /**
     * The mutual TLS authentication configuration for the domain name.
     */
    @Updatable
    public ApiMutualTlsAuthentication getMutualTlsAuthentication() {
        return mutualTlsAuthentication;
    }

    public void setMutualTlsAuthentication(ApiMutualTlsAuthentication mutualTlsAuthentication) {
        this.mutualTlsAuthentication = mutualTlsAuthentication;
    }

    /**
     * The collection of tags associated with a domain name.
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

    @Override
    public void copyFrom(DomainName model) {
        setName(model.domainName());
        setTags(model.hasTags() ? model.tags() : null);

        if (model.mutualTlsAuthentication() != null) {
            ApiMutualTlsAuthentication input = newSubresource(ApiMutualTlsAuthentication.class);
            input.copyFrom(model.mutualTlsAuthentication());
            setMutualTlsAuthentication(input);
        }

        if (model.hasDomainNameConfigurations()) {
            setDomainNameConfigurations(model.domainNameConfigurations().stream().map(c -> {
                ApiDomainNameConfiguration config = newSubresource(ApiDomainNameConfiguration.class);
                config.copyFrom(c);

                return config;
            }).collect(Collectors.toList()));

        } else {
            setDomainNameConfigurations(null);
        }
    }

    @Override
    public boolean refresh() {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        DomainName domainName = getDomainName(client);

        if (domainName == null) {
            return false;
        }

        copyFrom(domainName);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);
        AcmClient acmClient = createClient(AcmClient.class);

        client.createDomainName(r -> r.domainName(getName())
            .mutualTlsAuthentication(getMutualTlsAuthentication() != null
                ? getMutualTlsAuthentication().toMutualTlsAuthenticationInput() : null)
            .tags(getTags())
            .domainNameConfigurations(getDomainNameConfigurations().stream()
                .map(d -> d.toDomainNameConfiguration(acmClient))
                .collect(Collectors.toList())));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);
        AcmClient acmClient = createClient(AcmClient.class);

        client.updateDomainName(r -> r.domainName(getName())
            .mutualTlsAuthentication(getMutualTlsAuthentication() != null
                ? getMutualTlsAuthentication().toMutualTlsAuthenticationInput() : null)
            .domainNameConfigurations(getDomainNameConfigurations().stream()
                .map(d -> d.toDomainNameConfiguration(acmClient))
                .collect(Collectors.toList())));

        String arn = String.format(
            "arn:aws:apigateway:%s::/domainnames/%s",
            credentials(AwsCredentials.class).getRegion(),
            getName());

        if (changedFieldNames.contains("tags")) {
            DomainNameResource currentResource = (DomainNameResource) current;

            if (!currentResource.getTags().isEmpty()) {
                client.untagResource(r -> r.resourceArn(arn)
                    .tagKeys(currentResource.getTags().keySet())
                    .build());
            }

            client.tagResource(r -> r.resourceArn(arn).tags(getTags()));
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        ApiGatewayV2Client client = createClient(ApiGatewayV2Client.class);

        client.deleteDomainName(r -> r.domainName(getName()));
    }

    private DomainName getDomainName(ApiGatewayV2Client client) {
        DomainName domainName = null;

        GetDomainNamesResponse response = client.getDomainNames();

        if (response.hasItems()) {
            domainName = response.items()
                .stream()
                .filter(i -> i.domainName().equals(getName()))
                .findFirst()
                .orElse(null);
        }

        return domainName;
    }
}
