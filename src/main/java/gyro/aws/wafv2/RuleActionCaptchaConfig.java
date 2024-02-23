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
import gyro.core.validation.Min;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.wafv2.model.CaptchaConfig;

public class RuleActionCaptchaConfig extends Diffable implements Copyable<CaptchaConfig> {

    private Long immunityTime;

    /**
     * The time in seconds that the client should be immune to captcha after failing the challenge.
     */
    @Required
    @Updatable
    @Min(60)
    public Long getImmunityTime() {
        return immunityTime;
    }

    public void setImmunityTime(Long immunityTime) {
        this.immunityTime = immunityTime;
    }

    @Override
    public void copyFrom(CaptchaConfig model) {
        setImmunityTime(model.immunityTimeProperty().immunityTime());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    CaptchaConfig toCaptchaConfig() {
        return CaptchaConfig.builder()
            .immunityTimeProperty(i -> i.immunityTime(getImmunityTime()))
            .build();
    }
}
