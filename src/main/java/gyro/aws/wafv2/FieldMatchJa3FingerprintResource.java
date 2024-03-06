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
import software.amazon.awssdk.services.wafv2.model.JA3Fingerprint;

public class FieldMatchJa3FingerprintResource extends Diffable implements Copyable<JA3Fingerprint> {

    private FallbackBehavior fallbackBehavior;

    /**
     * The fallback behavior for the JA3 fingerprint.
     */
    @Required
    @Updatable
    @ValidStrings({"MATCH", "NO_MATCH"})
    public FallbackBehavior getFallbackBehavior() {
        return fallbackBehavior;
    }

    public void setFallbackBehavior(FallbackBehavior fallbackBehavior) {
        this.fallbackBehavior = fallbackBehavior;
    }

    @Override
    public void copyFrom(JA3Fingerprint model) {
        setFallbackBehavior(model.fallbackBehavior());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public JA3Fingerprint toJa3Fingerprint() {
        return JA3Fingerprint.builder()
            .fallbackBehavior(getFallbackBehavior())
            .build();
    }
}
