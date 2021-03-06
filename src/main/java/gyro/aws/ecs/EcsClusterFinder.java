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

package gyro.aws.ecs;

import java.util.ArrayList;
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

/**
 * Query ECS cluster.
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
        List<String> clusterArns = client.listClustersPaginator().clusterArns().stream().collect(Collectors.toList());

        if (!clusterArns.isEmpty()) {
            return new ArrayList<>(client.describeClusters(r -> r.clusters(clusterArns)
                .includeWithStrings("TAGS", "SETTINGS")).clusters());

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
            return new ArrayList<>(client.describeClusters(r -> r.clusters(filters.get("name"))
                .include(ClusterField.TAGS, ClusterField.SETTINGS)).clusters());

        } catch (ClusterNotFoundException ex) {
            return Collections.emptyList();
        }
    }
}
