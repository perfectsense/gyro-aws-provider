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

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.apigatewayv2.model.ApiMapping;
import software.amazon.awssdk.services.apigatewayv2.model.DomainName;
import software.amazon.awssdk.services.apigatewayv2.model.GetApiMappingsRequest;
import software.amazon.awssdk.services.apigatewayv2.model.GetApiMappingsResponse;
import software.amazon.awssdk.services.apigatewayv2.model.GetDomainNamesRequest;
import software.amazon.awssdk.services.apigatewayv2.model.GetDomainNamesResponse;

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
public class ApiMappingFinder extends ApiGatewayFinder<ApiGatewayV2Client, ApiMapping, ApiMappingResource> {

    private String domainName;
    private String mappingId;

    /**
     * The domain name which has the API.
     */
    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    /**
     * The ID of the mapping.
     */
    public String getMappingId() {
        return mappingId;
    }

    public void setMappingId(String mappingId) {
        this.mappingId = mappingId;
    }

    @Override
    protected List<ApiMapping> findAllAws(ApiGatewayV2Client client) {
        List<ApiMapping> apiMappings = new ArrayList<>();
        String marker = null;
        GetApiMappingsResponse response;

        for (String domainName : getDomainNames(client)) {
            do {
                if (ObjectUtils.isBlank(marker)) {
                    response = client.getApiMappings(r -> r.domainName(domainName));
                } else {
                    response = client.getApiMappings(GetApiMappingsRequest.builder()
                        .domainName(domainName).nextToken(marker).build());
                }

                marker = response.nextToken();
                apiMappings.addAll(response.items());
            } while (!ObjectUtils.isBlank(marker));
        }

        return apiMappings;
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
        List<DomainName> domainNames = new ArrayList<>();
        String marker = null;
        GetDomainNamesResponse response;

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.getDomainNames();
            } else {
                response = client.getDomainNames(GetDomainNamesRequest.builder().nextToken(marker).build());
            }

            marker = response.nextToken();
            domainNames.addAll(response.items());
        } while (!ObjectUtils.isBlank(marker));

        return domainNames.stream().map(DomainName::domainName).collect(Collectors.toList());
    }
}
