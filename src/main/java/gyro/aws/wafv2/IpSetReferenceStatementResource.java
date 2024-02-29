/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.wafv2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.wafv2.model.IPSetReferenceStatement;

public class IpSetReferenceStatementResource extends Diffable implements Copyable<IPSetReferenceStatement> {

    private IpSetResource ipSet;
    private IpSetForwardedIpConfigResource ipSetForwardedIpConfig;

    /**
     * The ip set resource to associate with.
     */
    @Required
    @Updatable
    public IpSetResource getIpSet() {
        return ipSet;
    }

    public void setIpSet(IpSetResource ipSet) {
        this.ipSet = ipSet;
    }

    /**
     * The forwarded IP configuration for the ip set.
     *
     * @subresource gyro.aws.wafv2.IpSetForwardedIpConfigResource
     */
    @Updatable
    public IpSetForwardedIpConfigResource getIpSetForwardedIpConfig() {
        return ipSetForwardedIpConfig;
    }

    public void setIpSetForwardedIpConfig(IpSetForwardedIpConfigResource ipSetForwardedIpConfig) {
        this.ipSetForwardedIpConfig = ipSetForwardedIpConfig;
    }

    @Override
    public void copyFrom(IPSetReferenceStatement ipSetReferenceStatement) {
        setIpSet(findById(IpSetResource.class, ipSetReferenceStatement.arn()));
        setIpSetForwardedIpConfig(null);
        if (ipSetReferenceStatement.ipSetForwardedIPConfig() != null) {
            IpSetForwardedIpConfigResource ipSetForwardedIpConfigResource = newSubresource(IpSetForwardedIpConfigResource.class);
            ipSetForwardedIpConfigResource.copyFrom(ipSetReferenceStatement.ipSetForwardedIPConfig());
            setIpSetForwardedIpConfig(ipSetForwardedIpConfigResource);
        }
    }

    @Override
    public String primaryKey() {
        return getIpSet() != null ? getIpSet().primaryKey() : "";
    }

    IPSetReferenceStatement toIpSetReferenceStatement() {
        return IPSetReferenceStatement.builder()
            .ipSetForwardedIPConfig(getIpSetForwardedIpConfig() != null ? getIpSetForwardedIpConfig().toForwardedIPConfig() : null)
            .arn(getIpSet().getArn())
            .build();
    }
}
