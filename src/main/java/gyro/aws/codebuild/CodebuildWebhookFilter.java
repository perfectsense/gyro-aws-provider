/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.codebuild.model.WebhookFilter;

public class CodebuildWebhookFilter extends Diffable implements Copyable<WebhookFilter> {

    private String pattern;
    private String type;
    private Boolean excludeMatchedPattern;

    /**
     * Based on the 'type', if 'type' is 'EVENT', the pattern is a comma-separated string that specifies one or more events.
     * If 'type' is not 'EVENT', the pattern is a regular expression pattern.
     */
    @Updatable
    @Required
    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * The type of webhook filter.
     */
    @Updatable
    @Required
    @ValidStrings({ "EVENT", "BASE_REF", "HEAD_REF", "ACTOR_ACCOUNT_ID", "FILE_PATH", "COMMIT_MESSAGE" })
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The field that specifies if the pattern determines which webhook events that do not trigger a build.
     */
    @Updatable
    public Boolean getExcludeMatchedPattern() {
        return excludeMatchedPattern;
    }

    public void setExcludeMatchedPattern(Boolean excludeMatchedPattern) {
        this.excludeMatchedPattern = excludeMatchedPattern;
    }

    @Override
    public void copyFrom(WebhookFilter model) {
        setPattern(model.pattern());
        setType(model.typeAsString());
        setExcludeMatchedPattern(model.excludeMatchedPattern());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
