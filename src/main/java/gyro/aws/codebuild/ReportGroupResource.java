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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.codebuild.CodeBuildClient;
import software.amazon.awssdk.services.codebuild.model.BatchGetReportGroupsResponse;
import software.amazon.awssdk.services.codebuild.model.CreateReportGroupResponse;
import software.amazon.awssdk.services.codebuild.model.ReportGroup;
import software.amazon.awssdk.services.codebuild.model.ResourceNotFoundException;
import software.amazon.awssdk.services.codebuild.model.Tag;

/**
 * Creates a report group with the specified Name, Type, and Report Export Configuration.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::report-group report-group
 *        report-export-config
 *            export-config-type: "NO_EXPORT"
 *        end
 *
 *        name: "gyro-test-report-group"
 *        type: "TEST"
 *    end
 */
@Type("report-group")
public class ReportGroupResource extends AwsResource implements Copyable<ReportGroup> {

    // Minimum required fields
    private Boolean deleteReports;
    private CodebuildReportExportConfig reportExportConfig;
    private String name;
    private Map<String, String> tags;
    private String type;

    // Read-only
    private String arn;
    private CodebuildReport report;

    /**
     * The report export config about where the report group test results are exported.
     */
    @Required
    @Updatable
    public CodebuildReportExportConfig getReportExportConfig() {
        return reportExportConfig;
    }

    public void setReportExportConfig(CodebuildReportExportConfig reportExportConfig) {
        this.reportExportConfig = reportExportConfig;
    }

    /**
     * The name of the report group.
     */
    @Required
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The type of report group.
     */
    @Required
    @ValidStrings({ "TEST", "CODE_COVERAGE" })
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * A list of tags that are attached to the report group.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * When set to ``true`` deleting the report group automatically deletes all the reports under it. If set to
     * ``false`` deleting a non empty report group is halted.
     */
    public Boolean getDeleteReports() {
        return deleteReports;
    }

    public void setDeleteReports(Boolean deleteReports) {
        this.deleteReports = deleteReports;
    }

    /**
     * The Amazon Resource Name (ARN) of the report group.
     */
    @Id
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The results from running a series of test cases during a run of a build project.
     */
    @Output
    public CodebuildReport getReport() {
        return report;
    }

    public void setReport(CodebuildReport report) {
        this.report = report;
    }

    @Override
    public void copyFrom(ReportGroup reportGroup) {
        setName(reportGroup.name());
        setType(reportGroup.typeAsString());

        if (reportGroup.exportConfig() != null) {
            CodebuildReportExportConfig exportConfig = newSubresource(CodebuildReportExportConfig.class);
            exportConfig.copyFrom(reportGroup.exportConfig());
            setReportExportConfig(exportConfig);
        } else {
            setReportExportConfig(null);
        }

        if (reportGroup.tags() != null) {
            Map<String, String> tags = new HashMap<>();
            CodebuildProjectTag tag = newSubresource(CodebuildProjectTag.class);

            for (Tag t : reportGroup.tags()) {
                tag.copyFrom(t);
                tags.put(t.key(), t.value());
            }

            setTags(tags);
        } else {
            setTags(null);
        }
    }

    @Override
    public boolean refresh() {
        CodeBuildClient client = createClient(CodeBuildClient.class);
        BatchGetReportGroupsResponse response = null;

        try {
            response = client.batchGetReportGroups(r -> r.reportGroupArns(Collections.singletonList(getArn())));
        } catch (ResourceNotFoundException ex) {
            // No Resource found
        }

        if (response == null || response.reportGroups().isEmpty()) {
            return false;
        }

        copyFrom(response.reportGroups().get(0));
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        CodeBuildClient client = createClient(CodeBuildClient.class);

        CreateReportGroupResponse response = client.createReportGroup(r -> r
            .exportConfig(getReportExportConfig().toReportExportConfig())
            .name(getName())
            .type(getType())
            .tags(CodebuildProjectTag.toProjectTags(getTags()))
        );

        setArn(response.reportGroup().arn());
        refresh();
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        CodeBuildClient client = createClient(CodeBuildClient.class);

        client.updateReportGroup(r -> r
            .arn(getArn())
            .exportConfig(getReportExportConfig().toReportExportConfig())
            .tags(CodebuildProjectTag.toProjectTags(getTags()))
        );
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        CodeBuildClient client = createClient(CodeBuildClient.class);

        client.deleteReportGroup(r -> r
            .arn(getArn())
            .deleteReports(getDeleteReports()));
    }
}
