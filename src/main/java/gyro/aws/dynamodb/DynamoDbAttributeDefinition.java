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

package gyro.aws.dynamodb;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;

public class DynamoDbAttributeDefinition extends Diffable implements Copyable<AttributeDefinition> {

    private String name;
    private String type;

    /**
     * The name for the attribute.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The data type for the attribute. Valid values are ``S`` (string), ``N`` (number), or ``B`` (binary).
     */
    @Required
    @ValidStrings({ "S", "N", "B" })
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void copyFrom(AttributeDefinition model) {
        setName(model.attributeName());
        setType(model.attributeTypeAsString());
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    AttributeDefinition toAttributeDefinition() {
        return AttributeDefinition.builder()
            .attributeName(getName())
            .attributeType(getType())
            .build();
    }
}
