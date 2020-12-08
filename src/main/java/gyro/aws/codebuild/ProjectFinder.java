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

package gyro.aws.codebuild;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.codebuild.CodeBuildClient;
import software.amazon.awssdk.services.codebuild.model.InvalidInputException;
import software.amazon.awssdk.services.codebuild.model.Project;

/**
 * Query build project.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    project: $(external-query aws::project { name: 'project-example-name'})
 */
@Type("project")
public class ProjectFinder extends AwsFinder<CodeBuildClient, Project, ProjectResource> {

    /**
     * The name of build project.
     */
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<Project> findAllAws(CodeBuildClient client) {
        List<String> projectNames = client.listProjectsPaginator().projects().stream().collect(Collectors.toList());

        return client.batchGetProjects(r -> r.names(projectNames)).projects();
    }

    @Override
    protected List<Project> findAws(
        CodeBuildClient client, Map<String, String> filters) {
        List<Project> responseList = new ArrayList<>();

        try {
            responseList = client.batchGetProjects(r -> r.names(filters.get("name"))).projects();
        } catch (InvalidInputException ignore) {
            // Project name not valid
        }

        return responseList;
    }
}
