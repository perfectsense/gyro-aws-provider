/*
 * Copyright 2019, Perfect Sense, Inc.
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

package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.s3.model.Tag;

public class S3Tag extends Diffable implements Copyable<Tag> {
    private String key;
    private String value;

    /**
     * The tag's key. (Required)
     */
    @Updatable
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * The tag's value. (Required)
     */
    @Updatable
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String primaryKey() {
        return getKey() + " - " + getValue();
    }

    @Override
    public void copyFrom(Tag tag) {
        setKey(tag.key());
        setValue(tag.value());
    }

    Tag toTag() {
        return Tag.builder()
                .key(getKey())
                .value(getValue())
                .build();
    }
}
