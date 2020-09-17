package gyro.aws.codebuild;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import software.amazon.awssdk.services.codebuild.CodeBuildClient;
import software.amazon.awssdk.services.codebuild.model.BatchGetProjectsResponse;
import software.amazon.awssdk.services.codebuild.model.InvalidInputException;
import software.amazon.awssdk.services.codebuild.model.Project;
import software.amazon.awssdk.services.codebuild.model.Webhook;

public class WebhookFinder extends AwsFinder<CodeBuildClient, Webhook, WebhookResource> {

    @Override
    protected List<Webhook> findAllAws(CodeBuildClient client) {
        List<Webhook> webhooks = new ArrayList<>();

        try {
            List<BatchGetProjectsResponse> projectsResponseList = client.listProjectsPaginator().stream()
                .map(projects ->
                    client.batchGetProjects(r -> r.names(projects.projects())))
                .collect(Collectors.toList());

            webhooks = projectsResponseList.stream()
                .map(BatchGetProjectsResponse::projects)
                .flatMap(projects ->
                    projects.stream()
                        .map(Project::webhook))
                .collect(Collectors.toList());

        } catch (InvalidInputException ex) {
            // Input project name list empty
        }

        return webhooks;
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
        } catch (InvalidInputException ex) {
            // Input project name list empty
        }

        return webhooks;
    }
}
