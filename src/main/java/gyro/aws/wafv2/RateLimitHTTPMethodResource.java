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
import software.amazon.awssdk.services.wafv2.model.RateLimitHTTPMethod;

public class RateLimitHTTPMethodResource extends Diffable implements Copyable<RateLimitHTTPMethod> {
    @Override
    public void copyFrom(RateLimitHTTPMethod model) {

    }

    RateLimitHTTPMethod toRateLimitHTTPMethod() {
        return RateLimitHTTPMethod.builder().build();
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
