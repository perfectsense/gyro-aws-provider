package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.codebuild.model.Report;

public class CodebuildReport extends Diffable implements Copyable<Report> {

    private String arn;
    private CodebuildCodeCoverageReportSummary codeCoverageReportSummary;
    private String created;
    private String executionId;
    private String expired;
    private CodebuildReportExportConfig exportConfig;
    private String name;
    private String reportGroupArn;
    private String status;
    private CodebuildTestReportSummary testSummary;
    private Boolean truncated;
    private String type;

    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Output
    public CodebuildCodeCoverageReportSummary getCodeCoverageReportSummary() {
        return codeCoverageReportSummary;
    }

    public void setCodeCoverageReportSummary(CodebuildCodeCoverageReportSummary codeCoverageReportSummary) {
        this.codeCoverageReportSummary = codeCoverageReportSummary;
    }

    @Output
    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    @Output
    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    @Output
    public String getExpired() {
        return expired;
    }

    public void setExpired(String expired) {
        this.expired = expired;
    }

    @Output
    public CodebuildReportExportConfig getExportConfig() {
        return exportConfig;
    }

    public void setExportConfig(CodebuildReportExportConfig exportConfig) {
        this.exportConfig = exportConfig;
    }

    @Output
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Output
    public String getReportGroupArn() {
        return reportGroupArn;
    }

    public void setReportGroupArn(String reportGroupArn) {
        this.reportGroupArn = reportGroupArn;
    }

    @Output
    @ValidStrings({ "GENERATING", "SUCCEEDED", "FAILED", "INCOMPLETE", "DELETING" })
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Output
    public CodebuildTestReportSummary getTestSummary() {
        return testSummary;
    }

    public void setTestSummary(CodebuildTestReportSummary testSummary) {
        this.testSummary = testSummary;
    }

    @Output
    public Boolean getTruncated() {
        return truncated;
    }

    public void setTruncated(Boolean truncated) {
        this.truncated = truncated;
    }

    @Output
    @ValidStrings({ "TEST", "CODE_COVERAGE" })
    public String getType() {
        return type;
    }

    public void setType(String type) {
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
        setStatus(model.statusAsString());
        setTruncated(model.truncated());
        setType(model.typeAsString());

        if (model.codeCoverageSummary() != null) {
            CodebuildCodeCoverageReportSummary summary = newSubresource(CodebuildCodeCoverageReportSummary.class);
            summary.copyFrom(model.codeCoverageSummary());
            setCodeCoverageReportSummary(summary);
        }

        if (model.exportConfig() != null) {
            CodebuildReportExportConfig exportConfig = newSubresource(CodebuildReportExportConfig.class);
            exportConfig.copyFrom(model.exportConfig());
            setExportConfig(exportConfig);
        }

        if (model.testSummary() != null) {
            CodebuildTestReportSummary summary = newSubresource(CodebuildTestReportSummary.class);
            summary.copyFrom(model.testSummary());
            setTestSummary(summary);
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
