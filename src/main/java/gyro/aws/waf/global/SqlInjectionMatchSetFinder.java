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
import software.amazon.awssdk.services.waf.model.ListSqlInjectionMatchSetsRequest;
import software.amazon.awssdk.services.waf.model.ListSqlInjectionMatchSetsResponse;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchSet;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchSetSummary;
import software.amazon.awssdk.services.waf.model.WafNonexistentItemException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query sql injection match set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    sql-injection-match-sets: $(external-query aws::waf-sql-injection-match-set)
 */
@Type("waf-sql-injection-match-set")
public class SqlInjectionMatchSetFinder extends gyro.aws.waf.common.SqlInjectionMatchSetFinder<WafClient, SqlInjectionMatchSetResource> {
    @Override
    protected List<SqlInjectionMatchSet> findAllAws(WafClient client) {
        List<SqlInjectionMatchSet> sqlInjectionMatchSets = new ArrayList<>();

        String marker = null;
        ListSqlInjectionMatchSetsResponse response;
        List<SqlInjectionMatchSetSummary> sqlInjectionMatchSetSummaries = new ArrayList<>();

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listSqlInjectionMatchSets();
            } else {
                response = client.listSqlInjectionMatchSets(ListSqlInjectionMatchSetsRequest.builder().nextMarker(marker).build());
            }

            marker = response.nextMarker();
            sqlInjectionMatchSetSummaries.addAll(response.sqlInjectionMatchSets());

        } while (!ObjectUtils.isBlank(marker));

        for (SqlInjectionMatchSetSummary sqlInjectionMatchSetSummary : sqlInjectionMatchSetSummaries) {
            sqlInjectionMatchSets.add(client.getSqlInjectionMatchSet(r -> r.sqlInjectionMatchSetId(sqlInjectionMatchSetSummary.sqlInjectionMatchSetId())).sqlInjectionMatchSet());
        }

        return sqlInjectionMatchSets;
    }

    @Override
    protected List<SqlInjectionMatchSet> findAws(WafClient client, Map<String, String> filters) {
        List<SqlInjectionMatchSet> sqlInjectionMatchSets = new ArrayList<>();

        try {
            sqlInjectionMatchSets.add(client.getSqlInjectionMatchSet(r -> r.sqlInjectionMatchSetId(filters.get("id"))).sqlInjectionMatchSet());
        } catch (WafNonexistentItemException ignore) {
            //ignore
        }

        return sqlInjectionMatchSets;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }
}