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
 *    project: $(external-query aws::project { names: ['project-example-name']})
 */
@Type("project")
public class ProjectFinder extends AwsFinder<CodeBuildClient, Project, ProjectResource> {

    /**
     * The names of build projects.
     */
    private List<String> names;

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    @Override
    protected List<Project> findAllAws(CodeBuildClient client) {
        List<Project> responseList = new ArrayList<>();

        try {
            List<String> projectNames = client.listProjectsPaginator().projects().stream().collect(Collectors.toList());
            responseList = client.batchGetProjects(r -> r.names(projectNames)).projects();

        } catch (InvalidInputException ex) {
            // Input project name list empty
        }

        return responseList;
    }

    @Override
    protected List<Project> findAws(
        CodeBuildClient client, Map<String, String> filters) {
        List<Project> responseList = new ArrayList<>();

        try {
            responseList = client.batchGetProjects(r -> r.names(filters.get("names"))).projects();
        } catch (InvalidInputException ex) {
            // Input project name list empty
        }

        return responseList;
    }
}
