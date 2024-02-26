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
import gyro.core.validation.Regex;
import software.amazon.awssdk.services.wafv2.model.RateLimitLabelNamespace;

public class RateLimitLabelNamespaceResource extends Diffable implements Copyable<RateLimitLabelNamespace> {
    private String namespace;

    /**
     * The namespace to use for the rate limit.
     */
    @Regex(value = "^(?!:)(?:[A-Za-z0-9_-]+:){1,1023}$",
        message = "a string of 1 to 1024 characters and ends with a colon (:). "
            + "The string can contain only alphanumeric characters (A-Z, a-z, 0-9), hyphen (-), "
            + "underscore (_), and colon (:). The string cannot start with a colon (:).")
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String primaryKey() {
        return getNamespace();
    }

    @Override
    public void copyFrom(RateLimitLabelNamespace model) {
        setNamespace(model.namespace());
    }

    RateLimitLabelNamespace toRateLimitLabelNamespace() {
        return RateLimitLabelNamespace.builder()
            .namespace(getNamespace())
            .build();
    }
}
