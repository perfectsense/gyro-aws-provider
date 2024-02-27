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

package gyro.aws.wafv2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.wafv2.model.TextTransformation;
import software.amazon.awssdk.services.wafv2.model.TextTransformationType;

public class TextTransformationResource extends Diffable implements Copyable<TextTransformation> {

    private Integer priority;
    private TextTransformationType type;

    /**
     * The priority of the text transformation.
     */
    @Required
    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * The type of the text transformation.
     */
    @Required
    @ValidStrings({"NONE", "COMPRESS_WHITE_SPACE", "HTML_ENTITY_DECODE", "LOWERCASE", "CMD_LINE",
        "URL_DECODE", "BASE64_DECODE", "HEX_DECODE", "MD5", "REPLACE_COMMENTS", "ESCAPE_SEQ_DECODE",
        "SQL_HEX_DECODE", "CSS_DECODE", "JS_DECODE", "NORMALIZE_PATH", "NORMALIZE_PATH_WIN",
        "REMOVE_NULLS", "REPLACE_NULLS", "BASE64_DECODE_EXT", "URL_DECODE_UNI", "UTF8_TO_UNICODE"})
    public TextTransformationType getType() {
        return type;
    }

    public void setType(TextTransformationType type) {
        this.type = type;
    }

    @Override
    public String primaryKey() {
        return String.format("transformation type - '%s' with priority - %s", getType(), getPriority());
    }

    @Override
    public void copyFrom(TextTransformation textTransformation) {
        setPriority(textTransformation.priority());
        setType(textTransformation.type());
    }

    TextTransformation toTextTransformation() {
        return TextTransformation.builder()
            .priority(getPriority())
            .type(getType())
            .build();
    }
}
