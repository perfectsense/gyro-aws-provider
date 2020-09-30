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

package gyro.aws.codebuild;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Regex;
import software.amazon.awssdk.services.codebuild.model.Tag;

public class CodebuildProjectTag extends Diffable implements Copyable<Tag> {

    private String key;
    private String value;

    /**
     * The tag's key.
     */
    @Regex("^([\\p{L}\\p{Z}\\p{N}_.:/=@+\\-]*)$")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * The tag's value.
     */
    @Regex("^([\\p{L}\\p{Z}\\p{N}_.:/=@+\\-]*)$")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void copyFrom(Tag model) {
        setKey(model.key());
        setValue(model.value());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public static List<Tag> toProjectTags(Map<String, String> tags) {
        List<Tag> projectTags = new ArrayList<>();

        for (String key : tags.keySet()) {
            projectTags.add(Tag.builder()
                .key(key)
                .value(tags.get(key))
                .build());
        }

        return projectTags;
    }
}
