/*
 * Copyright 2020, Perfect Sense, Inc.
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
import software.amazon.awssdk.services.wafv2.model.TextTransformation;

public class TextTransformationResource extends WafDiffable implements Copyable<TextTransformation> {

    private Integer priority;
    private String type;
    private Integer hashCode;

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void copyFrom(TextTransformation textTransformation) {
        setPriority(textTransformation.priority());
        setType(textTransformation.typeAsString());
        setHashCode(textTransformation.hashCode());
    }

    TextTransformation toTextTransformation() {
        return TextTransformation.builder()
            .priority(getPriority())
            .type(getType())
            .build();
    }
}
