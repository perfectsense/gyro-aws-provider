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

import software.amazon.awssdk.services.waf.model.SqlInjectionMatchSet;
import software.amazon.awssdk.services.waf.model.WafInvalidOperationException;
import software.amazon.awssdk.services.waf.model.WafLimitsExceededException;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.Set;
import java.util.stream.Collectors;

public class SqlInjectionMatchTupleResource extends gyro.aws.waf.common.SqlInjectionMatchTupleResource {
    @Override
    protected void saveSqlInjectionMatchTuple(boolean isDelete) {
        WafRegionalClient client = getRegionalClient();

        try {
            saveTuple(client, isDelete);
        } catch (WafLimitsExceededException ex) {
            handleLimit(client);

            saveTuple(client, isDelete);
        }
    }

    private void saveTuple(WafRegionalClient client, boolean isDelete) {
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

    private void handleLimit(WafRegionalClient client) {
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
