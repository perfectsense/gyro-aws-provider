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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.wafv2.model.RateLimitCookie;

public class RateLimitCookieResource extends Diffable implements Copyable<RateLimitCookie> {
    private String name;
    private Set<TextTransformationResource> textTransformation;

    /**
     * The name of the cookie to use for the rate limit.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The text transformations to apply to the cookie before using it for the rate limit.
     *
     * @subresource gyro.aws.wafv2.TextTransformationResource
     */
    public Set<TextTransformationResource> getTextTransformation() {
        if (textTransformation == null) {
            textTransformation = new HashSet<>();
        }

        return textTransformation;
    }

    public void setTextTransformation(Set<TextTransformationResource> textTransformation) {
        this.textTransformation = textTransformation;
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    @Override
    public void copyFrom(RateLimitCookie model) {
        setName(model.name());
        getTextTransformation().clear();
        if (model.textTransformations() != null) {
            model.textTransformations().forEach(o -> {
                TextTransformationResource textTransformation = newSubresource(TextTransformationResource.class);
                textTransformation.copyFrom(o);
                getTextTransformation().add(textTransformation);
            });
        }
    }

    RateLimitCookie toRateLimitCookie() {
        return RateLimitCookie.builder()
            .name(getName())
            .textTransformations(getTextTransformation().stream()
                .map(TextTransformationResource::toTextTransformation)
                .collect(Collectors.toSet()))
            .build();
    }
}
