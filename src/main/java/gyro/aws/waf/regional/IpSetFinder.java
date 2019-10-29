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

package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import software.amazon.awssdk.services.waf.model.IPSet;
import software.amazon.awssdk.services.waf.model.IPSetSummary;
import software.amazon.awssdk.services.waf.model.ListIpSetsRequest;
import software.amazon.awssdk.services.waf.model.ListIpSetsResponse;
import software.amazon.awssdk.services.waf.model.WafNonexistentItemException;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query ip set regional.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    ip-sets: $(external-query aws::waf-ip-set-regional)
 */
@Type("waf-ip-set-regional")
public class IpSetFinder extends gyro.aws.waf.common.IpSetFinder<WafRegionalClient, IpSetResource> {
    @Override
    protected List<IPSet> findAllAws(WafRegionalClient client) {
        List<IPSet> ipSets = new ArrayList<>();

        String marker = null;
        ListIpSetsResponse response;
        List<IPSetSummary> ipSetSummaries = new ArrayList<>();

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listIPSets();
            } else {
                response = client.listIPSets(ListIpSetsRequest.builder().nextMarker(marker).build());
            }

            marker = response.nextMarker();
            ipSetSummaries.addAll(response.ipSets());

        } while (!ObjectUtils.isBlank(marker));

        for (IPSetSummary ipSetSummary : ipSetSummaries) {
            ipSets.add(client.getIPSet(r -> r.ipSetId(ipSetSummary.ipSetId())).ipSet());
        }

        return ipSets;
    }

    @Override
    protected List<IPSet> findAws(WafRegionalClient client, Map<String, String> filters) {
        List<IPSet> ipSets = new ArrayList<>();

        try {
            ipSets.add(client.getIPSet(r -> r.ipSetId(filters.get("id"))).ipSet());
        } catch (WafNonexistentItemException ignore) {
            //ignore
        }

        return ipSets;
    }
}