package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.codebuild.model.ReportExportConfig;

public class CodebuildReportExportConfig extends Diffable implements Copyable<ReportExportConfig> {

    private String exportConfigType;
    private CodebuildS3ReportExportConfig s3ReportExportConfig;

    @Updatable
    @ValidStrings({ "S3", "NO_EXPORT" })
    public String getExportConfigType() {
        return exportConfigType;
    }

    public void setExportConfigType(String exportConfigType) {
        this.exportConfigType = exportConfigType;
    }

    @Updatable
    public CodebuildS3ReportExportConfig getS3ReportExportConfig() {
        return s3ReportExportConfig;
    }

    public void setS3ReportExportConfig(CodebuildS3ReportExportConfig s3ReportExportConfig) {
        this.s3ReportExportConfig = s3ReportExportConfig;
    }

    @Override
    public void copyFrom(ReportExportConfig model) {
        setExportConfigType(model.exportConfigTypeAsString());

        if (model.s3Destination() != null) {
            CodebuildS3ReportExportConfig s3ReportExportConfig = newSubresource(CodebuildS3ReportExportConfig.class);
            s3ReportExportConfig.copyFrom(model.s3Destination());
            setS3ReportExportConfig(s3ReportExportConfig);
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public ReportExportConfig toReportExportConfig() {
        return ReportExportConfig.builder()
            .exportConfigType(getExportConfigType())
            .s3Destination(getS3ReportExportConfig() != null ? getS3ReportExportConfig().toS3ReportExportConfig() : null)
            .build();
    }
}
