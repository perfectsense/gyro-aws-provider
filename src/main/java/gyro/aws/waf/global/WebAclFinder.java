/*
 * Copyright 2019, Perfect Sense, Inc.
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

package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ListWebAcLsRequest;
import software.amazon.awssdk.services.waf.model.ListWebAcLsResponse;
import software.amazon.awssdk.services.waf.model.WafNonexistentItemException;
import software.amazon.awssdk.services.waf.model.WebACL;
import software.amazon.awssdk.services.waf.model.WebACLSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query waf acl.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    waf-acl: $(external-query aws::waf-web-acl)
 */
@Type("waf-web-acl")
public class WebAclFinder extends gyro.aws.waf.common.WebAclFinder<WafClient, WebAclResource> {
    @Override
    protected List<WebACL> findAllAws(WafClient client) {
        List<WebACL> webACLS = new ArrayList<>();

        String marker = null;
        ListWebAcLsResponse response;
        List<WebACLSummary> webACLSummaries = new ArrayList<>();

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listWebACLs();
            } else {
                response = client.listWebACLs(ListWebAcLsRequest.builder().nextMarker(marker).build());
            }

            marker = response.nextMarker();
            webACLSummaries.addAll(response.webACLs());

        } while (!ObjectUtils.isBlank(marker));

        if (!webACLSummaries.isEmpty()) {
            for (WebACLSummary webACLSummary : webACLSummaries) {
                webACLS.add(client.getWebACL(r -> r.webACLId(webACLSummary.webACLId())).webACL());
            }
        }

        return webACLS;
    }

    @Override
    protected List<WebACL> findAws(WafClient client, Map<String, String> filters) {
        List<WebACL> webACLS = new ArrayList<>();

        try {
            webACLS.add(client.getWebACL(r -> r.webACLId(filters.get("id"))).webACL());
        } catch (WafNonexistentItemException ignore) {
            //ignore
        }

        return webACLS;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }
}
