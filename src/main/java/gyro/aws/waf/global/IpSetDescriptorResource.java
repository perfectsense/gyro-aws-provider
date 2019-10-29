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
import software.amazon.awssdk.services.waf.model.IPSetDescriptor;

public class IpSetDescriptorResource extends gyro.aws.waf.common.IpSetDescriptorResource {
    @Override
    protected void saveIpSetDescriptor(IPSetDescriptor ipSetDescriptor, boolean isDelete) {
        WafClient client = getGlobalClient();

        client.updateIPSet(toUpdateIpSetRequest(ipSetDescriptor, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }
}
