package gyro.aws.codebuild;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.codebuild.CodeBuildClient;
import software.amazon.awssdk.services.codebuild.model.BatchGetReportGroupsResponse;
import software.amazon.awssdk.services.codebuild.model.InvalidInputException;

@Type("report-group")
public class ReportGroupFinder extends AwsFinder<CodeBuildClient, BatchGetReportGroupsResponse, ReportGroupResource> {

    @Override
    protected List<BatchGetReportGroupsResponse> findAllAws(CodeBuildClient client) {
        List<BatchGetReportGroupsResponse> reportGroups = new ArrayList<>();

        try {
            reportGroups.add(client.batchGetReportGroups(request -> request
                .reportGroupArns(client.listReportGroups(r -> r.maxResults(100)).reportGroups())));
        } catch (InvalidInputException ex) {
            // Input report group ARNs empty
        }

        return reportGroups;
    }

    @Override
    protected List<BatchGetReportGroupsResponse> findAws(
        CodeBuildClient client, Map<String, String> filters) {
        List<BatchGetReportGroupsResponse> reportGroups = new ArrayList<>();

        try {
            reportGroups.add(client.batchGetReportGroups(request -> request
                .reportGroupArns(filters.get("arn"))));

        } catch (InvalidInputException ex) {
            // Input report group ARN empty
        }

        return reportGroups;
    }
}
