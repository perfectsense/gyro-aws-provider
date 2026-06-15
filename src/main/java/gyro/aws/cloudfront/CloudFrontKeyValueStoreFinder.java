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

import gyro.aws.AwsFinder;
import gyro.core.Type;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.DescribeKeyValueStoreRequest;
import software.amazon.awssdk.services.cloudfront.model.ListKeyValueStoresRequest;
import software.amazon.awssdk.services.cloudfront.model.ListKeyValueStoresResponse;
import software.amazon.awssdk.services.cloudfront.model.KeyValueStore;
import software.amazon.awssdk.services.cloudfront.model.NoSuchResourceException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Type("cloudfront-key-value-store")
public class CloudFrontKeyValueStoreFinder extends AwsFinder<CloudFrontClient, KeyValueStore, CloudFrontKeyValueStoreResource> {

    private String name;

    /**
     * The name of the Key Value Store.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<KeyValueStore> findAllAws(CloudFrontClient client) {
        List<KeyValueStore> stores = new ArrayList<>();

        String marker = null;
        do {
            ListKeyValueStoresRequest.Builder builder = ListKeyValueStoresRequest.builder();
            if (marker != null) {
                builder.marker(marker);
            }
            ListKeyValueStoresResponse response = client.listKeyValueStores(builder.build());

            stores.addAll(response.keyValueStoreList().items());
            marker = response.keyValueStoreList().nextMarker();
        } while (marker != null);

        return stores;
    }

    @Override
    protected List<KeyValueStore> findAws(CloudFrontClient client, Map<String, String> filters) {
        try {
            return Arrays.asList(client.describeKeyValueStore(
                DescribeKeyValueStoreRequest.builder()
                    .name(filters.get("name"))
                    .build()
            ).keyValueStore());
        } catch (NoSuchResourceException ex) {
            return Collections.emptyList();
        }
    }
}
