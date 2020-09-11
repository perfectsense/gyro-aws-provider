package gyro.aws.codebuild;

import java.util.ArrayList;
import java.util.Collections;
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
        try {
            return client.listProjectsPaginator().stream()
                .map(projects ->
                    client.batchGetProjects(r -> r.names(projects.projects())))
                .collect(Collectors.toList());
        } catch (InvalidInputException ex) {
            // Input project name list empty
        }

        return Collections.emptyList();
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
