package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import software.amazon.awssdk.services.waf.model.ListSqlInjectionMatchSetsRequest;
import software.amazon.awssdk.services.waf.model.ListSqlInjectionMatchSetsResponse;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchSet;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchSetSummary;
import software.amazon.awssdk.services.waf.model.WafNonexistentItemException;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query sql injection match set regional.
 *
 * .. code-block:: gyro
 *
 *    sql-injection-match-sets: $(aws::sql-injection-match-set-regional EXTERNAL/* | id = '')
 */
@Type("sql-injection-match-set-regional")
public class SqlInjectionMatchSetFinder extends gyro.aws.waf.common.SqlInjectionMatchSetFinder<WafRegionalClient, SqlInjectionMatchSetResource> {
    @Override
    protected List<SqlInjectionMatchSet> findAllAws(WafRegionalClient client) {
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
    protected List<SqlInjectionMatchSet> findAws(WafRegionalClient client, Map<String, String> filters) {
        List<SqlInjectionMatchSet> sqlInjectionMatchSets = new ArrayList<>();

        try {
            sqlInjectionMatchSets.add(client.getSqlInjectionMatchSet(r -> r.sqlInjectionMatchSetId(filters.get("sql-injection-match-set-id"))).sqlInjectionMatchSet());
        } catch (WafNonexistentItemException ignore) {
            //ignore
        }

        return sqlInjectionMatchSets;
    }
}