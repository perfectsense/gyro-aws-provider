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
import software.amazon.awssdk.services.waf.model.ByteMatchSet;
import software.amazon.awssdk.services.waf.model.ByteMatchSetSummary;
import software.amazon.awssdk.services.waf.model.ListByteMatchSetsRequest;
import software.amazon.awssdk.services.waf.model.ListByteMatchSetsResponse;
import software.amazon.awssdk.services.waf.model.WafNonexistentItemException;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query byte match set regional.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    byte-match-sets: $(external-query aws::waf-byte-match-set-regional)
 */
@Type("waf-byte-match-set-regional")
public class ByteMatchSetFinder extends gyro.aws.waf.common.ByteMatchSetFinder<WafRegionalClient, ByteMatchSetResource> {
    @Override
    protected List<ByteMatchSet> findAllAws(WafRegionalClient client) {
        List<ByteMatchSet> byteMatchSets = new ArrayList<>();

        String marker = null;
        ListByteMatchSetsResponse response;
        List<ByteMatchSetSummary> byteMatchSetSummaries = new ArrayList<>();

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listByteMatchSets();
            } else {
                response = client.listByteMatchSets(ListByteMatchSetsRequest.builder().nextMarker(marker).build());
            }

            marker = response.nextMarker();
            byteMatchSetSummaries.addAll(response.byteMatchSets());

        } while (!ObjectUtils.isBlank(marker));

        for (ByteMatchSetSummary byteMatchSetSummary : byteMatchSetSummaries) {
            byteMatchSets.add(client.getByteMatchSet(r -> r.byteMatchSetId(byteMatchSetSummary.byteMatchSetId())).byteMatchSet());
        }

        return byteMatchSets;
    }

    @Override
    protected List<ByteMatchSet> findAws(WafRegionalClient client, Map<String, String> filters) {
        List<ByteMatchSet> byteMatchSets = new ArrayList<>();

        if (filters.containsKey("id") && !ObjectUtils.isBlank(filters.get("id"))) {
            try {
                byteMatchSets.add(client.getByteMatchSet(r -> r.byteMatchSetId(filters.get("id"))).byteMatchSet());
            } catch (WafNonexistentItemException ignore) {
                //ignore
            }
        }

        return byteMatchSets;
    }
}