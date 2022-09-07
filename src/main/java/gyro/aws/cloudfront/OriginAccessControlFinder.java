/*
 * Copyright 2022, Perfect Sense, Inc.
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
import software.amazon.awssdk.services.cloudfront.model.GetOriginAccessControlRequest;
import software.amazon.awssdk.services.cloudfront.model.ListOriginAccessControlsRequest;
import software.amazon.awssdk.services.cloudfront.model.NoSuchOriginAccessControlException;
import software.amazon.awssdk.services.cloudfront.model.OriginAccessControl;
import software.amazon.awssdk.services.cloudfront.model.OriginAccessControlList;
import software.amazon.awssdk.services.cloudfront.model.OriginAccessControlSummary;

/**
 * Query Origin Access Control.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    origin-access-control: $(external-query aws::origin-access-control { id: 'E32SZWDLAJKB3A' })
 */
@Type("origin-access-control")
public class OriginAccessControlFinder
    extends AwsFinder<CloudFrontClient, OriginAccessControl, OriginAccessControlResource> {

    private String id;

    /**
     * The ID of the Origin Access Control (OAC).
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<OriginAccessControl> findAllAws(CloudFrontClient client) {
        List<OriginAccessControl> accessControls = new ArrayList<>();
        List<String> accessControlIds = new ArrayList<>();
        String marker = null;
        OriginAccessControlList accessControlList;

        do {
            if (marker == null) {
                accessControlList = client.listOriginAccessControls(ListOriginAccessControlsRequest.builder().build())
                    .originAccessControlList();
            } else {
                accessControlList =
                    client.listOriginAccessControls(ListOriginAccessControlsRequest.builder().marker(marker).build())
                        .originAccessControlList();
            }
            accessControlIds.addAll(
                accessControlList.items().stream().map(OriginAccessControlSummary::id).collect(Collectors.toList()));

            marker = accessControlList.marker();
        } while (Boolean.TRUE.equals(accessControlList.isTruncated()));

        accessControlIds.forEach(
            o -> accessControls.add(client.getOriginAccessControl(r -> r.id(o)).originAccessControl()));

        return accessControls;
    }

    @Override
    protected List<OriginAccessControl> findAws(CloudFrontClient client, Map<String, String> filters) {
        List<OriginAccessControl> accessControls = new ArrayList<>();

        if (filters.containsKey("id") && !ObjectUtils.isBlank(filters.get("id"))) {
            try {
                accessControls.add(client.getOriginAccessControl(GetOriginAccessControlRequest.builder()
                    .id(filters.get("id")).build()).originAccessControl());
            } catch (NoSuchOriginAccessControlException ignore) {
                // ignore
            }
        }

        return accessControls;
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
