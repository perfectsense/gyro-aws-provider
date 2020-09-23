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
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.dynamodb.model.CreateGlobalSecondaryIndexAction;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndexDescription;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughputDescription;

public class DynamoDbGlobalSecondaryIndex extends Diffable implements Copyable<GlobalSecondaryIndexDescription> {

    private String name;
    private String hashKey;
    private String rangeKey;
    private Long writeCapacity;
    private Long readCapacity;
    private String projectionType;
    private List<String> nonKeyAttributes;

    /**
     * The name for the index.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The name of the hash (partition) key for this index.
     */
    @Required
    public String getHashKey() {
        return hashKey;
    }

    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }

    /**
     * The name of the range (sort) key for this index.
     */
    public String getRangeKey() {
        return rangeKey;
    }

    public void setRangeKey(String rangeKey) {
        this.rangeKey = rangeKey;
    }

    /**
     * The maximum number of writes per second for this table before an exception is thrown. Required if ``billing-mode`` is set to ``PROVISIONED``.
     */
    @Updatable
    public Long getWriteCapacity() {
        return writeCapacity;
    }

    public void setWriteCapacity(Long writeCapacity) {
        this.writeCapacity = writeCapacity;
    }

    /**
     * The maximum number of reads per second for this table before an exception is thrown. Required if ``billing-mode`` is set to ``PROVISIONED``.
     */
    @Updatable
    public Long getReadCapacity() {
        return readCapacity;
    }

    public void setReadCapacity(Long readCapacity) {
        this.readCapacity = readCapacity;
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
    public void copyFrom(GlobalSecondaryIndexDescription model) {
        setName(model.indexName());
        setReadCapacity(Optional.ofNullable(model.provisionedThroughput())
            .map(ProvisionedThroughputDescription::readCapacityUnits)
            .orElse(null));
        setWriteCapacity(Optional.ofNullable(model.provisionedThroughput())
            .map(ProvisionedThroughputDescription::writeCapacityUnits)
            .orElse(null));
        setProjectionType(Optional.ofNullable(model.projection())
            .map(Projection::projectionTypeAsString)
            .orElse(null));
        setNonKeyAttributes(Optional.ofNullable(model.projection())
            .map(Projection::nonKeyAttributes)
            .orElse(null));

        setHashKey(null);
        setRangeKey(null);
        if (model.hasKeySchema()) {
            model.keySchema().forEach(k -> {
                if (KeyType.HASH.equals(k.keyType())) {
                    setHashKey(k.attributeName());
                } else if (KeyType.RANGE.equals(k.keyType())) {
                    setRangeKey(k.attributeName());
                }
            });
        }
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    GlobalSecondaryIndex toGlobalSecondaryIndex() {
        GlobalSecondaryIndex.Builder builder = GlobalSecondaryIndex.builder()
            .indexName(getName())
            .keySchema(createKeySchemas());

        if (getReadCapacity() != null && getWriteCapacity() != null) {
            builder.provisionedThroughput(t -> t.readCapacityUnits(getReadCapacity())
                .writeCapacityUnits(getWriteCapacity()));
        }

        if (!getNonKeyAttributes().isEmpty()) {
            builder.projection(p -> p.projectionType(getProjectionType()).nonKeyAttributes(getNonKeyAttributes()));
        } else {
            builder.projection(p -> p.projectionType(getProjectionType()));
        }

        return builder.build();
    }

    CreateGlobalSecondaryIndexAction toCreateGlobalSecondaryIndex() {
        CreateGlobalSecondaryIndexAction.Builder builder = CreateGlobalSecondaryIndexAction.builder()
            .indexName(getName())
            .keySchema(createKeySchemas());

        if (getReadCapacity() != null && getWriteCapacity() != null) {
            builder.provisionedThroughput(t -> t.readCapacityUnits(getReadCapacity())
                .writeCapacityUnits(getWriteCapacity()));
        }

        if (!getNonKeyAttributes().isEmpty()) {
            builder.projection(p -> p.projectionType(getProjectionType()).nonKeyAttributes(getNonKeyAttributes()));
        } else {
            builder.projection(p -> p.projectionType(getProjectionType()));
        }

        return builder.build();
    }

    private List<KeySchemaElement> createKeySchemas() {
        List<KeySchemaElement> keySchemas = new ArrayList<>();
        keySchemas.add(KeySchemaElement.builder().attributeName(hashKey).keyType(KeyType.HASH).build());

        if (rangeKey != null) {
            keySchemas.add(KeySchemaElement.builder().attributeName(rangeKey).keyType(KeyType.RANGE).build());
        }

        return keySchemas;
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();
        DynamoDbTableResource parentTable = (DynamoDbTableResource) parentResource();

        if (!getNonKeyAttributes().isEmpty() && !"INCLUDE".equals(getProjectionType())) {
            errors.add(new ValidationError(
                this,
                "projection-type",
                "The 'projection-type' must be 'INCLUDE' when specifying 'non-key-attributes'!"));
        }

        if ("PROVISIONED".equals(parentTable.getBillingMode()) && (getReadCapacity() == null
            || getWriteCapacity() == null)) {
            errors.add(new ValidationError(
                this,
                null,
                "'read-capacity' and 'write-capacity' must both be provided when the table 'billing-mode' is set to 'PROVISIONED'!"));
        }

        if ("PAY_PER_REQUEST".equals(parentTable.getBillingMode()) && (getReadCapacity() != null
            || getWriteCapacity() != null)) {
            errors.add(new ValidationError(
                this,
                null,
                "'read-capacity' or 'write-capacity' cannot be provided when the table 'billing-mode' is set to 'PAY_PER_REQUEST'!"));
        }

        return errors;
    }
}
