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

package gyro.aws.autoscaling;

import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Range;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.autoscalingplans.model.TagFilter;

public class AutoScalingTagFilter extends Diffable implements Copyable<TagFilter> {

    private String key;
    private List<String> values;

    /**
     * The tag key.
     */
    @Required
    @Regex(value = "[\\u0020-\\uD7FF\\uE000-\\uFFFD\\uD800\\uDC00-\\uDBFF\\uDFFF\\r\\n\\t]*", message = "alphanumeric characters and symbols excluding basic ASCII control characters.")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * The tag values.
     */
    @Range(min = 0, max = 20)
    @Regex(value = "[\\u0020-\\uD7FF\\uE000-\\uFFFD\\uD800\\uDC00-\\uDBFF\\uDFFF\\r\\n\\t]*", message = "alphanumeric characters and symbols excluding basic ASCII control characters.")
    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public void copyFrom(TagFilter model) {
        setKey(model.key());
        setValues(model.values());
    }

    @Override
    public String primaryKey() {
        return getKey();
    }

    public TagFilter toTagFilter() {
        return TagFilter.builder()
            .key(getKey())
            .values(getValues())
            .build();
    }
}
