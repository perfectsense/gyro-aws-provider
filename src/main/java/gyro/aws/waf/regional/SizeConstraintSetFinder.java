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
import software.amazon.awssdk.services.waf.model.ListSizeConstraintSetsRequest;
import software.amazon.awssdk.services.waf.model.ListSizeConstraintSetsResponse;
import software.amazon.awssdk.services.waf.model.SizeConstraintSet;
import software.amazon.awssdk.services.waf.model.SizeConstraintSetSummary;
import software.amazon.awssdk.services.waf.model.WafNonexistentItemException;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query size constraint set regional.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    size-constraint-sets: $(external-query aws::waf-size-constraint-set-regional)
 */
@Type("waf-size-constraint-set-regional")
public class SizeConstraintSetFinder extends gyro.aws.waf.common.SizeConstraintSetFinder<WafRegionalClient, SizeConstraintSetResource> {
    @Override
    protected List<SizeConstraintSet> findAllAws(WafRegionalClient client) {
        List<SizeConstraintSet> sizeConstraintSets = new ArrayList<>();

        String marker = null;
        ListSizeConstraintSetsResponse response;
        List<SizeConstraintSetSummary> sizeConstraintSetSummaries = new ArrayList<>();

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listSizeConstraintSets();
            } else {
                response = client.listSizeConstraintSets(ListSizeConstraintSetsRequest.builder().nextMarker(marker).build());
            }

            marker = response.nextMarker();
            sizeConstraintSetSummaries.addAll(response.sizeConstraintSets());

        } while (!ObjectUtils.isBlank(marker));

        for (SizeConstraintSetSummary sizeConstraintSetSummary : sizeConstraintSetSummaries) {
            sizeConstraintSets.add(client.getSizeConstraintSet(r -> r.sizeConstraintSetId(sizeConstraintSetSummary.sizeConstraintSetId())).sizeConstraintSet());
        }

        return sizeConstraintSets;
    }

    @Override
    protected List<SizeConstraintSet> findAws(WafRegionalClient client, Map<String, String> filters) {
        List<SizeConstraintSet> sizeConstraintSets = new ArrayList<>();

        try {
            sizeConstraintSets.add(client.getSizeConstraintSet(r -> r.sizeConstraintSetId(filters.get("id"))).sizeConstraintSet());
        } catch (WafNonexistentItemException ignore) {
            //ignore
        }

        return sizeConstraintSets;
    }
}