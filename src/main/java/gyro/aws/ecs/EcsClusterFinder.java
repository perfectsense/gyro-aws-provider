/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.ecs;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.Cluster;
import software.amazon.awssdk.services.ecs.model.ClusterField;
import software.amazon.awssdk.services.ecs.model.ClusterNotFoundException;
import software.amazon.awssdk.services.ecs.model.ListClustersResponse;

/**
 * Query ecs cluster.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    ecs-cluster: $(external-query aws::ecs-cluster { name: 'ecs-cluster-example' })
 */
@Type("ecs-cluster")
public class EcsClusterFinder extends AwsFinder<EcsClient, Cluster, EcsClusterResource> {

    /**
     * The name identifying the cluster.
     * Must consist of 1 to 255 letters, numbers, and hyphens, and begin with a letter.
     */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<Cluster> findAllAws(EcsClient client) {
        ListClustersResponse response = client.listClusters();

        if (response.hasClusterArns()) {
            return client.describeClusters(
                r -> r.clusters(response.clusterArns())
                    .includeWithStrings("TAGS", "SETTINGS")
            ).clusters().stream().collect(Collectors.toList());

        } else {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<Cluster> findAws(EcsClient client, Map<String, String> filters) {
        if (!filters.containsKey("name")) {
            throw new IllegalArgumentException("'name' is required.");
        }

        try {
            return client.describeClusters(
                r -> r.clusters(filters.get("name"))
                    .include(ClusterField.TAGS, ClusterField.SETTINGS)
            ).clusters().stream().collect(Collectors.toList());

        } catch (ClusterNotFoundException ex) {
            return Collections.emptyList();
        }
    }
}
