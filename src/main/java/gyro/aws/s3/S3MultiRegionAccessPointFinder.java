/*
 * Copyright 2019, Perfect Sense, Inc.
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

package gyro.aws.s3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gyro.aws.AwsCredentials;
import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import gyro.core.Type;
import software.amazon.awssdk.services.s3control.S3ControlClient;
import software.amazon.awssdk.services.s3control.model.GetMultiRegionAccessPointRequest;
import software.amazon.awssdk.services.s3control.model.GetMultiRegionAccessPointResponse;
import software.amazon.awssdk.services.s3control.model.ListMultiRegionAccessPointsRequest;
import software.amazon.awssdk.services.s3control.model.ListMultiRegionAccessPointsResponse;
import software.amazon.awssdk.services.s3control.model.MultiRegionAccessPointReport;
import software.amazon.awssdk.services.s3control.model.S3ControlException;
import software.amazon.awssdk.services.sts.StsClient;

/**
 * Query S3 Multi-Region Access Point.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    multi-region-access-point: $(external-query aws::s3-multi-region-access-point { name: 'mrap-example' })
 */
@Type("s3-multi-region-access-point")
public class S3MultiRegionAccessPointFinder extends AwsFinder<S3ControlClient, MultiRegionAccessPointReport, S3MultiRegionAccessPointResource> {

    private String name;

    /**
     * The name of the Multi-Region Access Point.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<MultiRegionAccessPointReport> findAllAws(S3ControlClient client) {
        List<MultiRegionAccessPointReport> multiRegionAccessPoints = new ArrayList<>();
        String nextToken = null;

        do {
            ListMultiRegionAccessPointsRequest.Builder requestBuilder = ListMultiRegionAccessPointsRequest.builder()
                .accountId(getAccountId());

            if (nextToken != null) {
                requestBuilder.nextToken(nextToken);
            }

            ListMultiRegionAccessPointsResponse response = client.listMultiRegionAccessPoints(requestBuilder.build());
            multiRegionAccessPoints.addAll(response.accessPoints());
            nextToken = response.nextToken();

        } while (nextToken != null);

        return multiRegionAccessPoints;
    }

    @Override
    protected List<MultiRegionAccessPointReport> findAws(S3ControlClient client, Map<String, String> filters) {
        List<MultiRegionAccessPointReport> multiRegionAccessPoints = new ArrayList<>();

        if (filters.containsKey("name")) {
            try {
                GetMultiRegionAccessPointResponse response = client.getMultiRegionAccessPoint(
                    GetMultiRegionAccessPointRequest.builder()
                        .accountId(getAccountId())
                        .name(filters.get("name"))
                        .build()
                );
                multiRegionAccessPoints.add(response.accessPoint());

            } catch (S3ControlException ex) {
                // Multi-Region Access Point not found, return empty list
            }
        }

        return multiRegionAccessPoints;
    }

    @Override
    protected String getRegion() {
        // Multi-Region Access Points are managed in us-west-2
        return "us-west-2";
    }

    private String getAccountId() {
        StsClient stsClient = AwsResource.createClient(StsClient.class, credentials(AwsCredentials.class));
        return stsClient.getCallerIdentity().account();
    }
}