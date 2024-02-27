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
import software.amazon.awssdk.services.wafv2.model.BodyParsingFallbackBehavior;
import software.amazon.awssdk.services.wafv2.model.JsonBody;
import software.amazon.awssdk.services.wafv2.model.JsonMatchScope;
import software.amazon.awssdk.services.wafv2.model.OversizeHandling;

public class FieldMatchJsonBodyResource extends Diffable implements Copyable<JsonBody> {

    private FieldMatchJsonPatternResource matchPattern;
    private JsonMatchScope matchScope;
    private BodyParsingFallbackBehavior invalidFallbackBehavior;
    private OversizeHandling oversizeHandling;

    /**
     * The match pattern for the body.
     *
     * @subresource gyro.aws.wafv2.FieldMatchJsonPatternResource
     */
    @Required
    @Updatable
    public FieldMatchJsonPatternResource getMatchPattern() {
        return matchPattern;
    }

    public void setMatchPattern(FieldMatchJsonPatternResource matchPattern) {
        this.matchPattern = matchPattern;
    }

    /**
     * The match scope for the body.
     */
    @Required
    @Updatable
    @ValidStrings({"ALL", "KEY", "VALUE"})
    public JsonMatchScope getMatchScope() {
        return matchScope;
    }

    public void setMatchScope(JsonMatchScope matchScope) {
        this.matchScope = matchScope;
    }

    /**
     * The invalid fallback behavior for the body.
     */
    @Updatable
    @ValidStrings({"MATCH", "NO_MATCH", "EVALUATE_AS_STRING"})
    public BodyParsingFallbackBehavior getInvalidFallbackBehavior() {
        return invalidFallbackBehavior;
    }

    public void setInvalidFallbackBehavior(BodyParsingFallbackBehavior invalidFallbackBehavior) {
        this.invalidFallbackBehavior = invalidFallbackBehavior;
    }

    /**
     * The oversize handling for the body.
     */
    @Required
    @Updatable
    @ValidStrings({"CONTINUE", "MATCH", "NO_MATCH"})
    public OversizeHandling getOversizeHandling() {
        return oversizeHandling;
    }

    public void setOversizeHandling(OversizeHandling oversizeHandling) {
        this.oversizeHandling = oversizeHandling;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(JsonBody model) {
        setMatchScope(model.matchScope());
        setInvalidFallbackBehavior(model.invalidFallbackBehavior());
        setOversizeHandling(model.oversizeHandling());
        setMatchPattern(null);
        if (model.matchPattern() != null) {
            FieldMatchJsonPatternResource matchPattern = newSubresource(FieldMatchJsonPatternResource.class);
            matchPattern.copyFrom(model.matchPattern());
            setMatchPattern(matchPattern);
        }
    }

    JsonBody toJsonBody() {
        return JsonBody.builder()
            .matchScope(getMatchScope())
            .invalidFallbackBehavior(getInvalidFallbackBehavior())
            .oversizeHandling(getOversizeHandling())
            .matchPattern(getMatchPattern() != null ? getMatchPattern().toJsonMatchPattern() : null)
            .build();
    }
}
