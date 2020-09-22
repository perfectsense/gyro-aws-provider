package gyro.aws.codebuild;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.validation.Min;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.codebuild.model.BuildStatusConfig;
import software.amazon.awssdk.services.codebuild.model.GitSubmodulesConfig;
import software.amazon.awssdk.services.codebuild.model.ProjectSource;

public class CodebuildProjectSource extends Diffable implements Copyable<ProjectSource> {

    private String type;
    private String location;

    private String buildspec;
    private CodebuildBuildStatusConfig buildStatusConfig;
    private Integer gitCloneDepth;
    private CodebuildGitSubmodulesConfig gitSubmodulesConfig;
    private Boolean insecureSsl;
    private Boolean reportBuildStatus;
    private String sourceIdentifier;

    @Updatable
    @Required
    @ValidStrings({ "CODECOMMIT", "CODEPIPELINE", "GITHUB", "S3", "BITBUCKET", "GITHUB_ENTERPRISE", "NO_SOURCE" })
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Updatable
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Updatable
    public String getBuildspec() {
        return buildspec;
    }

    public void setBuildspec(String buildspec) {
        this.buildspec = buildspec;
    }

    @Updatable
    public CodebuildBuildStatusConfig getBuildStatusConfig() {
        return buildStatusConfig;
    }

    public void setBuildStatusConfig(CodebuildBuildStatusConfig buildStatusConfig) {
        this.buildStatusConfig = buildStatusConfig;
    }

    @Updatable
    @Min(0)
    public Integer getGitCloneDepth() {
        return gitCloneDepth;
    }

    public void setGitCloneDepth(Integer gitCloneDepth) {
        this.gitCloneDepth = gitCloneDepth;
    }

    @Updatable
    public CodebuildGitSubmodulesConfig getGitSubmodulesConfig() {
        return gitSubmodulesConfig;
    }

    public void setGitSubmodulesConfig(CodebuildGitSubmodulesConfig gitSubmodulesConfig) {
        this.gitSubmodulesConfig = gitSubmodulesConfig;
    }

    @Updatable
    public Boolean getInsecureSsl() {
        return insecureSsl;
    }

    public void setInsecureSsl(Boolean insecureSsl) {
        this.insecureSsl = insecureSsl;
    }

    @Updatable
    public Boolean getReportBuildStatus() {
        return reportBuildStatus;
    }

    public void setReportBuildStatus(Boolean reportBuildStatus) {
        this.reportBuildStatus = reportBuildStatus;
    }

    @Output
    public String getSourceIdentifier() {
        return sourceIdentifier;
    }

    public void setSourceIdentifier(String sourceIdentifier) {
        this.sourceIdentifier = sourceIdentifier;
    }

    @Override
    public void copyFrom(ProjectSource model) {
        setType(model.typeAsString());
        setLocation(model.location());
        setBuildspec(model.buildspec());
        setGitCloneDepth(model.gitCloneDepth());
        setInsecureSsl(model.insecureSsl());
        setReportBuildStatus(model.reportBuildStatus());
        setSourceIdentifier(model.sourceIdentifier());

        if (model.buildStatusConfig() != null) {
            CodebuildBuildStatusConfig buildStatusConfig = newSubresource(CodebuildBuildStatusConfig.class);
            buildStatusConfig.copyFrom(model.buildStatusConfig());
            setBuildStatusConfig(buildStatusConfig);
        }

        if (model.gitSubmodulesConfig() != null) {
            CodebuildGitSubmodulesConfig gitSubmodulesConfig = newSubresource(CodebuildGitSubmodulesConfig.class);
            gitSubmodulesConfig.copyFrom(model.gitSubmodulesConfig());
            setGitSubmodulesConfig(gitSubmodulesConfig);
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getBuildStatusConfig() != null && !(getType().equals("GITHUB") || getType().equals("GITHUB_ENTERPRISE")
            || getType().equals("BITBUCKET"))) {
            errors.add(new ValidationError(
                this,
                null,
                "'build-status-config' is only used when 'type' is 'GITHUB', 'GITHUB_ENTERPRISE', or 'BITBUCKET'."
            ));
        }

        if (getReportBuildStatus() && !(getType().equals("GITHUB") || getType().equals("GITHUB_ENTERPRISE")
            || getType().equals("BITBUCKET"))) {
            errors.add(new ValidationError(
                this,
                null,
                "'report-build-status' is valid only when 'type' is 'GITHUB', 'GITHUB_ENTERPRISE', or 'BITBUCKET'."
            ));
        }

        return errors;
    }

    public ProjectSource toProjectSource() {
        return ProjectSource.builder()
            .type(getType())
            .location(getLocation())
            .buildspec(getBuildspec())
            .buildStatusConfig(getBuildStatusConfig().toBuildStatusConfig())
            .gitCloneDepth(getGitCloneDepth())
            .gitSubmodulesConfig(getGitSubmodulesConfig().toGitSubmodulesConfig())
            .insecureSsl(getInsecureSsl())
            .reportBuildStatus(getReportBuildStatus())
            .sourceIdentifier(getSourceIdentifier())
            .build();
    }
}
