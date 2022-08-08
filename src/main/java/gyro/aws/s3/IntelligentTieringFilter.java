/*
 * Copyright 2022, Brightspot.
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.s3.model.IntelligentTieringAndOperator;

public class IntelligentTieringFilter
    extends Diffable implements Copyable<software.amazon.awssdk.services.s3.model.IntelligentTieringFilter> {

    private String prefix;
    private List<S3Tag> tag;

    /**
     * Filter prefix.
     */
    @Updatable
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * A list of tags to filter upon.
     */
    @Updatable
    public List<S3Tag> getTag() {
        if (tag == null) {
            tag = new ArrayList<>();
        }

        return tag;
    }

    public void setTag(List<S3Tag> tag) {
        this.tag = tag;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.s3.model.IntelligentTieringFilter model) {
        setPrefix(model.prefix());

        getTag().clear();
        if (model.tag() != null) {
            S3Tag tag = newSubresource(S3Tag.class);
            tag.copyFrom(model.tag());
            getTag().add(tag);
        }

        if (model.and() != null) {
            setPrefix(model.and().prefix());
            setTag(model.and().tags().stream().map(tag -> {
                S3Tag s3Tag = newSubresource(S3Tag.class);
                s3Tag.copyFrom(tag);
                return s3Tag;
            }).collect(Collectors.toList()));
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    protected software.amazon.awssdk.services.s3.model.IntelligentTieringFilter toIntelligentTieringFilter() {
        software.amazon.awssdk.services.s3.model.IntelligentTieringFilter.Builder builder = software.amazon
            .awssdk.services.s3.model.IntelligentTieringFilter.builder();

        if ((StringUtils.isBlank(getPrefix()) && getTag().size() > 1)
            || (!StringUtils.isBlank(getPrefix()) && !getTag().isEmpty())) {
            builder = builder.and(toTieringAndOperator());
        } else {
            builder = builder.tag(!getTag().isEmpty() ? getTag().get(0).toTag() : null)
                .prefix(getPrefix());
        }

        return builder.build();
    }

    private IntelligentTieringAndOperator toTieringAndOperator() {
        return IntelligentTieringAndOperator.builder()
            .prefix(getPrefix())
            .tags(getTag().stream().map(S3Tag::toTag).collect(Collectors.toList()))
            .build();
    }
}
