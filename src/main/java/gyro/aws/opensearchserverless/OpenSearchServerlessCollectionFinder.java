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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.opensearchserverless.OpenSearchServerlessClient;
import software.amazon.awssdk.services.opensearchserverless.model.BatchGetCollectionResponse;
import software.amazon.awssdk.services.opensearchserverless.model.CollectionDetail;
import software.amazon.awssdk.services.opensearchserverless.model.CollectionSummary;
import software.amazon.awssdk.services.opensearchserverless.model.ListCollectionsResponse;
import software.amazon.awssdk.services.opensearchserverless.model.ResourceNotFoundException;
import software.amazon.awssdk.utils.builder.SdkBuilder;

/**
 * Query OpenSearch Serverless collection.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    collection: $(external-query aws::opensearch-serverless-collection { collection-id: ''})
 */
@Type("opensearch-serverless-collection")
public class OpenSearchServerlessCollectionFinder
    extends AwsFinder<OpenSearchServerlessClient, CollectionDetail, OpenSearchServerlessCollectionResource> {

    private String collectionId;
    private String name;

    /**
     * The ID of the collection.
     */
    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    /**
     * The name of the collection.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<CollectionDetail> findAllAws(OpenSearchServerlessClient client) {
        List<CollectionDetail> collectionDetails = new ArrayList<>();
        ListCollectionsResponse response = client.listCollections(SdkBuilder::build);
        List<String> collectionIds = response.collectionSummaries()
            .stream()
            .map(CollectionSummary::id)
            .collect(Collectors.toList());

        if (!collectionIds.isEmpty()) {
            BatchGetCollectionResponse batchGetCollectionResponse = client.batchGetCollection(r -> r.ids(collectionIds));
            collectionDetails = batchGetCollectionResponse.collectionDetails();
        }

        return collectionDetails;
    }

    @Override
    protected List<CollectionDetail> findAws(OpenSearchServerlessClient client, Map<String, String> filters) {
        List<CollectionDetail> collectionDetails = new ArrayList<>();
        try {
            if (filters.containsKey("collection-id")) {
                collectionDetails = client.batchGetCollection(r -> r.ids(filters.get("collection-id")))
                    .collectionDetails();
            } else if (filters.containsKey("name")) {
                collectionDetails = client.batchGetCollection(r -> r.names(filters.get("name"))).collectionDetails();
            }
        } catch (ResourceNotFoundException ex) {
            // Ignore
        }
        return collectionDetails;
    }
}
