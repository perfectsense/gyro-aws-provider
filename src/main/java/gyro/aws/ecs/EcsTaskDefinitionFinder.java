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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.EcsException;
import software.amazon.awssdk.services.ecs.model.ListTaskDefinitionsResponse;
import software.amazon.awssdk.services.ecs.model.TaskDefinition;

/**
 * Query ECS task definition.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    ecs-task-definition: $(external-query aws::ecs-task-definition { name: 'ecs-task-definition-example' })
 */
@Type("ecs-task-definition")
public class EcsTaskDefinitionFinder extends AwsFinder<EcsClient, TaskDefinition, EcsTaskDefinitionResource> {

    /**
     * An identifier for the task definition to find.
     * Specify the ``family`` to find the latest active revision, specify '``family``:``revision``' for a specific revision in the family, or provide the full ARN of the task definition to find.
     */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<TaskDefinition> findAllAws(EcsClient client) {
        ListTaskDefinitionsResponse listTaskDefinitionsResponse = client.listTaskDefinitions();

        if (listTaskDefinitionsResponse.hasTaskDefinitionArns()) {
            return listTaskDefinitionsResponse.taskDefinitionArns().stream()
                .map(o -> client.describeTaskDefinition(r -> r.taskDefinition(o))
                        .taskDefinition())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        } else {
            return Collections.emptyList();
        }

    }

    @Override
    protected List<TaskDefinition> findAws(EcsClient client, Map<String, String> filters) {
        if (!filters.containsKey("name")) {
            throw new IllegalArgumentException("'name' is required.");
        }

        List<TaskDefinition> taskDefinitions = new ArrayList<>();

        try {
            TaskDefinition definition = client.describeTaskDefinition(
                r -> r.taskDefinition(filters.get("name"))
            ).taskDefinition();

            if (definition != null) {
                taskDefinitions.add(definition);
            }

        } catch (EcsException ex) {
            // ignore
        }

        return taskDefinitions;
    }
}
