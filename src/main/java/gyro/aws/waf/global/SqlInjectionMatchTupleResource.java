package gyro.aws.waf.global;

import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchSet;
import software.amazon.awssdk.services.waf.model.WafInvalidOperationException;
import software.amazon.awssdk.services.waf.model.WafLimitsExceededException;

import java.util.Set;
import java.util.stream.Collectors;

public class SqlInjectionMatchTupleResource extends gyro.aws.waf.common.SqlInjectionMatchTupleResource {
    @Override
    protected void saveSqlInjectionMatchTuple(boolean isDelete) {
        WafClient client = getGlobalClient();

        try {
            saveTuple(client, isDelete);
        } catch (WafLimitsExceededException ex) {
            handleLimit(client);

            saveTuple(client, isDelete);
        }
    }

    private void saveTuple(WafClient client, boolean isDelete) {
        try {
            client.updateSqlInjectionMatchSet(
                toUpdateSqlInjectionMatchSetRequest(isDelete)
                    .changeToken(client.getChangeToken().changeToken())
                    .build()
            );
        } catch (WafInvalidOperationException ex) {
            if (!isDelete || !ex.awsErrorDetails().errorCode().equals("WAFInvalidOperationException")) {
                throw ex;
            }
        }
    }

    private void handleLimit(WafClient client) {
        SqlInjectionMatchSetResource parent = (SqlInjectionMatchSetResource) parent();

        Set<String> pendingSqlInjectionMatchTupleKeys = parent.getSqlInjectionMatchTuple().stream().map(SqlInjectionMatchTupleResource::primaryKey).collect(Collectors.toSet());

        SqlInjectionMatchSet sqlInjectionMatchSet = parent.getSqlInjectionMatchSet(client);

        SqlInjectionMatchSetResource sqlInjectionMatchSetResource = new SqlInjectionMatchSetResource();

        sqlInjectionMatchSetResource.copyFrom(sqlInjectionMatchSet);

        SqlInjectionMatchTupleResource sqlInjectionMatchTupleResource = sqlInjectionMatchSetResource.getSqlInjectionMatchTuple().stream().filter(o -> !pendingSqlInjectionMatchTupleKeys.contains(o.primaryKey())).findFirst().orElse(null);

        if (sqlInjectionMatchTupleResource != null) {
            sqlInjectionMatchTupleResource.saveTuple(client, true);
        }
    }
}
