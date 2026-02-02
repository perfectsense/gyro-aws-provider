/*
 * Copyright 2026, Brightspot.
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

package gyro.aws.cloudfront;

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
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;

import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.CreateKeyValueStoreRequest;
import software.amazon.awssdk.services.cloudfront.model.CreateKeyValueStoreResponse;
import software.amazon.awssdk.services.cloudfront.model.DescribeKeyValueStoreRequest;
import software.amazon.awssdk.services.cloudfront.model.DescribeKeyValueStoreResponse;
import software.amazon.awssdk.services.cloudfront.model.KeyValueStore;
import software.amazon.awssdk.services.cloudfront.model.UpdateKeyValueStoreRequest;
import software.amazon.awssdk.services.cloudfront.model.DeleteKeyValueStoreRequest;
import software.amazon.awssdk.services.cloudfront.model.NoSuchResourceException;

import software.amazon.awssdk.services.cloudfront.model.UpdateKeyValueStoreResponse;
import software.amazon.awssdk.services.cloudfrontkeyvaluestore.CloudFrontKeyValueStoreClient;
import software.amazon.awssdk.services.cloudfrontkeyvaluestore.model.DeleteKeyRequestListItem;
import software.amazon.awssdk.services.cloudfrontkeyvaluestore.model.GetKeyResponse;
import software.amazon.awssdk.services.cloudfrontkeyvaluestore.model.ListKeysResponseListItem;
import software.amazon.awssdk.services.cloudfrontkeyvaluestore.model.PutKeyRequest;
import software.amazon.awssdk.services.cloudfrontkeyvaluestore.model.ListKeysRequest;
import software.amazon.awssdk.services.cloudfrontkeyvaluestore.model.ListKeysResponse;
import software.amazon.awssdk.services.cloudfrontkeyvaluestore.model.PutKeyRequestListItem;
import software.amazon.awssdk.services.cloudfrontkeyvaluestore.model.PutKeyResponse;
import software.amazon.awssdk.services.cloudfrontkeyvaluestore.model.UpdateKeysResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Creates a CloudFront KeyValueStore.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::cloudfront-key-value-store kvs-example
 *         name: "kvs-example"
 *         comment: "Sample KV Store"
 *
 *         key-values: {
 *             "key-1": "true",
 *             "foo": "bar"
 *         }
 *     end
 *     aws::cloudfront-key-value-store kvs-example-2
 *         name: "kvs-example-2"
 *         comment: "Sample KV Store with s3"
 *
 *         import-source
 *            source-type: "S3"
 *            source-arn: "arn:aws:s3:::my-bucket/kvs-data.json"
 *         end
 *     end
 */
@Type("cloudfront-key-value-store")
public class CloudFrontKeyValueStoreResource extends AwsResource implements Copyable<KeyValueStore> {

    private String name;
    private String comment;
    private Map<String, String> keyValues;
    private CloudFrontKeyValueStoreImportSource importSource;

    // Read-only
    private String id;
    private String arn;
    private String status;
    // ETag for CloudFront KeyValueStore configuration (name/comment/status)
    private String etag;
    // ETag for CloudFrontKeyValueStore key/value contents
    private String kvsEtag;

    /**
     * The name of the key value store.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * A comment describing the key value store.
     */
    @Updatable
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * The key-value pairs stored in this KeyValueStore.
     */
    @Updatable
    @ConflictsWith("import-source")
    public Map<String, String> getKeyValues() {
        if (keyValues == null) {
            keyValues = new HashMap<>();
        }
        return keyValues;
    }

    public void setKeyValues(Map<String, String> keyValues) {
        this.keyValues = keyValues;
    }

    /**
     * The S3 import source for initially populating the key value store.
     * This can be specified only during creation. The S3 bucket must contain a valid JSON file.
     *
     * @subresource gyro.aws.cloudfront.CloudFrontKeyValueStoreImportSource
     */
    @ConflictsWith("key-values")
    public CloudFrontKeyValueStoreImportSource getImportSource() {
        return importSource;
    }

    public void setImportSource(CloudFrontKeyValueStoreImportSource importSource) {
        this.importSource = importSource;
    }

    /**
     * The unique ID of the key value store.
     */
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The Amazon Resource Name (ARN) of the key value store.
     */
    @Id
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The status of the key value store.
     */
    @Output
    @ValidStrings({"PROVISIONING", "READY", "DELETING"})
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * The current ETag/version identifier of the key value store.
     */
    @Output
    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    /**
     * The current ETag/version identifier of the key value store contents.
     */
    @Output
    public String getKvsEtag() {
        return kvsEtag;
    }

    public void setKvsEtag(String kvsEtag) {
        this.kvsEtag = kvsEtag;
    }

    @Override
    public void copyFrom(KeyValueStore model) {
        setName(model.name());
        setComment(model.comment());
        setId(model.id());
        setArn(model.arn());
        setStatus(model.status());

        CloudFrontKeyValueStoreClient kvsClient = createClient(CloudFrontKeyValueStoreClient.class);
        Map<String, String> kvs = new HashMap<>();

        String nextToken = null;
        do {
            ListKeysResponse listResponse = kvsClient.listKeys(
                ListKeysRequest.builder()
                    .kvsARN(getArn())
                    .nextToken(nextToken)
                    .build()
            );

            for (ListKeysResponseListItem keyItem : listResponse.items()) {
                GetKeyResponse getResponse = kvsClient.getKey(r -> r
                    .kvsARN(getArn())
                    .key(keyItem.key())
                );
                kvs.put(keyItem.key(), getResponse.value());
            }

            nextToken = listResponse.nextToken();
        } while (nextToken != null);

        setKeyValues(kvs);
    }

    @Override
    public boolean refresh() {
        CloudFrontClient client = createClient(CloudFrontClient.class);

        try {
            DescribeKeyValueStoreResponse response = client.describeKeyValueStore(
                DescribeKeyValueStoreRequest.builder()
                    .name(getName())
                    .build()
            );

            KeyValueStore kvs = response.keyValueStore();
            copyFrom(kvs);
            setEtag(response.eTag());
            CloudFrontKeyValueStoreClient kvsClient = createClient(CloudFrontKeyValueStoreClient.class);
            setKvsEtag(getKeyValueStoreETag(kvsClient));

            return true;
        } catch (NoSuchResourceException ex) {
            return false;
        }
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        CloudFrontClient client = createClient(CloudFrontClient.class);

        CreateKeyValueStoreRequest.Builder requestBuilder = CreateKeyValueStoreRequest.builder()
            .name(getName())
            .comment(getComment());

        if (getImportSource() != null) {
            requestBuilder.importSource(getImportSource().toImportSource());
        }

        CreateKeyValueStoreResponse response = client.createKeyValueStore(requestBuilder.build());

        KeyValueStore kvs = response.keyValueStore();
        setId(kvs.id());
        setArn(kvs.arn());
        setStatus(kvs.status());
        setEtag(response.eTag());

        waitForAvailability(client, TimeoutSettings.Action.CREATE);

        CloudFrontKeyValueStoreClient kvsClient = createClient(CloudFrontKeyValueStoreClient.class);
        setKvsEtag(getKeyValueStoreETag(kvsClient));

        state.save();

        // Create key-value pairs
        if (!getKeyValues().isEmpty()) {
            for (Map.Entry<String, String> entry : getKeyValues().entrySet()) {
                PutKeyResponse putResponse = kvsClient.putKey(
                    PutKeyRequest.builder()
                        .kvsARN(getArn())
                        .key(entry.getKey())
                        .value(entry.getValue())
                        .ifMatch(getKvsEtag())
                        .build()
                );

                setKvsEtag(putResponse.eTag());
            }
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties)
        throws Exception {
        CloudFrontClient client = createClient(CloudFrontClient.class);

        if (changedProperties.contains("comment")) {
            UpdateKeyValueStoreResponse response = client.updateKeyValueStore(
                UpdateKeyValueStoreRequest.builder()
                    .name(getName())
                    .comment(getComment())
                    .ifMatch(getEtag())
                    .build()
            );
            setEtag(response.eTag());
        }

        if (changedProperties.contains("key-values")) {
            CloudFrontKeyValueStoreResource currentResource = (CloudFrontKeyValueStoreResource) current;
            CloudFrontKeyValueStoreClient kvsClient = createClient(CloudFrontKeyValueStoreClient.class);
            String kvsEtag = currentResource.getKvsEtag();

            Map<String, String> currentKeyValues = currentResource.getKeyValues();
            Map<String, String> newKeyValues = getKeyValues();

            Set<String> deleteKeys = currentKeyValues.keySet();
            deleteKeys.removeAll(newKeyValues.keySet());
            UpdateKeysResponse updateResponse = kvsClient.updateKeys(r -> r.puts(newKeyValues.entrySet().stream()
                    .map(k -> PutKeyRequestListItem.builder().key(k.getKey()).value(k.getValue()).build())
                    .collect(Collectors.toSet()))
                .deletes(deleteKeys.stream().map(k -> DeleteKeyRequestListItem.builder().key(k).build())
                    .collect(Collectors.toSet()))
                .ifMatch(kvsEtag)
                .kvsARN(getArn()));
            setKvsEtag(updateResponse.eTag());
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        CloudFrontClient client = createClient(CloudFrontClient.class);

        client.deleteKeyValueStore(
            DeleteKeyValueStoreRequest.builder()
                .name(getName())
                .ifMatch(getEtag())
                .build()
        );
    }

    private void waitForAvailability(CloudFrontClient client, TimeoutSettings.Action action) {
        Wait.atMost(5, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .resourceOverrides(this, action)
            .prompt(false)
            .until(() -> {
                try {
                    DescribeKeyValueStoreResponse response = client.describeKeyValueStore(
                        DescribeKeyValueStoreRequest.builder()
                            .name(getName())
                            .build()
                    );

                    KeyValueStore kvs = response.keyValueStore();
                    setEtag(response.eTag());
                    return kvs != null && "READY".equals(kvs.status());
                } catch (NoSuchResourceException ex) {
                    return false;
                }
            });
    }

    private String getKeyValueStoreETag(CloudFrontKeyValueStoreClient kvsClient) {
        software.amazon.awssdk.services.cloudfrontkeyvaluestore.model.DescribeKeyValueStoreResponse response =
            kvsClient.describeKeyValueStore(
                software.amazon.awssdk.services.cloudfrontkeyvaluestore.model.DescribeKeyValueStoreRequest.builder()
                    .kvsARN(getArn())
                    .build()
            );
        return response.eTag();
    }
}
