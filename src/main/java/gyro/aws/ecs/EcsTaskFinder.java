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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.Task;

/**
 * Query ECS task.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    $ecs-task: $(external-query aws::ecs-task { cluster : "example-task"})
 */
@Type("ecs-task")
public class EcsTaskFinder extends AwsFinder<EcsClient, Task, EcsTaskResource> {

    private String cluster;
    private String id;

    /**
     * The cluster in which the task is running.
     */
    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    /**
     * The id of the running task.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<Task> findAllAws(EcsClient client) {
        List<Task> tasks = new ArrayList<>();
        client.listClusters()
            .clusterArns()
            .forEach(c -> {
                List<String> taskArns = client.listTasks(r -> r.cluster(c)).taskArns();
                if (!taskArns.isEmpty()) {
                    tasks.addAll(client.describeTasks(r -> r.cluster(c).tasks(taskArns)).tasks());
                }
            });

        return tasks;
    }

    @Override
    protected List<Task> findAws(EcsClient client, Map<String, String> filters) {
        List<Task> tasks = new ArrayList<>();
        client.listClusters()
            .clusterArns()
            .stream()
            .filter(c -> !(filters.containsKey("cluster") && !c.split("/")[c.split("/").length - 1]
                .equals(filters.get("cluster"))))
            .forEach(c -> {
                List<String> taskArns = client.listTasks(r -> r.cluster(c)).taskArns();
                if (!taskArns.isEmpty()) {
                    tasks.addAll(client.describeTasks(r -> r
                        .cluster(c)
                        .tasks(taskArns)
                        .build())
                        .tasks()
                        .stream()
                        .filter(s -> !(filters.containsKey("id") && !s.taskArn().contains(filters.get("id"))))
                        .collect(Collectors.toList()));
                }
            });

        return tasks;
    }
}
