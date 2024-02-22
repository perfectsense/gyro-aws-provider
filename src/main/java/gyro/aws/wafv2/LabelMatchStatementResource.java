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
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.wafv2.model.LabelMatchScope;
import software.amazon.awssdk.services.wafv2.model.LabelMatchStatement;

public class LabelMatchStatementResource extends Diffable implements Copyable<LabelMatchStatement> {

    private LabelMatchScope scope;
    private String key;

    /**
     * The part of the web request that you want AWS WAF to inspect.
     */
    @Required
    @ValidStrings({ "LABEL", "NAMESPACE" })
    public LabelMatchScope getScope() {
        return scope;
    }

    public void setScope(LabelMatchScope scope) {
        this.scope = scope;
    }

    /**
     * The value that you want AWS WAF to search for.
     */
    @Required
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String primaryKey() {
        return String.format("label match statement - '%s'", getScope());
    }

    @Override
    public void copyFrom(LabelMatchStatement labelMatchStatement) {
        setScope(labelMatchStatement.scope());
        setKey(labelMatchStatement.key());
    }

    LabelMatchStatement toLabelMatchStatement() {
        return LabelMatchStatement.builder()
            .scope(getScope())
            .key(getKey())
            .build();
    }

}
