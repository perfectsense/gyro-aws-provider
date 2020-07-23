/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.wafv2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.ListWebAcLsRequest;
import software.amazon.awssdk.services.wafv2.model.ListWebAcLsResponse;
import software.amazon.awssdk.services.wafv2.model.Scope;
import software.amazon.awssdk.services.wafv2.model.WafInvalidParameterException;
import software.amazon.awssdk.services.wafv2.model.WafNonexistentItemException;
import software.amazon.awssdk.services.wafv2.model.WebACL;

/**
 * Query waf v2 web acl.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    web-acl: $(external-query aws::wafv2-web-acl)
 */
@Type("wafv2-web-acl")
public class WebAclFinder extends AwsFinder<Wafv2Client, WebACL, WebAclResource> {

    private String id;
    private String name;
    private String scope;

    /**
     *  The id of the web acl.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The name of the web acl.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The scope of the web acl.
     */
    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    protected List<WebACL> findAllAws(Wafv2Client client) {
        List<WebACL> webACLs = new ArrayList<>();
        ListWebAcLsResponse response;
        String marker = null;

        do {
            try {
                response = client.listWebACLs(ListWebAcLsRequest.builder()
                    .scope(Scope.CLOUDFRONT)
                    .nextMarker(marker)
                    .build());

                marker = response.nextMarker();

                webACLs.addAll(response.webACLs()
                    .stream()
                    .map(o -> getWebACL(client, o.id(), o.name(), Scope.CLOUDFRONT.toString()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
            } catch (WafInvalidParameterException ex) {
                // Ignore
                // Occurs if no cloudfront based web acl present
            }

        } while (!ObjectUtils.isBlank(marker));

        marker = null;

        do {
            try {
                response = client.listWebACLs(ListWebAcLsRequest.builder()
                    .scope(Scope.REGIONAL)
                    .nextMarker(marker)
                    .build());

                marker = response.nextMarker();

                webACLs.addAll(response.webACLs()
                    .stream()
                    .map(o -> getWebACL(client, o.id(), o.name(), Scope.REGIONAL.toString()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
            } catch (WafInvalidParameterException ex) {
                // Ignore
                // Occurs if no regional based web acl present
            }

        } while (!ObjectUtils.isBlank(marker));

        return webACLs;
        /*GetWebAclResponse response = client.getWebACL(
            r -> r.id("8487a18b-fd40-40d4-8375-e4e7957fad41")
                .name("dj-test-waf")
                .scope(Scope.REGIONAL)
        );



        return Collections.singletonList(response.webACL());*/
    }

    @Override
    protected List<WebACL> findAws(Wafv2Client client, Map<String, String> filters) {
        List<WebACL> webACLs = new ArrayList<>();

        WebACL webACL = getWebACL(client, filters.get("id"), filters.get("name"), filters.get("scope"));

        if (webACL != null) {
            webACLs.add(webACL);
        }

        return webACLs;
    }

    private WebACL getWebACL(Wafv2Client client, String id, String name, String scope) {
        try {
            return client.getWebACL(r -> r.id(id).name(name).scope(scope)).webACL();
        } catch (WafNonexistentItemException ex) {
            return null;
        }
    }
}
