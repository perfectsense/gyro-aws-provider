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
import software.amazon.awssdk.services.codebuild.model.BatchGetProjectsResponse;
import software.amazon.awssdk.services.codebuild.model.InvalidInputException;
import software.amazon.awssdk.services.codebuild.model.Project;
import software.amazon.awssdk.services.codebuild.model.Webhook;

/**
 * Query webhook.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    webhook: $(external-query aws::webhook {names : ['project-example-name']})
 */
@Type("webhook")
public class WebhookFinder extends AwsFinder<CodeBuildClient, Webhook, WebhookResource> {

    private List<String> names;

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    @Override
    protected List<Webhook> findAllAws(CodeBuildClient client) {
        List<BatchGetProjectsResponse> projectsResponseList = client.listProjectsPaginator().stream()
            .map(projects ->
                client.batchGetProjects(r -> r.names(projects.projects())))
            .collect(Collectors.toList());

        return projectsResponseList.stream()
            .map(BatchGetProjectsResponse::projects)
            .flatMap(projects ->
                projects.stream()
                    .map(Project::webhook))
            .collect(Collectors.toList());
    }

    @Override
    protected List<Webhook> findAws(
        CodeBuildClient client, Map<String, String> filters) {

        List<Webhook> webhooks = new ArrayList<>();
        List<BatchGetProjectsResponse> responseList = new ArrayList<>();

        try {
            responseList.add(client.batchGetProjects(r -> r.names(filters.get("names"))));

            webhooks = responseList.stream()
                .map(BatchGetProjectsResponse::projects)
                .flatMap(projects ->
                    projects.stream()
                        .map(Project::webhook))
                .collect(Collectors.toList());

            if (webhooks.isEmpty()) {
                return webhooks;
            }

        } catch (InvalidInputException ex) {
            return webhooks;
        }

        return webhooks;
    }
}
