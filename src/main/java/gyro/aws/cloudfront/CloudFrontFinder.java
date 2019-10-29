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

package gyro.aws.cloudfront;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.Distribution;
import software.amazon.awssdk.services.cloudfront.model.DistributionList;
import software.amazon.awssdk.services.cloudfront.model.DistributionSummary;
import software.amazon.awssdk.services.cloudfront.model.ListDistributionsRequest;
import software.amazon.awssdk.services.cloudfront.model.NoSuchDistributionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query cloudfront.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    cloudfront: $(external-query aws::cloudfront { id: 'E1QBYJ3V1SUL1G' })
 */
@Type("cloudfront")
public class CloudFrontFinder extends AwsFinder<CloudFrontClient, Distribution, CloudFrontResource> {
    private String id;

    /**
     * The ID of the cloudfront distribution.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<Distribution> findAllAws(CloudFrontClient client) {
        List<Distribution> distributions = new ArrayList<>();
        List<String> distributionIds = new ArrayList<>();
        String marker = null;
        DistributionList distributionList;

        do {
            if (marker == null) {
                distributionList = client.listDistributions().distributionList();
            } else {
                distributionList = client.listDistributions(ListDistributionsRequest.builder().marker(marker).build()).distributionList();
            }
            distributionIds.addAll(distributionList.items().stream().map(DistributionSummary::id).collect(Collectors.toList()));

            marker = distributionList.marker();
        } while (distributionList.isTruncated());

        distributionIds.forEach(o -> distributions.add(client.getDistribution(r -> r.id(o)).distribution()));

        return distributions;
    }

    @Override
    protected List<Distribution> findAws(CloudFrontClient client, Map<String, String> filters) {
        List<Distribution> distributions = new ArrayList<>();

        if (filters.containsKey("id") && !ObjectUtils.isBlank(filters.get("id"))) {
            try {
                distributions.add(client.getDistribution(r -> r.id(filters.get("id"))).distribution());
            } catch (NoSuchDistributionException ignore) {
                // ignore
            }
        }

        return distributions;
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
