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

import software.amazon.awssdk.services.waf.model.ByteMatchSet;
import software.amazon.awssdk.services.waf.model.WafInvalidOperationException;
import software.amazon.awssdk.services.waf.model.WafLimitsExceededException;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.Set;
import java.util.stream.Collectors;

public class ByteMatchTupleResource extends gyro.aws.waf.common.ByteMatchTupleResource {
    @Override
    protected void saveByteMatchTuple(boolean isDelete) {
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
            client.updateByteMatchSet(
                toByteMatchSetUpdateRequest(isDelete)
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
        ByteMatchSetResource parent = (ByteMatchSetResource) parent();

        Set<String> pendingByteMatchTupleKeys = parent.getByteMatchTuple().stream().map(ByteMatchTupleResource::primaryKey).collect(Collectors.toSet());

        ByteMatchSet byteMatchSet = parent.getByteMatchSet(client);

        ByteMatchSetResource byteMatchSetResource = new ByteMatchSetResource();

        byteMatchSetResource.copyFrom(byteMatchSet);

        ByteMatchTupleResource byteMatchTupleResource = byteMatchSetResource.getByteMatchTuple().stream().filter(o -> !pendingByteMatchTupleKeys.contains(o.primaryKey())).findFirst().orElse(null);

        if (byteMatchTupleResource != null) {
            byteMatchTupleResource.saveTuple(client, true);
        }
    }
}
