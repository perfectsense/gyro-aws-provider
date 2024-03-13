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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.TimeoutSettings;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Min;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.BillingModeSummary;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndexUpdate;
import software.amazon.awssdk.services.dynamodb.model.IndexStatus;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ListTagsOfResourceResponse;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughputDescription;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.SSESpecification;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.services.dynamodb.model.TableStatus;
import software.amazon.awssdk.services.dynamodb.model.Tag;
import software.amazon.awssdk.services.dynamodb.model.UpdateTableRequest;

/**
 * Creates a DynamoDb table.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::dynamodb-table dynamodb-table-example
 *         name: "dynamodb-table-example"
 *         hash-key: "HashKeyName"
 *         range-key: "RangeKeyName"
 *         billing-mode: "PROVISIONED"
 *         read-capacity: 5
 *         write-capacity: 5
 *         stream-enabled: true
 *         stream-view-type: "KEYS_ONLY"
 *
 *         attribute
 *             name: "HashKeyName"
 *             type: "S"
 *         end
 *
 *         attribute
 *             name: "RangeKeyName"
 *             type: "S"
 *         end
 *
 *         attribute
 *             name: "GlobalRangeKeyName"
 *             type: "S"
 *         end
 *
 *         attribute
 *             name: "LocalRangeKeyName"
 *             type: "S"
 *         end
 *
 *         global-secondary-index
 *             name: "global-secondary-index"
 *             hash-key: "RangeKeyName"
 *             range-key: "GlobalRangeKeyName"
 *             write-capacity: 20
 *             read-capacity: 20
 *             projection-type: "INCLUDE"
 *             non-key-attributes: ["HashKeyName"]
 *         end
 *
 *         local-secondary-index
 *             name: "local-secondary-index"
 *             range-key: "LocalRangeKeyName"
 *             projection-type: "ALL"
 *         end
 *
 *         server-side-encryption
 *             enabled: true
 *         end
 *
 *         tags: {
 *             Name: "dynamodb-table-example"
 *         }
 *     end
 */
@Type("dynamodb-table")
public class DynamoDbTableResource extends AwsResource implements Copyable<TableDescription> {

    private Set<DynamoDbAttributeDefinition> attribute;
    private String hashKey;
    private String rangeKey;
    private String name;
    private String billingMode;
    private Set<DynamoDbGlobalSecondaryIndex> globalSecondaryIndex;
    private Set<DynamoDbLocalSecondaryIndex> localSecondaryIndex;
    private Long writeCapacity;
    private Long readCapacity;
    private DynamoDbServerSideEncryption serverSideEncryption;
    private Boolean streamEnabled;
    private String streamViewType;
    private Map<String, String> tags;

    // Read-only
    private String arn;
    private String streamArn;
    private String streamLabel;

    /**
     * Set of attribute definitions that describe the key schema.
     *
     * @subresource gyro.aws.dynamodb.DynamoDbAttributeDefinition
     */
    @Required
    @Updatable
    public Set<DynamoDbAttributeDefinition> getAttribute() {
        if (attribute == null) {
            attribute = new HashSet<>();
        }
        return attribute;
    }

    public void setAttribute(Set<DynamoDbAttributeDefinition> attribute) {
        this.attribute = attribute;
    }

    /**
     * The name of the hash (partition) key for this index. Must be defined as an ``attribute``.
     */
    @Required
    public String getHashKey() {
        return hashKey;
    }

    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }

    /**
     * The name of the range (sort) key for this index. Must be defined as an ``attribute``.
     */
    public String getRangeKey() {
        return rangeKey;
    }

    public void setRangeKey(String rangeKey) {
        this.rangeKey = rangeKey;
    }

    /**
     * The name of the DynamoDb table.
     */
    @Id
    @Required
    @Regex("[a-zA-Z0-9_.-]+")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Controls how you are charged for read and write throughput and how you manage capacity. Defaults to ``PROVISIONED``.
     */
    @Updatable
    @ValidStrings({ "PROVISIONED", "PAY_PER_REQUEST" })
    public String getBillingMode() {
        return billingMode != null ? billingMode : "PROVISIONED";
    }

    public void setBillingMode(String billingMode) {
        this.billingMode = billingMode;
    }

    /**
     * One or more global secondary indexes to be created on the table.
     *
     * @subresource gyro.aws.dynamodb.DynamoDbGlobalSecondaryIndex
     */
    @Updatable
    public Set<DynamoDbGlobalSecondaryIndex> getGlobalSecondaryIndex() {
        if (globalSecondaryIndex == null) {
            globalSecondaryIndex = new HashSet<>();
        }
        return globalSecondaryIndex;
    }

    public void setGlobalSecondaryIndex(Set<DynamoDbGlobalSecondaryIndex> globalSecondaryIndex) {
        this.globalSecondaryIndex = globalSecondaryIndex;
    }

    /**
     * One or more local secondary indexes to be created on the table. LSIs automatically use the same hash key of the table itself.
     *
     * @subresource gyro.aws.dynamodb.DynamoDbLocalSecondaryIndex
     */
    public Set<DynamoDbLocalSecondaryIndex> getLocalSecondaryIndex() {
        if (localSecondaryIndex == null) {
            localSecondaryIndex = new HashSet<>();
        }
        return localSecondaryIndex;
    }

    public void setLocalSecondaryIndex(Set<DynamoDbLocalSecondaryIndex> localSecondaryIndex) {
        this.localSecondaryIndex = localSecondaryIndex;
    }

    /**
     * The maximum number of writes per second for this table before an exception is thrown. Required if ``billing-mode`` is set to ``PROVISIONED``.
     */
    @Updatable
    @Min(1)
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
    @Min(1)
    public Long getReadCapacity() {
        return readCapacity;
    }

    public void setReadCapacity(Long readCapacity) {
        this.readCapacity = readCapacity;
    }

    /**
     * Settings used to determine server side encryption.
     *
     * @subresource gyro.aws.dynamodb.DynamoDbServerSideEncryption
     */
    @Updatable
    public DynamoDbServerSideEncryption getServerSideEncryption() {
        return serverSideEncryption;
    }

    public void setServerSideEncryption(DynamoDbServerSideEncryption serverSideEncryption) {
        this.serverSideEncryption = serverSideEncryption;
    }

    /**
     * Indicates whether streams should be enabled.
     */
    @Updatable
    public Boolean getStreamEnabled() {
        if (streamEnabled == null) {
            streamEnabled = false;
        }

        return streamEnabled;
    }

    public void setStreamEnabled(Boolean streamEnabled) {
        this.streamEnabled = streamEnabled;
    }

    /**
     * Determines what information is written to the stream when an item is modified.
     */
    @Updatable
    @ValidStrings({ "NEW_IMAGE", "OLD_IMAGE", "NEW_AND_OLD_IMAGES", "KEYS_ONLY" })
    public String getStreamViewType() {
        return streamViewType;
    }

    public void setStreamViewType(String streamViewType) {
        this.streamViewType = streamViewType;
    }

    /**
     * The tags for the DynamoDb table.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The ARN of the DynamoDb table.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The ARN of the table stream.
     */
    @Output
    public String getStreamArn() {
        return streamArn;
    }

    public void setStreamArn(String streamArn) {
        this.streamArn = streamArn;
    }

    /**
     * A timestamp, in ISO 8601 format, for the table stream.
     */
    @Output
    public String getStreamLabel() {
        return streamLabel;
    }

    public void setStreamLabel(String streamLabel) {
        this.streamLabel = streamLabel;
    }

    @Override
    public void copyFrom(TableDescription model) {
        setName(model.tableName());
        setArn(model.tableArn());
        setStreamArn(model.latestStreamArn());
        setStreamLabel(model.latestStreamLabel());
        setBillingMode(Optional.ofNullable(model.billingModeSummary())
            .map(BillingModeSummary::billingModeAsString)
            .orElse(null));
        setWriteCapacity(Optional.ofNullable(model.provisionedThroughput())
            .map(ProvisionedThroughputDescription::writeCapacityUnits)
            .orElse(null));
        setReadCapacity(Optional.ofNullable(model.provisionedThroughput())
            .map(ProvisionedThroughputDescription::readCapacityUnits)
            .orElse(null));

        setAttribute(null);
        if (model.hasAttributeDefinitions()) {
            setAttribute(model.attributeDefinitions().stream().map(a -> {
                DynamoDbAttributeDefinition attributeDefinition = newSubresource(DynamoDbAttributeDefinition.class);
                attributeDefinition.copyFrom(a);
                return attributeDefinition;
            }).collect(Collectors.toSet()));
        }

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

        setGlobalSecondaryIndex(null);
        if (model.hasGlobalSecondaryIndexes()) {
            setGlobalSecondaryIndex(model.globalSecondaryIndexes().stream().map(a -> {
                DynamoDbGlobalSecondaryIndex globalSecondaryIndex = newSubresource(DynamoDbGlobalSecondaryIndex.class);
                globalSecondaryIndex.copyFrom(a);
                return globalSecondaryIndex;
            }).collect(Collectors.toSet()));
        }

        setLocalSecondaryIndex(null);
        if (model.hasLocalSecondaryIndexes()) {
            setLocalSecondaryIndex(model.localSecondaryIndexes().stream().map(a -> {
                DynamoDbLocalSecondaryIndex localSecondaryIndex = newSubresource(DynamoDbLocalSecondaryIndex.class);
                localSecondaryIndex.copyFrom(a);
                return localSecondaryIndex;
            }).collect(Collectors.toSet()));
        }

        DynamoDbServerSideEncryption serverSideEncryption = newSubresource(DynamoDbServerSideEncryption.class);
        serverSideEncryption.copyFrom(model.sseDescription());
        setServerSideEncryption(serverSideEncryption);

        setStreamEnabled(null);
        setStreamViewType(null);
        if (model.streamSpecification() != null) {
            setStreamEnabled(model.streamSpecification().streamEnabled());
            setStreamViewType(model.streamSpecification().streamViewTypeAsString());
        }

        setTags(getDynamoDbTags());
    }

    @Override
    public boolean refresh() {
        DynamoDbClient client = createClient(DynamoDbClient.class);

        TableDescription table = getTable(client);

        if (table == null) {
            return false;
        }

        copyFrom(table);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        DynamoDbClient client = createClient(DynamoDbClient.class);
        CreateTableRequest.Builder builder = CreateTableRequest.builder()
            .tableName(getName())
            .keySchema(createKeySchemas())
            .billingMode(getBillingMode())
            .attributeDefinitions(getAttribute().stream()
                .map(DynamoDbAttributeDefinition::toAttributeDefinition)
                .collect(Collectors.toSet()))
            .sseSpecification(Optional.ofNullable(getServerSideEncryption())
                .map(DynamoDbServerSideEncryption::toSSESpecification)
                .orElse(null))
            .tags(createTags(getTags()));

        if (getReadCapacity() != null && getWriteCapacity() != null) {
            builder.provisionedThroughput(t -> t.readCapacityUnits(getReadCapacity())
                .writeCapacityUnits(getWriteCapacity()));
        }

        if (getStreamEnabled() != null) {
            builder.streamSpecification(s -> s.streamEnabled(getStreamEnabled()).streamViewType(getStreamViewType()));
        }

        if (!getGlobalSecondaryIndex().isEmpty()) {
            builder.globalSecondaryIndexes(getGlobalSecondaryIndex().stream()
                .map(DynamoDbGlobalSecondaryIndex::toGlobalSecondaryIndex)
                .collect(Collectors.toSet()));
        }

        if (!getLocalSecondaryIndex().isEmpty()) {
            builder.localSecondaryIndexes(getLocalSecondaryIndex().stream()
                .map(DynamoDbLocalSecondaryIndex::toLocalSecondaryIndex)
                .collect(Collectors.toSet()));
        }

        CreateTableResponse table = client.createTable(builder.build());

        waitForActive(5, 1, () -> tableActive(client), "ACTIVE", TimeoutSettings.Action.CREATE);

        copyFrom(table.tableDescription());
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        DynamoDbClient client = createClient(DynamoDbClient.class);

        Map<String, Set<DynamoDbGlobalSecondaryIndex>> diffGsiChanges = diffGsiChanges(
            ((DynamoDbTableResource) current).getGlobalSecondaryIndex(),
            getGlobalSecondaryIndex());
        Set<DynamoDbGlobalSecondaryIndex> deleteGsi = diffGsiChanges.get("delete");
        Set<DynamoDbGlobalSecondaryIndex> updateGsi = diffGsiChanges.get("update");
        Set<DynamoDbGlobalSecondaryIndex> addGsi = diffGsiChanges.get("add");

        for (DynamoDbGlobalSecondaryIndex delete : deleteGsi) {
            client.updateTable(r -> r.tableName(getName())
                .globalSecondaryIndexUpdates(GlobalSecondaryIndexUpdate.builder()
                    .delete(d -> d.indexName(delete.getName())).build()));
            state.save();

            waitForActive(5, 1, () -> gsiDeleted(client, delete.getName()), "INDEX DELETED",
                TimeoutSettings.Action.UPDATE);
        }

        UpdateTableRequest.Builder builder = UpdateTableRequest.builder().tableName(getName());
        boolean hasUpdate = false;
        boolean hasGsiUpdate = false;

        if (changedFieldNames.contains("billing-mode")) {
            builder.billingMode(getBillingMode());
            hasUpdate = true;
        }

        if ("PROVISIONED".equals(getBillingMode())
            && (changedFieldNames.contains("read-capacity") || changedFieldNames.contains("write-capacity"))) {
            builder.provisionedThroughput(t -> t.readCapacityUnits(getReadCapacity())
                .writeCapacityUnits(getWriteCapacity()));
            hasUpdate = true;
        }

        if (changedFieldNames.contains("stream-enabled") || changedFieldNames.contains("stream-view-type")) {
            builder.streamSpecification(s -> s.streamEnabled(getStreamEnabled()).streamViewType(getStreamViewType()));
            hasUpdate = true;
        }

        if ("PROVISIONED".equals(getBillingMode()) && !updateGsi.isEmpty()) {
            List<GlobalSecondaryIndexUpdate> updates = updateGsi.stream().map(g -> GlobalSecondaryIndexUpdate.builder()
                .update(d -> d.indexName(g.getName())
                    .provisionedThroughput(p -> p.readCapacityUnits(g.getReadCapacity())
                        .writeCapacityUnits(g.getWriteCapacity()))).build())
                .collect(Collectors.toList());
            builder.globalSecondaryIndexUpdates(updates);
            hasGsiUpdate = true;
        }

        if (hasUpdate || hasGsiUpdate) {
            client.updateTable(builder.build());
            state.save();

            waitForActive(30, 30, () -> tableActive(client), "ACTIVE", TimeoutSettings.Action.UPDATE);
            if (hasGsiUpdate) {
                waitForActive(30, 10, () -> allGsiActive(client), "INDEX ACTIVE", TimeoutSettings.Action.UPDATE);
            }
        }

        for (DynamoDbGlobalSecondaryIndex add : addGsi) {
            client.updateTable(r -> r.tableName(getName())
                .attributeDefinitions(getAttribute().stream()
                    .map(DynamoDbAttributeDefinition::toAttributeDefinition)
                    .collect(Collectors.toSet()))
                .globalSecondaryIndexUpdates(GlobalSecondaryIndexUpdate.builder()
                    .create(add.toCreateGlobalSecondaryIndex()).build()));
            state.save();

            waitForActive(30, 10, () -> allGsiActive(client), "INDEX ACTIVE", TimeoutSettings.Action.UPDATE);
        }

        if (changedFieldNames.contains("server-side-encryption")) {
            client.updateTable(r -> r.tableName(getName())
                .sseSpecification(Optional.ofNullable(getServerSideEncryption())
                    .map(DynamoDbServerSideEncryption::toSSESpecification)
                    .orElse(SSESpecification.builder().enabled(false).build())));
        }

        if (changedFieldNames.contains("tags")) {
            updateTags(client);
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        DynamoDbClient client = createClient(DynamoDbClient.class);
        client.deleteTable(r -> r.tableName(getName()));
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if ("PROVISIONED".equals(getBillingMode()) && (getReadCapacity() == null || getWriteCapacity() == null)) {
            errors.add(new ValidationError(
                this,
                null,
                "'read-capacity' and 'write-capacity' must both be provided when 'billing-mode' is set to 'PROVISIONED'!"));
        }

        if ("PAY_PER_REQUEST".equals(getBillingMode()) && ((getReadCapacity() != null && getReadCapacity() > 0) || (getWriteCapacity() != null && getWriteCapacity() > 0))) {
            errors.add(new ValidationError(
                this,
                null,
                "'read-capacity' or 'write-capacity' cannot be provided when 'billing-mode' is set to 'PAY_PER_REQUEST'!"));
        }

        if (getHashKey() != null && getAttribute().stream().noneMatch(a -> getHashKey().equals(a.getName()))) {
            errors.add(new ValidationError(
                this,
                "hash-key",
                "There must be an 'attribute' declared with the same name as the 'hash-key'!"));
        }

        if (getRangeKey() != null && getAttribute().stream().noneMatch(a -> getRangeKey().equals(a.getName()))) {
            errors.add(new ValidationError(
                this,
                "range-key",
                "There must be an 'attribute' declared with the same name as the 'range-key'!"));
        }

        if (!getStreamEnabled() && getStreamViewType() != null) {
            errors.add(new ValidationError(
                this,
                "stream-view-type",
                "'stream-view-type' can only be used when 'stream-enabled' is set to 'true'!"));
        }

        if (getStreamEnabled() && getStreamViewType() == null) {
            errors.add(new ValidationError(
                this,
                "stream-enabled",
                "'stream-view-type' is required when 'stream-enabled' is set to 'true'!"));
        }

        return errors;
    }

    public TableDescription getTable(DynamoDbClient client) {
        try {
            return client.describeTable(r -> r.tableName(getName())).table();
        } catch (ResourceNotFoundException ignore) {
            return null;
        }
    }

    private void waitForActive(
        long interval, long duration, BooleanSupplier waitCheck, String state,
        TimeoutSettings.Action action) {
        boolean waitResult = Wait.atMost(duration, TimeUnit.MINUTES)
            .checkEvery(interval, TimeUnit.SECONDS)
            .resourceOverrides(this, action)
            .prompt(false)
            .until(waitCheck::getAsBoolean);

        if (!waitResult) {
            throw new GyroException(String.format(
                "Unable to reach '%s' state for DynamoDb table - %s",
                state,
                getName()));
        }
    }

    private boolean tableActive(DynamoDbClient client) {
        TableDescription table = getTable(client);

        return table != null && TableStatus.ACTIVE.equals(table.tableStatus());
    }

    private boolean allGsiActive(DynamoDbClient client) {
        TableDescription table = getTable(client);

        return table != null && table.globalSecondaryIndexes().stream()
            .allMatch(g -> IndexStatus.ACTIVE.equals(g.indexStatus()));
    }

    private boolean gsiDeleted(DynamoDbClient client, String name) {
        TableDescription table = getTable(client);

        return table != null && table.globalSecondaryIndexes().stream().noneMatch(g -> name.equals(g.indexName()));
    }

    private List<KeySchemaElement> createKeySchemas() {
        List<KeySchemaElement> keySchemas = new ArrayList<>();
        keySchemas.add(KeySchemaElement.builder().attributeName(hashKey).keyType(KeyType.HASH).build());

        if (rangeKey != null) {
            keySchemas.add(KeySchemaElement.builder().attributeName(rangeKey).keyType(KeyType.RANGE).build());
        }

        return keySchemas;
    }

    private List<Tag> createTags(Map<String, String> tags) {
        return tags.entrySet()
            .stream()
            .map(t -> Tag.builder().key(t.getKey()).value(t.getValue()).build())
            .collect(Collectors.toList());
    }

    private Map<String, String> getDynamoDbTags() {
        DynamoDbClient client = createClient(DynamoDbClient.class);

        ListTagsOfResourceResponse response = client.listTagsOfResource(r -> r.resourceArn(getArn()));

        return response.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value));
    }

    private Map<String, Set<DynamoDbGlobalSecondaryIndex>> diffGsiChanges(
        Set<DynamoDbGlobalSecondaryIndex> currentGsiSet,
        Set<DynamoDbGlobalSecondaryIndex> pendingGsiSet) {
        Set<DynamoDbGlobalSecondaryIndex> deleteGsi = new HashSet<>();
        Set<DynamoDbGlobalSecondaryIndex> updateGsi = new HashSet<>();
        Set<DynamoDbGlobalSecondaryIndex> addGsi = new HashSet<>();

        for (DynamoDbGlobalSecondaryIndex currentGsi : currentGsiSet) {
            boolean delete = true;
            for (DynamoDbGlobalSecondaryIndex pendingGsi : pendingGsiSet) {
                if (currentGsi.getName().equals(pendingGsi.getName())) {
                    // We must delete then add GSIs with changed non-updateable fields
                    if (!Objects.equals(pendingGsi.getHashKey(), currentGsi.getHashKey())
                        || !Objects.equals(pendingGsi.getRangeKey(), currentGsi.getRangeKey())
                        || !Objects.equals(pendingGsi.getProjectionType(), currentGsi.getProjectionType())
                        || !Objects.equals(pendingGsi.getNonKeyAttributes(), currentGsi.getNonKeyAttributes())) {
                        deleteGsi.add(currentGsi);
                        addGsi.add(pendingGsi);
                    } else if (!Objects.equals(pendingGsi.getReadCapacity(), currentGsi.getReadCapacity())
                        || !Objects.equals(pendingGsi.getWriteCapacity(), currentGsi.getWriteCapacity())) {
                        // We can update the GSI if the read or write capacity has changed
                        updateGsi.add(pendingGsi);
                    }
                    delete = false;
                    break;
                }
            }

            // Delete all GSIs that are no longer in pending
            if (delete) {
                deleteGsi.add(currentGsi);
            }
        }

        // Add all GSIs that are only in pending
        addGsi.addAll(pendingGsiSet.stream()
            .filter(g -> currentGsiSet.stream().noneMatch(g2 -> g2.getName().equals(g.getName())))
            .collect(Collectors.toSet()));

        Map<String, Set<DynamoDbGlobalSecondaryIndex>> diff = new HashMap<>();
        diff.put("delete", deleteGsi);
        diff.put("update", updateGsi);
        diff.put("add", addGsi);

        return diff;
    }

    private void updateTags(DynamoDbClient client) {
        Map<String, String> pendingTags = getTags();
        Map<String, String> currentTags = getDynamoDbTags();

        MapDifference<String, String> diff = Maps.difference(currentTags, pendingTags);

        if (!diff.entriesOnlyOnLeft().isEmpty()) {
            client.untagResource(
                r -> r.resourceArn(getArn())
                    .tagKeys(diff.entriesOnlyOnLeft().keySet())
            );
        }

        if (!diff.entriesOnlyOnRight().isEmpty()) {
            client.tagResource(
                r -> r.resourceArn(getArn())
                    .tags(createTags(diff.entriesOnlyOnRight()))
            );
        }

        if (!diff.entriesDiffering().isEmpty()) {
            client.untagResource(
                r -> r.resourceArn(getArn())
                    .tagKeys(diff.entriesDiffering().keySet())
            );

            Map<String, String> addTags = new HashMap<>();
            diff.entriesDiffering().keySet().forEach(o -> addTags.put(o, diff.entriesDiffering().get(o).rightValue()));

            client.tagResource(
                r -> r.resourceArn(getArn())
                    .tags(createTags(addTags))
            );
        }
    }
}
