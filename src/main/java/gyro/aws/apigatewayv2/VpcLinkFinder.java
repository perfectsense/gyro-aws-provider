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
import software.amazon.awssdk.services.apigatewayv2.model.GetVpcLinksRequest;
import software.amazon.awssdk.services.apigatewayv2.model.GetVpcLinksResponse;
import software.amazon.awssdk.services.apigatewayv2.model.VpcLink;

/**
 * Query Deployment.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    vpc-link: $(external-query aws::api-gateway-vpc-link {name: "example-vpc-link"})
 */
@Type("api-gateway-vpc-link")
public class VpcLinkFinder extends ApiGatewayFinder<ApiGatewayV2Client, VpcLink, VpcLinkResource> {

    private String name;

    /**
     * The name of the VPC link.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<VpcLink> findAllAws(ApiGatewayV2Client client) {
        List<VpcLink> vpcLinks = new ArrayList<>();
        String marker = null;
        GetVpcLinksResponse response;

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.getVpcLinks(GetVpcLinksRequest.builder().build());
            } else {
                response = client.getVpcLinks(GetVpcLinksRequest.builder().nextToken(marker).build());
            }

            marker = response.nextToken();
            vpcLinks.addAll(response.items());
        } while (!ObjectUtils.isBlank(marker));

        return vpcLinks;
    }

    @Override
    protected List<VpcLink> findAws(ApiGatewayV2Client client, Map<String, String> filters) {
        List<VpcLink> vpcLinks = new ArrayList<>();
        String marker = null;
        GetVpcLinksResponse response;

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.getVpcLinks(GetVpcLinksRequest.builder().build());
            } else {
                response = client.getVpcLinks(GetVpcLinksRequest.builder().nextToken(marker).build());
            }

            marker = response.nextToken();
            vpcLinks.addAll(response.items().stream().filter(i -> i.name().equals(filters.get("name")))
                .collect(Collectors.toList()));
        } while (!ObjectUtils.isBlank(marker));

        return vpcLinks;
    }
}
