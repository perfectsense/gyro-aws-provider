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
import software.amazon.awssdk.services.wafv2.model.RateLimitUriPath;

public class RateLimitUriPathResource extends Diffable implements Copyable<RateLimitUriPath> {
    private Set<TextTransformationResource> textTransformation;

    /**
     * The text transformations to apply to the URI path before using it for the rate limit.
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
        return "";
    }

    @Override
    public void copyFrom(RateLimitUriPath model) {
        getTextTransformation().clear();
        if (model.textTransformations() != null) {
            model.textTransformations().forEach(o -> {
                TextTransformationResource textTransformation = newSubresource(TextTransformationResource.class);
                textTransformation.copyFrom(o);
                getTextTransformation().add(textTransformation);
            });
        }
    }

    RateLimitUriPath toRateLimitUriPath() {
        return RateLimitUriPath.builder()
            .textTransformations(getTextTransformation().stream()
                .map(TextTransformationResource::toTextTransformation)
                .collect(Collectors.toSet()))
            .build();
    }
}
