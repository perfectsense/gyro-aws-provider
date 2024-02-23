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

import java.util.HashMap;
import java.util.Map;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.wafv2.model.CustomHTTPHeader;
import software.amazon.awssdk.services.wafv2.model.CustomRequestHandling;

public class RuleActionCustomRequestHandlingResource extends Diffable implements Copyable<CustomRequestHandling> {

    private Map<String, String> insertHeaders;

    /**
     * A list of custom request handling actions to be inserted into the request header.
     */
    @Required
    @Updatable
    public Map<String, String> getInsertHeaders() {
        if (insertHeaders == null) {
            insertHeaders = new HashMap<>();
        }

        return insertHeaders;
    }

    public void setInsertHeaders(Map<String, String> insertHeaders) {
        this.insertHeaders = insertHeaders;
    }

    @Override
    public void copyFrom(CustomRequestHandling model) {
        setInsertHeaders(null);
        if (model.insertHeaders() != null) {
            model.insertHeaders().forEach(h -> getInsertHeaders().put(h.name(), h.value()));
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    CustomRequestHandling toCustomRequestHandling() {
        return CustomRequestHandling.builder()
            .insertHeaders(getInsertHeaders().entrySet().stream()
                .map(e -> CustomHTTPHeader.builder()
                    .name(e.getKey())
                    .value(e.getValue())
                    .build())
                .collect(java.util.stream.Collectors.toList()))
            .build();
    }
}
