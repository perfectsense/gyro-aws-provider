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

package gyro.aws.cloudfront;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.GetPublicKeyRequest;
import software.amazon.awssdk.services.cloudfront.model.ListPublicKeysRequest;
import software.amazon.awssdk.services.cloudfront.model.NoSuchPublicKeyException;
import software.amazon.awssdk.services.cloudfront.model.PublicKey;
import software.amazon.awssdk.services.cloudfront.model.PublicKeyList;
import software.amazon.awssdk.services.cloudfront.model.PublicKeySummary;

/**
 * Query CloudFront public key.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    public-key: $(external-query aws::cloudfront-public-key { id: 'K2SZWDLAJKB3A' })
 */
@Type("cloudfront-public-key")
public class CloudFrontPublicKeyFinder extends AwsFinder<CloudFrontClient, PublicKey, CloudFrontPublicKeyResource> {

    private String id;

    /**
     * The ID of the public key.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<PublicKey> findAllAws(CloudFrontClient client) {
        List<PublicKey> publicKeys = new ArrayList<>();
        List<String> publicKeyIds = new ArrayList<>();
        String marker = null;
        PublicKeyList publicKeyList;

        do {
            String currentMarker = marker;
            publicKeyList = client.listPublicKeys(ListPublicKeysRequest.builder()
                .marker(currentMarker)
                .build()).publicKeyList();

            if (publicKeyList.hasItems()) {
                publicKeyIds.addAll(publicKeyList.items().stream()
                    .map(PublicKeySummary::id)
                    .collect(Collectors.toList()));
            }

            marker = publicKeyList.nextMarker();
        } while (!ObjectUtils.isBlank(marker));

        publicKeyIds.forEach(o -> publicKeys.add(client.getPublicKey(r -> r.id(o)).publicKey()));

        return publicKeys;
    }

    @Override
    protected List<PublicKey> findAws(CloudFrontClient client, Map<String, String> filters) {
        List<PublicKey> publicKeys = new ArrayList<>();

        if (filters.containsKey("id") && !ObjectUtils.isBlank(filters.get("id"))) {
            try {
                publicKeys.add(client.getPublicKey(GetPublicKeyRequest.builder()
                    .id(filters.get("id"))
                    .build()).publicKey());
            } catch (NoSuchPublicKeyException ignore) {
                // Public key not found.
            }
        }

        return publicKeys;
    }

    @Override
    protected String getRegion() {
        return "us-east-1";
    }

    @Override
    protected String getEndpoint() {
        return "https://cloudfront.amazonaws.com";
    }
}
