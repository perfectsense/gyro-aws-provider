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

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.codebuild.CodeBuildClient;
import software.amazon.awssdk.services.codebuild.model.InvalidInputException;
import software.amazon.awssdk.services.codebuild.model.ListReportGroupsResponse;
import software.amazon.awssdk.services.codebuild.model.ReportGroup;

/**
 * Query report group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    report-group: $(external-query aws::report-group { arn: "arn:aws:codebuild:us-east-2:242040583208:report-group/report-group-test"})
 */
@Type("report-group")
public class ReportGroupFinder extends AwsFinder<CodeBuildClient, ReportGroup, ReportGroupResource> {

    private String arn;

    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    protected List<ReportGroup> findAllAws(CodeBuildClient client) {
        List<ReportGroup> reportGroups = new ArrayList<>();

        String reportGroupToken;

        do {
            ListReportGroupsResponse listReportGroupsResponse = client.listReportGroups(r -> r.maxResults(100));

            for (String group : listReportGroupsResponse.reportGroups()) {
                reportGroups.addAll(client.batchGetReportGroups(r -> r.reportGroupArns(group)).reportGroups());
            }

            reportGroupToken = listReportGroupsResponse.nextToken();
        } while (reportGroupToken != null);

        return reportGroups;
    }

    @Override
    protected List<ReportGroup> findAws(
        CodeBuildClient client, Map<String, String> filters) {
        List<ReportGroup> reportGroups = new ArrayList<>();

        try {
            reportGroups.addAll(client.batchGetReportGroups(request -> request
                .reportGroupArns(filters.get("arn"))).reportGroups());

            if (reportGroups.isEmpty()) {
                return reportGroups;
            }

        } catch (InvalidInputException ex) {
            // Input report group ARN empty
        }

        return reportGroups;
    }
}
