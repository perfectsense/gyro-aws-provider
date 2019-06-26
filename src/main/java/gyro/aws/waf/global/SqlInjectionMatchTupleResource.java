package gyro.aws.waf.global;

import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchTuple;

public class SqlInjectionMatchTupleResource extends gyro.aws.waf.common.SqlInjectionMatchTupleResource {
    @Override
    protected void saveSqlInjectionMatchTuple(SqlInjectionMatchTuple sqlInjectionMatchTuple, boolean isDelete) {
        WafClient client = getGlobalClient();

        client.updateSqlInjectionMatchSet(toUpdateSqlInjectionMatchSetRequest(sqlInjectionMatchTuple, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }
}
