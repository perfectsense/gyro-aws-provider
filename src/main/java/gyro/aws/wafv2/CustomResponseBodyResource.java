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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.wafv2.model.CustomResponseBody;
import software.amazon.awssdk.services.wafv2.model.ResponseContentType;

public class CustomResponseBodyResource extends Diffable implements Copyable<CustomResponseBody> {

    private String name;
    private String content;
    private ResponseContentType contentType;

    /**
     * The custom response body name.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The custom response body content.
     */
    @Required
    @Updatable
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Required
    @Updatable
    @ValidStrings({ "TEXT_PLAIN", "TEXT_HTML", "APPLICATION_JSON" })
    public ResponseContentType getContentType() {
        return contentType;
    }

    public void setContentType(ResponseContentType contentType) {
        this.contentType = contentType;
    }

    @Override
    public void copyFrom(CustomResponseBody model) {
        setContent(model.content());
        setContentType(model.contentType());
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    CustomResponseBody toCustomResponseBody() {
        return CustomResponseBody.builder()
            .content(getContent())
            .contentType(getContentType())
            .build();
    }
}
