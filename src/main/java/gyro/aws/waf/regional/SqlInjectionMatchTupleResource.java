package gyro.aws.waf.regional;

import software.amazon.awssdk.services.waf.model.SqlInjectionMatchTuple;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

public class SqlInjectionMatchTupleResource extends gyro.aws.waf.common.SqlInjectionMatchTupleResource {
    public SqlInjectionMatchTupleResource() {

    }

    public SqlInjectionMatchTupleResource(SqlInjectionMatchTuple sqlInjectionMatchTuple) {
        setData(sqlInjectionMatchTuple.fieldToMatch().data());
        setType(sqlInjectionMatchTuple.fieldToMatch().typeAsString());
        setTextTransformation(sqlInjectionMatchTuple.textTransformationAsString());
    }
    @Override
    protected void saveSqlInjectionMatchTuple(SqlInjectionMatchTuple sqlInjectionMatchTuple, boolean isDelete) {
        WafRegionalClient client = getRegionalClient();

        client.updateSqlInjectionMatchSet(getUpdateSqlInjectionMatchSetRequest(sqlInjectionMatchTuple, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }
}
