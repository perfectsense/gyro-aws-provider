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

package gyro.aws.waf.common;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;

public class FieldToMatch extends Diffable implements Copyable<software.amazon.awssdk.services.waf.model.FieldToMatch> {
    private String data;
    private String type;

    /**
     * If type selected as ``HEADER`` or ``SINGLE_QUERY_ARG``, the value needs to be provided.
     */
    public String getData() {
        return data != null ? data.toLowerCase() : null;
    }

    public void setData(String data) {
        this.data = data;
    }

    /**
     * Part of the request to filter on.
     */
    @Required
    public String getType() {
        return type != null ? type.toUpperCase() : null;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.waf.model.FieldToMatch fieldToMatch) {
        setData(fieldToMatch.data());
        setType(fieldToMatch.typeAsString());
    }

    @Override
    public String primaryKey() {
        return "field to match";
    }

    software.amazon.awssdk.services.waf.model.FieldToMatch toFieldToMatch() {
        return software.amazon.awssdk.services.waf.model.FieldToMatch.builder().type(getType()).data(getData()).build();
    }
}
