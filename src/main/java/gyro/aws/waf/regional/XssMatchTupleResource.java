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

import software.amazon.awssdk.services.waf.model.WafInvalidOperationException;
import software.amazon.awssdk.services.waf.model.WafLimitsExceededException;
import software.amazon.awssdk.services.waf.model.XssMatchSet;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.Set;
import java.util.stream.Collectors;

public class XssMatchTupleResource extends gyro.aws.waf.common.XssMatchTupleResource {
    @Override
    protected void saveXssMatchTuple(boolean isDelete) {
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
            client.updateXssMatchSet(
                toUpdateXssMatchSetRequest(isDelete)
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
        XssMatchSetResource parent = (XssMatchSetResource) parent();

        Set<String> pendingXssMatchTupleKeys = parent.getXssMatchTuple().stream().map(XssMatchTupleResource::primaryKey).collect(Collectors.toSet());

        XssMatchSet xssMatchSet = parent.getXssMatchSet(client);

        XssMatchSetResource xssMatchSetResource = new XssMatchSetResource();

        xssMatchSetResource.copyFrom(xssMatchSet);

        XssMatchTupleResource xssMatchTupleResource = xssMatchSetResource.getXssMatchTuple().stream().filter(o -> !pendingXssMatchTupleKeys.contains(o.primaryKey())).findFirst().orElse(null);

        if (xssMatchTupleResource != null) {
            xssMatchTupleResource.saveTuple(client, true);
        }
    }
}
