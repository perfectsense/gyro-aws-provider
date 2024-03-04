/*
 * Copyright 2024, Brightspot.
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
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.wafv2.model.FallbackBehavior;
import software.amazon.awssdk.services.wafv2.model.ForwardedIPPosition;
import software.amazon.awssdk.services.wafv2.model.IPSetForwardedIPConfig;

public class IpSetForwardedIpConfigResource extends Diffable implements Copyable<IPSetForwardedIPConfig> {

    private String headerName;
    private FallbackBehavior fallbackBehavior;
    private ForwardedIPPosition position;

    /**
     * The name of the header to use for the forwarded IP.
     */
    @Required
    @Updatable
    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    /**
     * The fallback behavior for the forwarded IP.
     */
    @Required
    @Updatable
    @ValidStrings({ "MATCH", "NO_MATCH" })
    public FallbackBehavior getFallbackBehavior() {
        return fallbackBehavior;
    }

    public void setFallbackBehavior(FallbackBehavior fallbackBehavior) {
        this.fallbackBehavior = fallbackBehavior;
    }

    /**
     * The position of the forwarded IP.
     */
    @Required
    @Updatable
    @ValidStrings({ "FIRST", "LAST", "ANY" })
    public ForwardedIPPosition getPosition() {
        return position;
    }

    public void setPosition(ForwardedIPPosition position) {
        this.position = position;
    }

    @Override
    public void copyFrom(IPSetForwardedIPConfig model) {
        setHeaderName(model.headerName());
        setFallbackBehavior(model.fallbackBehavior());
        setPosition(model.position());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public IPSetForwardedIPConfig toForwardedIPConfig() {
        return IPSetForwardedIPConfig.builder()
            .headerName(getHeaderName())
            .fallbackBehavior(getFallbackBehavior())
            .position(getPosition() != null ? getPosition() : null)
            .build();
    }
}
