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
import java.util.Objects;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import gyro.core.validation.DependsOn;
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
 *    ecs-task-definition: $(external-query aws::ecs-task-definition { family: 'ecs-task-definition-example', revision: 1 })
 */
@Type("ecs-task-definition")
public class EcsTaskDefinitionFinder extends AwsFinder<EcsClient, TaskDefinition, EcsTaskDefinitionResource> {

    private String family;
    private Integer revision;
    private String arn;

    /**
     * The name shared among all revisions of a task definition.
     */
    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    /**
     * A version number of a task definition in a ``family``.
     */
    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    /**
     * The full Amazon Resource Name (ARN) of the task definition.
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    protected List<TaskDefinition> findAllAws(EcsClient client) {
        ListTaskDefinitionsResponse listTaskDefinitionsResponse = client.listTaskDefinitions();

        if (listTaskDefinitionsResponse.hasTaskDefinitionArns()) {
            return listTaskDefinitionsResponse.taskDefinitionArns().stream()
                .map(o -> getTaskDefinitionByIdentifier(client, o))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        } else {
            return Collections.emptyList();
        }

    }

    @Override
    protected List<TaskDefinition> findAws(EcsClient client, Map<String, String> filters) {
        List<TaskDefinition> taskDefinitions = new ArrayList<>();

        try {
            if (filters.containsKey("arn")) {
                TaskDefinition definitionFromArn = getTaskDefinitionByIdentifier(client, filters.get("arn"));

                if (definitionFromArn != null) {
                    taskDefinitions.add(definitionFromArn);
                }

            } else if (filters.containsKey("family")) {
                if (filters.containsKey("revision")) {
                    TaskDefinition definitionFromRevision = getTaskDefinitionByIdentifier(
                        client, filters.get("family") + ":" + filters.get("revision")
                    );

                    if (definitionFromRevision != null) {
                        taskDefinitions.add(definitionFromRevision);
                    }

                } else {
                    taskDefinitions = client.listTaskDefinitions(r -> r.familyPrefix(filters.get("family")))
                        .taskDefinitionArns().stream()
                        .map(o -> getTaskDefinitionByIdentifier(client, o))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                }

            } else {
                throw new IllegalArgumentException("One of the parameters 'arn' or 'family' is required.");
            }

        } catch (EcsException ex) {
            // ignore
        }

        return taskDefinitions;
    }

    private TaskDefinition getTaskDefinitionByIdentifier(EcsClient client, String identifier) {
        return client.describeTaskDefinition(r -> r.taskDefinition(identifier)).taskDefinition();
    }
}
