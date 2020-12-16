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
import java.util.Set;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.validation.Min;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.codebuild.model.ProjectSource;
import software.amazon.awssdk.services.codebuild.model.SourceType;

public class CodebuildProjectSource extends Diffable implements Copyable<ProjectSource> {

    private String buildSpec;
    private CodebuildBuildStatusConfig buildStatusConfig;
    private Integer gitCloneDepth;
    private CodebuildGitSubmodulesConfig gitSubmodulesConfig;
    private Boolean insecureSsl;
    private String location;
    private Boolean reportBuildStatus;
    private String sourceIdentifier;
    private SourceType type;

    /**
     * The type of repository that contains the source code to be built.
     */
    @Updatable
    @Required
    @ValidStrings({ "CODECOMMIT", "CODEPIPELINE", "GITHUB", "S3", "BITBUCKET", "GITHUB_ENTERPRISE", "NO_SOURCE" })
    public SourceType getType() {
        return type;
    }

    public void setType(SourceType type) {
        this.type = type;
    }

    /**
     * The location of the source code to be built.
     */
    @Updatable
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * The buildspec file declaration to use for the builds in the build project.
     */
    @Updatable
    public String getBuildSpec() {
        return buildSpec;
    }

    public void setBuildSpec(String buildspec) {
        this.buildSpec = buildspec;
    }

    /**
     * The config that defines how the build project reports the build status to the source provider.
     *
     * @subresource gyro.aws.codebuild.CodebuildBuildStatusConfig
     */
    @Updatable
    public CodebuildBuildStatusConfig getBuildStatusConfig() {
        return buildStatusConfig;
    }

    public void setBuildStatusConfig(CodebuildBuildStatusConfig buildStatusConfig) {
        this.buildStatusConfig = buildStatusConfig;
    }

    /**
     * The git clone depth for the build project.
     */
    @Updatable
    @Min(0)
    public Integer getGitCloneDepth() {
        return gitCloneDepth;
    }

    public void setGitCloneDepth(Integer gitCloneDepth) {
        this.gitCloneDepth = gitCloneDepth;
    }

    /**
     * The git submodules configuration for the build project.
     *
     * @subresource gyro.aws.codebuild.CodebuildGitSubmodulesConfig
     */
    @Updatable
    public CodebuildGitSubmodulesConfig getGitSubmodulesConfig() {
        return gitSubmodulesConfig;
    }

    public void setGitSubmodulesConfig(CodebuildGitSubmodulesConfig gitSubmodulesConfig) {
        this.gitSubmodulesConfig = gitSubmodulesConfig;
    }

    /**
     * The field that specifies to ignore SSL warnings while connecting to the project source code.
     */
    @Updatable
    public Boolean getInsecureSsl() {
        return insecureSsl;
    }

    public void setInsecureSsl(Boolean insecureSsl) {
        this.insecureSsl = insecureSsl;
    }

    /**
     * The field that specifies to report the status of a build's start and finish to the source provider.
     */
    @Updatable
    public Boolean getReportBuildStatus() {
        return reportBuildStatus;
    }

    public void setReportBuildStatus(Boolean reportBuildStatus) {
        this.reportBuildStatus = reportBuildStatus;
    }

    /**
     * The identifier for the project source.
     */
    @Output
    public String getSourceIdentifier() {
        return sourceIdentifier;
    }

    public void setSourceIdentifier(String sourceIdentifier) {
        this.sourceIdentifier = sourceIdentifier;
    }

    @Override
    public void copyFrom(ProjectSource model) {
        setType(model.type());
        setLocation(model.location());
        setBuildSpec(model.buildspec());
        setGitCloneDepth(model.gitCloneDepth());
        setInsecureSsl(model.insecureSsl());
        setReportBuildStatus(model.reportBuildStatus());
        setSourceIdentifier(model.sourceIdentifier());

        setBuildStatusConfig(null);
        if (model.buildStatusConfig() != null) {
            CodebuildBuildStatusConfig buildStatusConfig = newSubresource(CodebuildBuildStatusConfig.class);
            buildStatusConfig.copyFrom(model.buildStatusConfig());
            setBuildStatusConfig(buildStatusConfig);
        }

        setGitSubmodulesConfig(null);
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

        if (getBuildStatusConfig() != null && !(getType().equals(SourceType.GITHUB)
            || getType().equals(SourceType.GITHUB_ENTERPRISE)
            || getType().equals(SourceType.BITBUCKET))) {
            errors.add(new ValidationError(
                this,
                null,
                "'build-status-config' can only be set when 'type' is 'GITHUB', 'GITHUB_ENTERPRISE', or 'BITBUCKET'."
            ));
        }

        if (getReportBuildStatus() == Boolean.TRUE && !(getType().equals(SourceType.GITHUB)
            || getType().equals(SourceType.GITHUB_ENTERPRISE)
            || getType().equals(SourceType.BITBUCKET))) {
            errors.add(new ValidationError(
                this,
                null,
                "'report-build-status' can only be set when 'type' is 'GITHUB', 'GITHUB_ENTERPRISE', or 'BITBUCKET'."
            ));
        }

        return errors;
    }

    public ProjectSource toProjectSource() {
        return ProjectSource.builder()
            .type(getType())
            .location(getLocation())
            .buildspec(getBuildSpec())
            .buildStatusConfig(getBuildStatusConfig() != null ? getBuildStatusConfig().toBuildStatusConfig() : null)
            .gitCloneDepth(getGitCloneDepth())
            .gitSubmodulesConfig(
                getGitSubmodulesConfig() != null ? getGitSubmodulesConfig().toGitSubmodulesConfig() : null)
            .insecureSsl(getInsecureSsl())
            .reportBuildStatus(getReportBuildStatus())
            .sourceIdentifier(getSourceIdentifier())
            .build();
    }
}
