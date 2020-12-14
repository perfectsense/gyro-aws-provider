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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.codebuild.CodeBuildClient;
import software.amazon.awssdk.services.codebuild.model.InvalidInputException;
import software.amazon.awssdk.services.codebuild.model.ListReportGroupsResponse;
import software.amazon.awssdk.services.codebuild.model.ReportGroup;
import software.amazon.awssdk.services.codebuild.model.ReportGroupSortByType;

/**
 * Query report group.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    report-group: $(external-query aws::codebuild-report-group { arn: ""})
 */
@Type("codebuild-report-group")
public class ReportGroupFinder extends AwsFinder<CodeBuildClient, ReportGroup, ReportGroupResource> {

    private String arn;

    /**
     * The arn of the report group.
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    protected List<ReportGroup> findAllAws(CodeBuildClient client) {
        List<String> reportGroupArns = client.listReportGroupsPaginator(r -> r.sortBy(ReportGroupSortByType.NAME))
            .stream().map(ListReportGroupsResponse::reportGroups)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        return client.batchGetReportGroups(r -> r.reportGroupArns(reportGroupArns)).reportGroups();
    }

    @Override
    protected List<ReportGroup> findAws(
        CodeBuildClient client, Map<String, String> filters) {
        List<ReportGroup> reportGroups = new ArrayList<>();

        try {
            reportGroups.addAll(client.batchGetReportGroups(request -> request
                .reportGroupArns(filters.get("arn"))).reportGroups());

        } catch (InvalidInputException ignore) {
            // Report group arn not valid
        }

        return reportGroups;
    }
}
