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
import java.util.Objects;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.codebuild.CodeBuildClient;
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
 *    webhook: $(external-query aws::codebuild-webhook {name : 'project-example-name'})
 */
@Type("codebuild-webhook")
public class WebhookFinder extends AwsFinder<CodeBuildClient, Webhook, WebhookResource> {

    /**
     * The name of build projects.
     */
    private String name;

    public String getNames() {
        return name;
    }

    public void setNames(String name) {
        this.name = name;
    }

    @Override
    protected List<Webhook> findAllAws(CodeBuildClient client) {
        return client.listProjectsPaginator().stream()
            .map(projects -> client.batchGetProjects(r -> r.names(projects.projects())))
            .flatMap(o -> o.projects().stream())
            .map(Project::webhook)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    protected List<Webhook> findAws(
        CodeBuildClient client, Map<String, String> filters) {

        List<Webhook> webhooks = new ArrayList<>();

        try {
            webhooks.addAll(client.batchGetProjects(r -> r.names(filters.get("name")))
                .projects()
                .stream()
                .map(Project::webhook)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        } catch (InvalidInputException ex) {
            // Invalid input
        }

        return webhooks;
    }
}
