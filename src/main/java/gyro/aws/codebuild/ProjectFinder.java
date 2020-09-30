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

package gyro.aws.codebuild;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.codebuild.CodeBuildClient;
import software.amazon.awssdk.services.codebuild.model.BatchGetProjectsResponse;
import software.amazon.awssdk.services.codebuild.model.InvalidInputException;

@Type("project")
public class ProjectFinder extends AwsFinder<CodeBuildClient, BatchGetProjectsResponse, ProjectResource> {

    @Override
    protected List<BatchGetProjectsResponse> findAllAws(CodeBuildClient client) {
        List<BatchGetProjectsResponse> responseList = new ArrayList<>();

        try {
            responseList = client.listProjectsPaginator().stream()
                .map(projects ->
                    client.batchGetProjects(r -> r.names(projects.projects())))
                .collect(Collectors.toList());
        } catch (InvalidInputException ex) {
            // Input project name list empty
        }

        return responseList;
    }

    @Override
    protected List<BatchGetProjectsResponse> findAws(
        CodeBuildClient client, Map<String, String> filters) {
        List<BatchGetProjectsResponse> responseList = new ArrayList<>();

        try {
            responseList.add(client.batchGetProjects(r -> r.names(filters.get("names"))));
        } catch (InvalidInputException ex) {
            // Input project name list empty
        }

        return responseList;
    }
}
