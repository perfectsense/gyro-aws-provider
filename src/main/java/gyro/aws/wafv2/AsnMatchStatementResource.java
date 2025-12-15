/*
 * Copyright 2025, Brightspot.
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

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.CollectionMax;
import gyro.core.validation.Max;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.wafv2.model.AsnMatchStatement;

public class AsnMatchStatementResource extends Diffable implements Copyable<AsnMatchStatement> {

    private List<Long> asnList;
    private ForwardedIpConfigResource forwardedIpConfig;

    /**
     * The ASN list for the matching.
     */
    @Required
    @CollectionMax(100)
    public List<Long> getAsnList() {
        if(asnList == null) {
            asnList = new ArrayList<>();
        }

        return asnList;
    }

    public void setAsnList(List<Long> asnList) {
        this.asnList = asnList;
    }

    /**
     * The forwarded IP configuration for the ASN match statement.
     *
     * @subresource gyro.aws.wafv2.ForwardedIpConfigResource
     */
    @Updatable
    public ForwardedIpConfigResource getForwardedIpConfig() {
        return forwardedIpConfig;
    }

    public void setForwardedIpConfig(ForwardedIpConfigResource forwardedIpConfig) {
        this.forwardedIpConfig = forwardedIpConfig;
    }

    @Override
    public String primaryKey() {
        return getAsnList().isEmpty()
            ? "empty-asn-list"
            : getAsnList().toString();
    }

    @Override
    public void copyFrom(AsnMatchStatement asnMatchStatement) {
        setAsnList(asnMatchStatement.asnList());

        setForwardedIpConfig(null);
        if (asnMatchStatement.forwardedIPConfig() != null) {
            ForwardedIpConfigResource forwardedIpConfig = newSubresource(ForwardedIpConfigResource.class);
            forwardedIpConfig.copyFrom(asnMatchStatement.forwardedIPConfig());
            setForwardedIpConfig(forwardedIpConfig);
        }
    }

    AsnMatchStatement toAsnMatchStatement() {
        AsnMatchStatement.Builder builder = AsnMatchStatement.builder().asnList(getAsnList());

        if (getForwardedIpConfig() != null) {
            builder.forwardedIPConfig(getForwardedIpConfig().toForwardedIPConfig());
        }

        return builder.build();
    }
}
