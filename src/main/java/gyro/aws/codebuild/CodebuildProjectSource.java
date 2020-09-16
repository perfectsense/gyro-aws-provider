package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
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
}
