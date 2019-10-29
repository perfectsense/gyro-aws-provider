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

import software.amazon.awssdk.services.waf.model.RegexMatchSet;
import software.amazon.awssdk.services.waf.model.RegexMatchTuple;
import software.amazon.awssdk.services.waf.model.WafInvalidOperationException;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

public class RegexMatchTupleResource extends gyro.aws.waf.common.RegexMatchTupleResource {
    @Override
    protected void saveRegexMatchTuple(RegexMatchTuple regexMatchTuple, boolean isDelete) {
        WafRegionalClient client = getRegionalClient();

        // Handle if replacing regex match tuple.
        if (!isDelete) {
            RegexMatchSetResource parent = (RegexMatchSetResource) parent();

            RegexMatchSet regexMatchSet = parent.getRegexMatchSet(client);

            if (!regexMatchSet.regexMatchTuples().isEmpty()) {
                client.updateRegexMatchSet(toUpdateRegexMatchSetRequest(regexMatchSet.regexMatchTuples().get(0), true)
                    .changeToken(client.getChangeToken().changeToken())
                    .build()
                );
            }
        }

        try {
            client.updateRegexMatchSet(toUpdateRegexMatchSetRequest(regexMatchTuple, isDelete)
                .changeToken(client.getChangeToken().changeToken())
                .build()
            );
        } catch (WafInvalidOperationException ex) {
            if (!isDelete || !ex.awsErrorDetails().errorCode().equals("WAFInvalidOperationException")) {
                throw ex;
            }
        }
    }
}
