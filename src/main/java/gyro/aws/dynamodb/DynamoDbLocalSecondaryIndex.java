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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.LocalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.LocalSecondaryIndexDescription;
import software.amazon.awssdk.services.dynamodb.model.Projection;

public class DynamoDbLocalSecondaryIndex extends Diffable implements Copyable<LocalSecondaryIndexDescription> {

    private String name;
    private String rangeKey;
    private String projectionType;
    private List<String> nonKeyAttributes;

    /**
     * The name for the index. (Required)
     */
    @Required
    @Regex("[a-zA-Z0-9_.-]+")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The name of the range (sort) key for this index. (Required)
     */
    @Required
    public String getRangeKey() {
        return rangeKey;
    }

    public void setRangeKey(String rangeKey) {
        this.rangeKey = rangeKey;
    }

    /**
     * The set of attributes that are projected into this index. Valid values are ``ALL``, ``INCLUDE`` or ``KEYS_ONLY``.
     */
    @Required
    @ValidStrings({ "ALL", "INCLUDE", "KEYS_ONLY" })
    public String getProjectionType() {
        return projectionType;
    }

    public void setProjectionType(String projectionType) {
        this.projectionType = projectionType;
    }

    /**
     * Provides a list of attributes to project into the index. These do not need to be defined as attributes on the table. Required only when ``projection-type`` is set to ``INCLUDE``.
     */
    public List<String> getNonKeyAttributes() {
        if (nonKeyAttributes == null) {
            nonKeyAttributes = new ArrayList<>();
        }
        return nonKeyAttributes;
    }

    public void setNonKeyAttributes(List<String> nonKeyAttributes) {
        this.nonKeyAttributes = nonKeyAttributes;
    }

    @Override
    public void copyFrom(LocalSecondaryIndexDescription model) {
        setName(model.indexName());
        setProjectionType(Optional.ofNullable(model.projection())
            .map(Projection::projectionTypeAsString)
            .orElse(null));
        setNonKeyAttributes(Optional.ofNullable(model.projection())
            .map(Projection::nonKeyAttributes)
            .orElse(null));

        setRangeKey(null);
        if (model.hasKeySchema()) {
            model.keySchema().forEach(k -> {
                if (KeyType.RANGE.equals(k.keyType())) {
                    setRangeKey(k.attributeName());
                }
            });
        }
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    LocalSecondaryIndex toLocalSecondaryIndex() {
        LocalSecondaryIndex.Builder builder = LocalSecondaryIndex.builder()
            .indexName(getName())
            .keySchema(createKeySchemas());

        if (!getNonKeyAttributes().isEmpty()) {
            builder.projection(p -> p.projectionType(getProjectionType()).nonKeyAttributes(getNonKeyAttributes()));
        } else {
            builder.projection(p -> p.projectionType(getProjectionType()));
        }

        return builder.build();
    }

    private List<KeySchemaElement> createKeySchemas() {
        List<KeySchemaElement> keySchemas = new ArrayList<>();
        String hashKey = ((DynamoDbTableResource) parentResource()).getHashKey();
        keySchemas.add(KeySchemaElement.builder().attributeName(hashKey).keyType(KeyType.HASH).build());

        if (rangeKey != null) {
            keySchemas.add(KeySchemaElement.builder().attributeName(rangeKey).keyType(KeyType.RANGE).build());
        }

        return keySchemas;
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (!getNonKeyAttributes().isEmpty() && !"INCLUDE".equals(getProjectionType())) {
            errors.add(new ValidationError(
                this,
                "projection-type",
                "The 'projection-type' must be 'INCLUDE' when specifying 'non-key-attributes'!"));
        }

        return errors;
    }
}
