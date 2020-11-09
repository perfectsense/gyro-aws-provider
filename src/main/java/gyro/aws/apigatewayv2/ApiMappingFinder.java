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

import java.util.List;
import java.util.Map;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.apigatewayv2.model.ApiMapping;

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
        throw new IllegalArgumentException("Cannot query ApiMappings without 'domain-name'.");
    }

    @Override
    protected List<ApiMapping> findAws(ApiGatewayV2Client client, Map<String, String> filters) {
        if (!filters.containsKey("domain-name")) {
            throw new IllegalArgumentException("Cannot query ApiMappings without 'domain-name'.");
        }

        List<ApiMapping> apiMappings = client.getApiMappings(r -> r.domainName(filters.get("domain-name"))).items();

        if (filters.containsKey("id")) {
            apiMappings.removeIf(i -> !i.apiMappingId().equals(filters.get("id")));
        }

        return apiMappings;
    }
}
