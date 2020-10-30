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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.codebuild.model.Report;
import software.amazon.awssdk.services.codebuild.model.ReportStatusType;
import software.amazon.awssdk.services.codebuild.model.ReportType;

public class CodebuildReport extends Diffable implements Copyable<Report> {

    private String arn;
    private CodebuildCodeCoverageReportSummary codeCoverageReportSummary;
    private String created;
    private String executionId;
    private String expired;
    private CodebuildReportExportConfig exportConfig;
    private String name;
    private String reportGroupArn;
    private ReportStatusType status;
    private CodebuildTestReportSummary testSummary;
    private Boolean truncated;
    private ReportType type;

    /**
     * The ARN of the report run.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The report summary that contains a code coverage summary for the report.
     *
     * @subresource gyro.aws.codebuild.CodebuildCodeCoverageReportSummary
     */
    @Output
    public CodebuildCodeCoverageReportSummary getCodeCoverageReportSummary() {
        return codeCoverageReportSummary;
    }

    public void setCodeCoverageReportSummary(CodebuildCodeCoverageReportSummary codeCoverageReportSummary) {
        this.codeCoverageReportSummary = codeCoverageReportSummary;
    }

    /**
     * The date and time the report run occurred.
     */
    @Output
    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    /**
     * The ARN of the build run that generated the report.
     */
    @Output
    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    /**
     * The date and time the report expires.
     */
    @Output
    public String getExpired() {
        return expired;
    }

    public void setExpired(String expired) {
        this.expired = expired;
    }

    /**
     * Specifies where the raw data used to generate the report was exported.
     *
     * @subresource gyro.aws.codebuild.CodebuildReportExportConfig
     */
    @Output
    public CodebuildReportExportConfig getExportConfig() {
        return exportConfig;
    }

    public void setExportConfig(CodebuildReportExportConfig exportConfig) {
        this.exportConfig = exportConfig;
    }

    /**
     * The name of the report that was run.
     */
    @Output
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The ARN of the report group associated with the report.
     */
    @Output
    public String getReportGroupArn() {
        return reportGroupArn;
    }

    public void setReportGroupArn(String reportGroupArn) {
        this.reportGroupArn = reportGroupArn;
    }

    /**
     * The status of the report.
     */
    @Output
    @ValidStrings({"GENERATING", "SUCCEEDED", "FAILED", "INCOMPLETE", "DELETING"})
    public ReportStatusType getStatus() {
        return status;
    }

    public void setStatus(ReportStatusType status) {
        this.status = status;
    }

    /**
     * The test report summary.
     *
     * @subresource gyro.aws.codebuild.CodebuildTestReportSummary
     */
    @Output
    public CodebuildTestReportSummary getTestSummary() {
        return testSummary;
    }

    public void setTestSummary(CodebuildTestReportSummary testSummary) {
        this.testSummary = testSummary;
    }

    /**
     * When set to ``true`` the report run is truncated.
     */
    @Output
    public Boolean getTruncated() {
        return truncated;
    }

    public void setTruncated(Boolean truncated) {
        this.truncated = truncated;
    }

    /**
     * The type of the report that was run.
     */
    @Output
    @ValidStrings({"TEST", "CODE_COVERAGE"})
    public ReportType getType() {
        return type;
    }

    public void setType(ReportType type) {
        this.type = type;
    }

    @Override
    public void copyFrom(Report model) {
        setArn(model.arn());

        setCreated(model.created().toString());
        setExecutionId(model.executionId());
        setExpired(model.expired().toString());
        setName(model.name());
        setReportGroupArn(model.reportGroupArn());
        setStatus(model.status());
        setTruncated(model.truncated());
        setType(model.type());

        if (model.codeCoverageSummary() != null) {
            CodebuildCodeCoverageReportSummary summary = newSubresource(CodebuildCodeCoverageReportSummary.class);
            summary.copyFrom(model.codeCoverageSummary());
            setCodeCoverageReportSummary(summary);
        } else {
            setCodeCoverageReportSummary(null);
        }

        if (model.exportConfig() != null) {
            CodebuildReportExportConfig exportConfig = newSubresource(CodebuildReportExportConfig.class);
            exportConfig.copyFrom(model.exportConfig());
            setExportConfig(exportConfig);
        } else {
            setExportConfig(null);
        }

        if (model.testSummary() != null) {
            CodebuildTestReportSummary summary = newSubresource(CodebuildTestReportSummary.class);
            summary.copyFrom(model.testSummary());
            setTestSummary(summary);
        } else {
            setTestSummary(null);
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
