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
import software.amazon.awssdk.services.waf.model.ActivatedRule;

public class ActivatedRuleResource extends gyro.aws.waf.common.ActivatedRuleResource {
    @Override
    protected void saveActivatedRule(ActivatedRule activatedRule, boolean isDelete) {
        WebAclResource parent = (WebAclResource) parent();

        if (!isDelete || parent.isActivatedRulePresent(activatedRule)) {

            WafClient client = getGlobalClient();

            client.updateWebACL(
                toUpdateWebAclRequest(parent, activatedRule, isDelete)
                    .changeToken(client.getChangeToken().changeToken())
                    .build()
            );
        }
    }

    @Override
    protected void handlePriority() {
        WebAclResource parent = (WebAclResource) parent();

        ActivatedRule activatedRule = parent.getActivatedRuleWithPriority(getPriority());
        if (activatedRule != null) {
            //delete conflicting activated rule with same priority
            saveActivatedRule(activatedRule, true);
        }
    }
}
