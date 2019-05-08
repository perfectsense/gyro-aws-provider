package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.waf.model.CreateSqlInjectionMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GetSqlInjectionMatchSetResponse;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchSet;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchTuple;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

//@ResourceName("sql-injection-match-set")
public class SqlInjectionMatchSetResource extends gyro.aws.waf.common.SqlInjectionMatchSetResource {
    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getSqlInjectionMatchSetId())) {
            return false;
        }

        GetSqlInjectionMatchSetResponse response = getRegionalClient().getSqlInjectionMatchSet(
            r -> r.sqlInjectionMatchSetId(getSqlInjectionMatchSetId())
        );

        SqlInjectionMatchSet sqlInjectionMatchSet = response.sqlInjectionMatchSet();
        setName(sqlInjectionMatchSet.name());

        getSqlInjectionMatchTuple().clear();
        for (SqlInjectionMatchTuple sqlInjectionMatchTuple : sqlInjectionMatchSet.sqlInjectionMatchTuples()) {
            SqlInjectionMatchTupleResource sqlInjectionMatchTupleResource = new SqlInjectionMatchTupleResource(sqlInjectionMatchTuple);
            sqlInjectionMatchTupleResource.parent(this);
            getSqlInjectionMatchTuple().add(sqlInjectionMatchTupleResource);
        }

        return true;
    }

    @Override
    public void create() {
        WafRegionalClient client = getRegionalClient();

        CreateSqlInjectionMatchSetResponse response = client.createSqlInjectionMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setSqlInjectionMatchSetId(response.sqlInjectionMatchSet().sqlInjectionMatchSetId());
    }

    @Override
    public void delete() {
        WafRegionalClient client = getRegionalClient();

        client.deleteSqlInjectionMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .sqlInjectionMatchSetId(getSqlInjectionMatchSetId())
        );
    }
}
