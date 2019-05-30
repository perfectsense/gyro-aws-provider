package gyro.aws.waf.regional;

import gyro.core.Type;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchSet;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchSetSummary;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Type("sql-injection-match-set-regional")
public class SqlInjectionMatchSetFinder extends gyro.aws.waf.common.SqlInjectionMatchSetFinder<WafRegionalClient, SqlInjectionMatchSetResource> {
    @Override
    protected List<SqlInjectionMatchSet> findAllAws(WafRegionalClient client) {
        List<SqlInjectionMatchSet> sqlInjectionMatchSets = new ArrayList<>();

        List<SqlInjectionMatchSetSummary> sqlInjectionMatchSetSummaries = client.listSqlInjectionMatchSets().sqlInjectionMatchSets();

        for (SqlInjectionMatchSetSummary sqlInjectionMatchSetSummary : sqlInjectionMatchSetSummaries) {
            sqlInjectionMatchSets.add(client.getSqlInjectionMatchSet(r -> r.sqlInjectionMatchSetId(sqlInjectionMatchSetSummary.sqlInjectionMatchSetId())).sqlInjectionMatchSet());
        }

        return sqlInjectionMatchSets;
    }

    @Override
    protected List<SqlInjectionMatchSet> findAws(WafRegionalClient client, Map<String, String> filters) {
        List<SqlInjectionMatchSet> sqlInjectionMatchSets = new ArrayList<>();

        if (filters.containsKey("sql-injection-match-set-id")) {
            sqlInjectionMatchSets.add(client.getSqlInjectionMatchSet(r -> r.sqlInjectionMatchSetId(filters.get("sql-injection-match-set-id"))).sqlInjectionMatchSet());
        }

        return sqlInjectionMatchSets;
    }
}