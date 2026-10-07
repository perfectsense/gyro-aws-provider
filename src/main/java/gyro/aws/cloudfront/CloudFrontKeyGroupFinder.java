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
import software.amazon.awssdk.services.cloudfront.model.GetKeyGroupRequest;
import software.amazon.awssdk.services.cloudfront.model.KeyGroup;
import software.amazon.awssdk.services.cloudfront.model.KeyGroupList;
import software.amazon.awssdk.services.cloudfront.model.KeyGroupSummary;
import software.amazon.awssdk.services.cloudfront.model.ListKeyGroupsRequest;
import software.amazon.awssdk.services.cloudfront.model.NoSuchResourceException;

/**
 * Query CloudFront key group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    key-group: $(external-query aws::cloudfront-key-group { id: 'a1b2c3d4-1234-5678-90ab-cdef01234567' })
 */
@Type("cloudfront-key-group")
public class CloudFrontKeyGroupFinder extends AwsFinder<CloudFrontClient, KeyGroup, CloudFrontKeyGroupResource> {

    private String id;

    /**
     * The ID of the key group.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<KeyGroup> findAllAws(CloudFrontClient client) {
        List<KeyGroup> keyGroups = new ArrayList<>();
        String marker = null;
        KeyGroupList keyGroupList;

        do {
            String currentMarker = marker;
            keyGroupList = client.listKeyGroups(ListKeyGroupsRequest.builder()
                .marker(currentMarker)
                .build()).keyGroupList();

            if (keyGroupList.hasItems()) {
                keyGroups.addAll(keyGroupList.items().stream()
                    .map(KeyGroupSummary::keyGroup)
                    .collect(Collectors.toList()));
            }

            marker = keyGroupList.nextMarker();
        } while (!ObjectUtils.isBlank(marker));

        return keyGroups;
    }

    @Override
    protected List<KeyGroup> findAws(CloudFrontClient client, Map<String, String> filters) {
        List<KeyGroup> keyGroups = new ArrayList<>();

        if (filters.containsKey("id") && !ObjectUtils.isBlank(filters.get("id"))) {
            try {
                keyGroups.add(client.getKeyGroup(GetKeyGroupRequest.builder()
                    .id(filters.get("id"))
                    .build()).keyGroup());
            } catch (NoSuchResourceException ignore) {
                // Key group not found.
            }
        }

        return keyGroups;
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
