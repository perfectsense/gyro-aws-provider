/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.dax;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.dax.DaxClient;
import software.amazon.awssdk.services.dax.model.Cluster;
import software.amazon.awssdk.services.dax.model.DescribeClustersRequest;
import software.amazon.awssdk.services.dax.model.DescribeClustersResponse;

/**
 * Query DAX cluster.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    dax-cluster: $(external-query aws::dax-cluster { name: "cluster-example"})
 */
@Type("dax-cluster")
public class DaxClusterFinder extends AwsFinder<DaxClient, Cluster, DaxClusterResource> {

    private String name;

    /**
     * The name of the cluster.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<Cluster> findAllAws(DaxClient client) {
        List<Cluster> clusters = new ArrayList<>();
        DescribeClustersResponse response;
        String token = null;

        do {
            if (ObjectUtils.isBlank(token)) {
                response = client.describeClusters();
            } else {
                response = client.describeClusters(DescribeClustersRequest.builder()
                    .nextToken(token)
                    .build());
            }

            if (response.hasClusters()) {
                clusters.addAll(response.clusters());
            }

            token = response.nextToken();
        } while (token != null);

        return clusters;
    }

    @Override
    protected List<Cluster> findAws(DaxClient client, Map<String, String> filters) {
        return client.describeClusters(r -> r.clusterNames(filters.get("name"))).clusters();
    }
}
