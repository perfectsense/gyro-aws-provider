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
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.apigatewayv2.ApiGatewayV2Client;
import software.amazon.awssdk.services.apigatewayv2.model.DomainName;

/**
 * Query Domain Name.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    domain-name: $(external-query aws::api-gateway-domain-name {name: "vpn.ops-test.psdops.com"})
 */
@Type("api-gateway-domain-name")
public class DomainNameFinder extends AwsFinder<ApiGatewayV2Client, DomainName, DomainNameResource> {

    private String name;

    /**
     * The name of the domain.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<DomainName> findAllAws(ApiGatewayV2Client client) {
        return client.getDomainNames().items();
    }

    @Override
    protected List<DomainName> findAws(ApiGatewayV2Client client, Map<String, String> filters) {
        return client.getDomainNames()
            .items()
            .stream()
            .filter(i -> i.domainName().equals(filters.get("name")))
            .collect(Collectors.toList());
    }
}
