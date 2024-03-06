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
import software.amazon.awssdk.services.wafv2.model.Headers;
import software.amazon.awssdk.services.wafv2.model.MapMatchScope;
import software.amazon.awssdk.services.wafv2.model.OversizeHandling;

public class FieldMatchHeadersResource extends Diffable implements Copyable<Headers> {

    private FieldMatchHeaderMatchPatternResource matchPattern;
    private MapMatchScope matchScope;
    private OversizeHandling oversizeHandling;

    /**
     * The match pattern for the headers.
     *
     * @subresource gyro.aws.wafv2.FieldMatchHeaderMatchPatternResource
     */
    @Required
    @Updatable
    public FieldMatchHeaderMatchPatternResource getMatchPattern() {
        return matchPattern;
    }

    public void setMatchPattern(FieldMatchHeaderMatchPatternResource matchPattern) {
        this.matchPattern = matchPattern;
    }

    /**
     * The match scope for the headers.
     */
    @Required
    @Updatable
    @ValidStrings({"ALL", "KEY", "VALUE"})
    public MapMatchScope getMatchScope() {
        return matchScope;
    }

    public void setMatchScope(MapMatchScope matchScope) {
        this.matchScope = matchScope;
    }

    /**
     * The oversize handling for the headers.
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
    public void copyFrom(Headers model) {
        setMatchScope(model.matchScope());
        setOversizeHandling(model.oversizeHandling());
        setMatchPattern(null);
        if (model.matchPattern() != null) {
            FieldMatchHeaderMatchPatternResource matchPattern = newSubresource(FieldMatchHeaderMatchPatternResource.class);
            matchPattern.copyFrom(model.matchPattern());
            setMatchPattern(matchPattern);
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    Headers toHeaders() {
        return Headers.builder()
            .matchPattern(getMatchPattern().toHeaderMatchPattern())
            .matchScope(getMatchScope())
            .oversizeHandling(getOversizeHandling())
            .build();
    }
}
