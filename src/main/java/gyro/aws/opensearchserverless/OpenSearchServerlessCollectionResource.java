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

package gyro.aws.opensearchserverless;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.TimeoutSettings;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.opensearchserverless.OpenSearchServerlessClient;
import software.amazon.awssdk.services.opensearchserverless.model.BatchGetCollectionResponse;
import software.amazon.awssdk.services.opensearchserverless.model.CollectionDetail;
import software.amazon.awssdk.services.opensearchserverless.model.CollectionStatus;
import software.amazon.awssdk.services.opensearchserverless.model.CollectionType;
import software.amazon.awssdk.services.opensearchserverless.model.CreateCollectionResponse;
import software.amazon.awssdk.services.opensearchserverless.model.ResourceNotFoundException;
import software.amazon.awssdk.services.opensearchserverless.model.StandbyReplicas;
import software.amazon.awssdk.services.opensearchserverless.model.Tag;

/**
 * Create an OpenSearch Serverless collection.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::opensearch-serverless-collection collection-example
 *         name: "collection-example"
 *         description: "Collection example"
 *         type: "SEARCH"
 *         standby-replicas: "ENABLED"
 *         tags: {
 *             Name: "collection-example"
 *         }
 *     end
 */
@Type("opensearch-serverless-collection")
public class OpenSearchServerlessCollectionResource extends AwsResource implements Copyable<CollectionDetail> {

    private String name;
    private String description;
    private CollectionType type;
    private StandbyReplicas standbyReplicas;
    private Map<String, String> tags;

    //output
    private String arn;
    private String id;

    /**
     * The name of the collection.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The description of the collection.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The type of the collection.
     */
    @Required
    @ValidStrings({ "SEARCH", "TIMESERIES", "VECTORSEARCH" })
    public CollectionType getType() {
        return type;
    }

    public void setType(CollectionType type) {
        this.type = type;
    }

    /**
     * Should the collection have standby replicas.
     */
    @Required
    @ValidStrings({ "ENABLED", "DISABLED" })
    public StandbyReplicas getStandbyReplicas() {
        return standbyReplicas;
    }

    public void setStandbyReplicas(StandbyReplicas standbyReplicas) {
        this.standbyReplicas = standbyReplicas;
    }

    /**
     * The tags attached to the collection.
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
     * The ARN of the collection.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The ID of the collection.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void copyFrom(CollectionDetail model) {
        setArn(model.arn());
        setId(model.id());
        setName(model.name());
        setDescription(model.description());
        setType(model.type());
        setStandbyReplicas(model.standbyReplicas());

        getTags().clear();
        try (OpenSearchServerlessClient client = createClient(OpenSearchServerlessClient.class)) {
            client.listTagsForResource(r -> r.resourceArn(model.arn()))
                .tags().forEach(t -> getTags().put(t.key(), t.value()));
        }
    }

    @Override
    public boolean refresh() {
        try (OpenSearchServerlessClient client = createClient(OpenSearchServerlessClient.class)) {
            CollectionDetail collectionDetail = getOpenSearchServerlessCollection(client);

            if (collectionDetail == null) {
                return false;
            }

            copyFrom(collectionDetail);

            return true;
        }
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        try (OpenSearchServerlessClient client = createClient(OpenSearchServerlessClient.class)) {
            String token = UUID.randomUUID().toString();

            CreateCollectionResponse collection = client.createCollection(r -> r.name(getName())
                .clientToken(token)
                .description(getDescription())
                .standbyReplicas(getStandbyReplicas())
                .tags(getTags().entrySet().stream()
                    .map(e -> Tag.builder()
                        .key(e.getKey())
                        .value(e.getValue())
                        .build())
                    .collect(Collectors.toList()))
                .type(getType())
            );

            setId(collection.createCollectionDetail().id());

            Wait.atMost(20, TimeUnit.MINUTES)
                .checkEvery(4, TimeUnit.MINUTES)
                .resourceOverrides(this, TimeoutSettings.Action.CREATE)
                .prompt(false)
                .until(() -> {
                    CollectionDetail collectionDetail = getOpenSearchServerlessCollection(client);
                    return collectionDetail != null && collectionDetail.status().equals(CollectionStatus.ACTIVE);
                });
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        try (OpenSearchServerlessClient client = createClient(OpenSearchServerlessClient.class)) {
            if (changedFieldNames.contains("description")) {
                String token = UUID.randomUUID().toString();
                client.updateCollection(r -> r.id(getId())
                    .clientToken(token)
                    .description(getDescription())
                );
            }

            if (changedFieldNames.contains("tags")) {
                OpenSearchServerlessCollectionResource old = (OpenSearchServerlessCollectionResource) current;

                if (!old.getTags().isEmpty()) {
                    client.untagResource(r -> r.resourceArn(getArn())
                        .tagKeys(old.getTags().keySet())
                    );
                }

                if (!getTags().isEmpty()) {
                    client.tagResource(r -> r.resourceArn(getArn())
                        .tags(getTags().entrySet().stream()
                            .map(e -> Tag.builder()
                                .key(e.getKey())
                                .value(e.getValue())
                                .build())
                            .collect(Collectors.toList()))
                    );
                }
            }

            Wait.atMost(20, TimeUnit.MINUTES)
                .checkEvery(4, TimeUnit.MINUTES)
                .resourceOverrides(this, TimeoutSettings.Action.UPDATE)
                .prompt(false)
                .until(() -> {
                    CollectionDetail collectionDetail = getOpenSearchServerlessCollection(client);
                    return collectionDetail != null && collectionDetail.status().equals(CollectionStatus.ACTIVE);
                });
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        try (OpenSearchServerlessClient client = createClient(OpenSearchServerlessClient.class)) {
            client.deleteCollection(r -> r.id(getId()));

            Wait.atMost(20, TimeUnit.MINUTES)
                .checkEvery(4, TimeUnit.MINUTES)
                .resourceOverrides(this, TimeoutSettings.Action.DELETE)
                .prompt(false)
                .until(() -> getOpenSearchServerlessCollection(client) == null);

        } catch (ResourceNotFoundException ex) {
            // ignore
        }
    }

    private CollectionDetail getOpenSearchServerlessCollection(OpenSearchServerlessClient client) {
        CollectionDetail collectionDetail = null;

        try {
            BatchGetCollectionResponse response = client.batchGetCollection(r -> r.ids(getId()));

            if (!response.collectionDetails().isEmpty()) {
                collectionDetail = response.collectionDetails().get(0);
            }

        } catch (ResourceNotFoundException ex) {
            // ignore
        }

        return collectionDetail;
    }
}
