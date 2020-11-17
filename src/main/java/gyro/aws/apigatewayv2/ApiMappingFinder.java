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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.apigatewayv2.model.ApiMapping;
import software.amazon.awssdk.services.apigatewayv2.model.DomainName;

/**
 * Query Api.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    api-mapping: $(external-query aws::api-mapping {domain-name: "vpn.ops-test.psdops.com", mapping-id: ""})
 */
@Type("api-mapping")
public class ApiMappingFinder extends AwsFinder<ApiGatewayV2Client, ApiMapping, ApiMappingResource> {

    private String domainName;
    private String mappingId;

    /**
     * The domain name which has the api.
     */
    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    /**
     * The id of the mapping.
     */
    public String getMappingId() {
        return mappingId;
    }

    public void setMappingId(String mappingId) {
        this.mappingId = mappingId;
    }

    @Override
    protected List<ApiMapping> findAllAws(ApiGatewayV2Client client) {
        return getDomainNames(client).stream()
            .map(d -> client.getApiMappings(r -> r.domainName(d)).items())
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    @Override
    protected List<ApiMapping> findAws(ApiGatewayV2Client client, Map<String, String> filters) {
        List<ApiMapping> apiMappings = new ArrayList<>();

        if (filters.containsKey("domain-name")) {
            apiMappings = client.getApiMappings(r -> r.domainName(filters.get("domain-name"))).items();

        } else {
            for (String api : getDomainNames(client)) {
                apiMappings.addAll(client.getApiMappings(r -> r.domainName(api)).items());
            }
        }

        if (filters.containsKey("id")) {
            apiMappings.removeIf(i -> !i.apiMappingId().equals(filters.get("id")));
        }

        return apiMappings;
    }

    private List<String> getDomainNames(ApiGatewayV2Client client) {
        return client.getDomainNames().items().stream().map(DomainName::domainName).collect(Collectors.toList());
    }
}
