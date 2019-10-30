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

package gyro.aws.waf.global;

import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.SizeConstraintSet;
import software.amazon.awssdk.services.waf.model.WafInvalidOperationException;
import software.amazon.awssdk.services.waf.model.WafLimitsExceededException;

import java.util.Set;
import java.util.stream.Collectors;

public class SizeConstraintResource extends gyro.aws.waf.common.SizeConstraintResource {
    @Override
    protected void saveSizeConstraint(boolean isDelete) {
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
            client.updateSizeConstraintSet(
                toUpdateSizeConstraintSetRequest(isDelete)
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
        SizeConstraintSetResource parent = (SizeConstraintSetResource) parent();

        Set<String> pendingSizeConstraintTupleKeys = parent.getSizeConstraint().stream().map(SizeConstraintResource::primaryKey).collect(Collectors.toSet());

        SizeConstraintSet sizeConstraintSet = parent.getSizeConstraintSet(client);

        SizeConstraintSetResource sizeConstraintSetResource = new SizeConstraintSetResource();

        sizeConstraintSetResource.copyFrom(sizeConstraintSet);

        SizeConstraintResource sizeConstraintResource = sizeConstraintSetResource.getSizeConstraint().stream().filter(o -> !pendingSizeConstraintTupleKeys.contains(o.primaryKey())).findFirst().orElse(null);

        if (sizeConstraintResource != null) {
            sizeConstraintResource.saveTuple(client, true);
        }
    }
}
