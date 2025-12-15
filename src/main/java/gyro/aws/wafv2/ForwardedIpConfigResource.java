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
import software.amazon.awssdk.services.wafv2.model.ForwardedIPConfig;

public class ForwardedIpConfigResource extends Diffable implements Copyable<ForwardedIPConfig> {

    private FallbackBehavior fallbackBehavior;
    private String headerName;

    /**
     * The match status to assign to the web request if the request doesn't have a valid IP address in the specified position.
     */
    @ValidStrings({"MATCH", "NO_MATCH"})
    @Required
    @Updatable
    public FallbackBehavior getFallbackBehavior() {
        return fallbackBehavior;
    }

    public void setFallbackBehavior(FallbackBehavior fallbackBehavior) {
        this.fallbackBehavior = fallbackBehavior;
    }

    /**
     * The name of the HTTP header to be used for the IP address.
     */
    @Required
    @Updatable
    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(ForwardedIPConfig model) {
        setFallbackBehavior(model.fallbackBehavior());
        setHeaderName(model.headerName());
    }

    ForwardedIPConfig toForwardedIPConfig() {
        return ForwardedIPConfig.builder()
            .fallbackBehavior(getFallbackBehavior())
            .headerName(getHeaderName())
            .build();
    }
}
