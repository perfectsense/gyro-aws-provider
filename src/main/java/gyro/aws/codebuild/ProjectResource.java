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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.RoleResource;
import gyro.aws.kms.KmsKeyResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.CollectionMax;
import gyro.core.validation.Range;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.codebuild.CodeBuildClient;
import software.amazon.awssdk.services.codebuild.model.BatchGetProjectsResponse;
import software.amazon.awssdk.services.codebuild.model.CreateProjectResponse;
import software.amazon.awssdk.services.codebuild.model.Project;
import software.amazon.awssdk.services.codebuild.model.ProjectArtifacts;
import software.amazon.awssdk.services.codebuild.model.ProjectFileSystemLocation;
import software.amazon.awssdk.services.codebuild.model.ProjectSource;
import software.amazon.awssdk.services.codebuild.model.ResourceNotFoundException;
import software.amazon.awssdk.services.codebuild.model.Tag;

/**
 * Creates a build project with the specified Name, Artifacts, Environment, Source, and Role.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::project project
 *        name: "project-example-name"
 *        description: "project-description"
 *        service-role: $(aws::iam-role iam-role-example)
 *        tags: {
 *            "tag": "value"
 *        }
 *
 *        source
 *            type: "S3"
 *            location: "codebuild-us-east-2-242040583208-input-bucket/MessageUtil.zip"
 *        end
 *
 *        artifacts
 *            type: "S3"
 *            location: "codebuild-us-east-2-242040583208-output-bucket"
 *            name: "project-example-artifact-name"
 *            encryption-disabled: false
 *            path: "example-path/path"
 *            packaging: "NONE"
 *        end
 *
 *        environment
 *            compute-type: "BUILD_GENERAL1_LARGE"
 *            type: "ARM_CONTAINER"
 *            image: "aws/codebuild/amazonlinux2-x86_64-standard:3.0"
 *        end
 *    end
 */
@Type("project")
public class ProjectResource extends AwsResource implements Copyable<Project> {

    private CodebuildProjectArtifacts artifacts;
    private Boolean badgeEnabled;
    private CodebuildProjectBuildBatchConfig buildBatchConfig;
    private CodebuildProjectCache cache;
    private String description;
    private KmsKeyResource encryptionKey;
    private CodebuildProjectEnvironment environment;
    private List<CodebuildProjectFileSystemLocation> fileSystemLocations;
    private CodebuildLogsConfig logsConfig;
    private String name;
    private Integer queuedTimeoutInMinutes;
    private List<CodebuildProjectArtifacts> secondaryArtifacts;
    private List<CodebuildProjectSource> secondarySources;
    private List<CodebuildProjectSourceVersion> secondarySourceVersions;
    private RoleResource serviceRole;
    private CodebuildProjectSource source;
    private String sourceVersion;
    private Map<String, String> tags;
    private Integer timeoutInMinutes;
    private CodebuildVpcConfig vpcConfig;
    private WebhookResource webhook;

    // Read-only
    private String arn;
    private CodebuildProjectBadge badge;

    /**
     * The build output artifacts for the build project.
     *
     * @subresource gyro.aws.codebuild.CodebuildProjectArtifacts
     */
    @Updatable
    @Required
    public CodebuildProjectArtifacts getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(CodebuildProjectArtifacts artifacts) {
        this.artifacts = artifacts;
    }

    /**
     * When set to ``true`` a publicly accessible URL will be generated for the project's build badge.
     */
    @Updatable
    public Boolean getBadgeEnabled() {
        return badgeEnabled;
    }

    public void setBadgeEnabled(Boolean badgeEnabled) {
        this.badgeEnabled = badgeEnabled;
    }

    /**
     * The description of the build project.
     */
    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The name of the build project.
     */
    @Id
    @Required
    @Regex("[A-Za-z0-9][A-Za-z0-9\\-_]{1,254}")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The IAM role that provides permissions for the build project.
     */
    @Updatable
    @Required
    public RoleResource getServiceRole() {
        return serviceRole;
    }

    public void setServiceRole(RoleResource serviceRole) {
        this.serviceRole = serviceRole;
    }

    /**
     * The build input source code configuration for the build project.
     *
     * @subresource gyro.aws.codebuild.CodebuildProjectSource
     */
    @Updatable
    @Required
    public CodebuildProjectSource getSource() {
        return source;
    }

    public void setSource(CodebuildProjectSource source) {
        this.source = source;
    }

    /**
     * The build environment configuration for the build project.
     *
     * @subresource gyro.aws.codebuild.CodebuildProjectEnvironment
     */
    @Updatable
    @Required
    public CodebuildProjectEnvironment getEnvironment() {
        return environment;
    }

    public void setEnvironment(CodebuildProjectEnvironment environment) {
        this.environment = environment;
    }

    /**
     * Specifies a list of tags that are attached to the build project.
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
     * The batch build options for the build project.
     *
     * @subresource gyro.aws.codebuild.CodebuildProjectBuildBatchConfig
     */
    @Updatable
    public CodebuildProjectBuildBatchConfig getBuildBatchConfig() {
        return buildBatchConfig;
    }

    public void setBuildBatchConfig(CodebuildProjectBuildBatchConfig buildBatchConfig) {
        this.buildBatchConfig = buildBatchConfig;
    }

    /**
     * The configuration to store logs for the build project.
     *
     * @subresource gyro.aws.codebuild.CodebuildLogsConfig
     */
    @Updatable
    public CodebuildLogsConfig getLogsConfig() {
        return logsConfig;
    }

    public void setLogsConfig(CodebuildLogsConfig logsConfig) {
        this.logsConfig = logsConfig;
    }

    /**
     * The recently used information for the build project.
     *
     * @subresource gyro.aws.codebuild.CodebuildProjectCache
     */
    @Updatable
    public CodebuildProjectCache getCache() {
        return cache;
    }

    public void setCache(CodebuildProjectCache cache) {
        this.cache = cache;
    }

    /**
     * The AWS Key Management Service Customer Master Key.
     */
    @Updatable
    public KmsKeyResource getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(KmsKeyResource encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    /**
     * The list of File System Locations for a build project.
     *
     * @subresource gyro.aws.codebuild.CodebuildProjectFileSystemLocation
     */
    @Updatable
    public List<CodebuildProjectFileSystemLocation> getFileSystemLocations() {
        if (fileSystemLocations == null) {
            fileSystemLocations = new ArrayList<>();
        }
        return fileSystemLocations;
    }

    public void setFileSystemLocations(List<CodebuildProjectFileSystemLocation> fileSystemLocations) {
        this.fileSystemLocations = fileSystemLocations;
    }

    /**
     * The number of minutes a build is allowed to be queued before it times out.
     */
    @Updatable
    @Range(min = 5, max = 480)
    public Integer getQueuedTimeoutInMinutes() {
        return queuedTimeoutInMinutes;
    }

    public void setQueuedTimeoutInMinutes(Integer queuedTimeoutInMinutes) {
        this.queuedTimeoutInMinutes = queuedTimeoutInMinutes;
    }

    /**
     * The list of secondary artifacts.
     *
     * @subresource gyro.aws.codebuild.CodebuildProjectArtifacts
     */
    @CollectionMax(12)
    @Updatable
    public List<CodebuildProjectArtifacts> getSecondaryArtifacts() {
        if (secondaryArtifacts == null) {
            secondaryArtifacts = new ArrayList<>();
        }
        return secondaryArtifacts;
    }

    public void setSecondaryArtifacts(List<CodebuildProjectArtifacts> secondaryArtifacts) {
        this.secondaryArtifacts = secondaryArtifacts;
    }

    /**
     * The list of secondary sources.
     *
     * @subresource gyro.aws.codebuild.CodebuildProjectSource
     */
    @CollectionMax(12)
    @Updatable
    public List<CodebuildProjectSource> getSecondarySources() {
        if (secondarySources == null) {
            secondarySources = new ArrayList<>();
        }
        return secondarySources;
    }

    public void setSecondarySources(List<CodebuildProjectSource> secondarySources) {
        this.secondarySources = secondarySources;
    }

    /**
     * The list of optional secondary source versions.
     *
     * @subresource gyro.aws.codebuild.CodebuildProjectSourceVersion
     */
    @CollectionMax(12)
    @Updatable
    public List<CodebuildProjectSourceVersion> getSecondarySourceVersions() {
        if (secondarySourceVersions == null) {
            secondarySourceVersions = new ArrayList<>();
        }
        return secondarySourceVersions;
    }

    public void setSecondarySourceVersions(List<CodebuildProjectSourceVersion> secondarySourceVersions) {
        this.secondarySourceVersions = secondarySourceVersions;
    }

    /**
     * The version of the build input for the build project.
     */
    @Updatable
    public String getSourceVersion() {
        return sourceVersion;
    }

    public void setSourceVersion(String sourceVersion) {
        this.sourceVersion = sourceVersion;
    }

    /**
     * The number of minutes to wait before it times out any build that has not been completed.
     */
    @Updatable
    @Range(min = 5, max = 480)
    public Integer getTimeoutInMinutes() {
        return timeoutInMinutes;
    }

    public void setTimeoutInMinutes(Integer timeoutInMinutes) {
        this.timeoutInMinutes = timeoutInMinutes;
    }

    /**
     * The VPC config to access resources in an Amazon VPC.
     *
     * @subresource gyro.aws.codebuild.CodebuildVpcConfig
     */
    @Updatable
    public CodebuildVpcConfig getVpcConfig() {
        return vpcConfig;
    }

    public void setVpcConfig(CodebuildVpcConfig vpcConfig) {
        this.vpcConfig = vpcConfig;
    }

    /**
     * The webhook that connects repository events to a build project.
     */
    @Output
    public WebhookResource getWebhook() {
        return webhook;
    }

    public void setWebhook(WebhookResource webhook) {
        this.webhook = webhook;
    }

    /**
     * The Amazon Resource Name (ARN) of the build project.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The build badge for the build project
     */
    @Output
    public CodebuildProjectBadge getBadge() {
        return badge;
    }

    public void setBadge(CodebuildProjectBadge badge) {
        this.badge = badge;
    }

    @Override
    public void copyFrom(Project project) {
        setDescription(project.description());
        setName(project.name());
        setServiceRole(findById(RoleResource.class, project.serviceRole()));
        setEncryptionKey(findById(KmsKeyResource.class, project.encryptionKey()));
        setQueuedTimeoutInMinutes(project.queuedTimeoutInMinutes());
        setSourceVersion(project.sourceVersion());
        setTimeoutInMinutes(project.timeoutInMinutes());
        setWebhook(findById(WebhookResource.class, project.webhook()));
        setArn(project.arn());

        if (project.artifacts() != null) {
            CodebuildProjectArtifacts projectArtifacts = newSubresource(CodebuildProjectArtifacts.class);
            projectArtifacts.copyFrom(project.artifacts());
            setArtifacts(projectArtifacts);
        } else {
            setArtifacts(null);
        }

        if (project.source() != null) {
            CodebuildProjectSource projectSource = newSubresource(CodebuildProjectSource.class);
            projectSource.copyFrom(project.source());
            setSource(projectSource);
        } else {
            setSource(null);
        }

        if (project.environment() != null) {
            CodebuildProjectEnvironment environment = newSubresource(CodebuildProjectEnvironment.class);
            environment.copyFrom(project.environment());
            setEnvironment(environment);
        } else {
            setEnvironment(null);
        }

        if (project.badge() != null) {
            CodebuildProjectBadge badge = newSubresource(CodebuildProjectBadge.class);
            badge.copyFrom(project.badge());
            setBadge(badge);
            setBadgeEnabled(badge.getBadgeEnabled());
        } else {
            setBadge(null);
            setBadgeEnabled(null);
        }

        if (project.tags() != null) {
            Map<String, String> tags = new HashMap<>();
            CodebuildProjectTag tag = newSubresource(CodebuildProjectTag.class);

            for (Tag t : project.tags()) {
                tag.copyFrom(t);
                tags.put(t.key(), t.value());
            }

            setTags(tags);
        } else {
            setTags(null);
        }

        if (project.buildBatchConfig() != null) {
            CodebuildProjectBuildBatchConfig buildBatchConfig = newSubresource(
                CodebuildProjectBuildBatchConfig.class);
            buildBatchConfig.copyFrom(project.buildBatchConfig());
            setBuildBatchConfig(buildBatchConfig);
        } else {
            setBuildBatchConfig(null);
        }

        if (project.logsConfig() != null) {
            CodebuildLogsConfig logsConfig = newSubresource(CodebuildLogsConfig.class);
            logsConfig.copyFrom(project.logsConfig());
            setLogsConfig(logsConfig);
        } else {
            setLogsConfig(null);
        }

        if (project.cache() != null) {
            CodebuildProjectCache cache = newSubresource(CodebuildProjectCache.class);
            cache.copyFrom(project.cache());
            setCache(cache);
        } else {
            setCache(null);
        }

        if (project.fileSystemLocations() != null) {
            List<CodebuildProjectFileSystemLocation> fileSystemLocations = new ArrayList<>();
            CodebuildProjectFileSystemLocation fileSystemLocation = newSubresource(
                CodebuildProjectFileSystemLocation.class);

            for (ProjectFileSystemLocation pfsl : project.fileSystemLocations()) {
                fileSystemLocation.copyFrom(pfsl);
                fileSystemLocations.add(fileSystemLocation);
            }

            setFileSystemLocations(fileSystemLocations);
        } else {
            setFileSystemLocations(null);
        }

        if (project.secondaryArtifacts() != null) {
            List<CodebuildProjectArtifacts> secondaryArtifacts = new ArrayList<>();
            CodebuildProjectArtifacts artifact = newSubresource(CodebuildProjectArtifacts.class);

            for (ProjectArtifacts a : project.secondaryArtifacts()) {
                artifact.copyFrom(a);
                secondaryArtifacts.add(artifact);
            }

            setSecondaryArtifacts(secondaryArtifacts);
        } else {
            setSecondaryArtifacts(null);
        }

        if (project.secondarySources() != null) {
            List<CodebuildProjectSource> secondarySources = new ArrayList<>();
            CodebuildProjectSource source = newSubresource(CodebuildProjectSource.class);

            for (ProjectSource s : project.secondarySources()) {
                source.copyFrom(s);
                secondarySources.add(source);
            }

            setSecondarySources(secondarySources);
        } else {
            setSecondarySources(null);
        }

        if (project.vpcConfig() != null) {
            CodebuildVpcConfig vpcConfig = newSubresource(CodebuildVpcConfig.class);
            vpcConfig.copyFrom(project.vpcConfig());
            setVpcConfig(vpcConfig);
        } else {
            setVpcConfig(null);
        }
    }

    @Override
    public boolean refresh() {
        CodeBuildClient client = createClient(CodeBuildClient.class);
        BatchGetProjectsResponse response = null;

        try {
            response = client.batchGetProjects(r -> r.names(getName()));
        } catch (ResourceNotFoundException ex) {
            // No Resource found
        }

        if (response == null || response.projects().isEmpty()) {
            return false;
        }

        copyFrom(response.projects().get(0));
        return true;

    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        CodeBuildClient client = createClient(CodeBuildClient.class);

        CreateProjectResponse response = client.createProject(r -> r
            .name(getName())
            .badgeEnabled(getBadgeEnabled())
            .encryptionKey(getEncryptionKey() != null ? getEncryptionKey().getArn() : null)
            .queuedTimeoutInMinutes(getQueuedTimeoutInMinutes())
            .sourceVersion(getSourceVersion())
            .timeoutInMinutes(getTimeoutInMinutes())
            .buildBatchConfig(getBuildBatchConfig() != null ? getBuildBatchConfig().toProjectBuildBatchConfig() : null)
            .logsConfig(getLogsConfig() != null ? getLogsConfig().toLogsConfig() : null)
            .description(getDescription())
            .serviceRole(getServiceRole() != null ? getServiceRole().getArn() : null)
            .environment(getEnvironment().toProjectEnvironment())
            .source(getSource().toProjectSource())
            .artifacts(getArtifacts().toProjectArtifacts())
            .tags(CodebuildProjectTag.toProjectTags(getTags()))
            .cache(getCache() != null ? getCache().toProjectCache() : null)
            .fileSystemLocations(getFileSystemLocations().stream()
                .map(CodebuildProjectFileSystemLocation::toProjectFileSystemLocation)
                .collect(Collectors.toList()))
            .secondaryArtifacts(getSecondaryArtifacts().stream()
                .map(CodebuildProjectArtifacts::toProjectArtifacts)
                .collect(
                    Collectors.toList()))
            .secondarySources(getSecondarySources().stream()
                .map(CodebuildProjectSource::toProjectSource)
                .collect(Collectors.toList()))
            .vpcConfig(getVpcConfig() != null ? getVpcConfig().toProjectVpcConfig() : null)
        );

        setArn(response.project().arn());
        refresh();
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

        CodeBuildClient client = createClient(CodeBuildClient.class);

        client.updateProject(r -> r.name(getName())
            .badgeEnabled(getBadgeEnabled())
            .buildBatchConfig(getBuildBatchConfig() != null ? getBuildBatchConfig().toProjectBuildBatchConfig() : null)
            .logsConfig(getLogsConfig() != null ? getLogsConfig().toLogsConfig() : null)
            .description(getDescription())
            .serviceRole(getServiceRole() != null ? getServiceRole().getArn() : null)
            .environment(getEnvironment().toProjectEnvironment())
            .source(getSource().toProjectSource())
            .artifacts(getArtifacts().toProjectArtifacts())
            .tags(CodebuildProjectTag.toProjectTags(getTags()))
            .cache(getCache() != null ? getCache().toProjectCache() : null)
            .encryptionKey(getEncryptionKey() != null ? getEncryptionKey().getArn() : null)
            .fileSystemLocations(getFileSystemLocations().stream()
                .map(CodebuildProjectFileSystemLocation::toProjectFileSystemLocation)
                .collect(Collectors.toList()))
            .queuedTimeoutInMinutes(getQueuedTimeoutInMinutes())
            .secondaryArtifacts(getSecondaryArtifacts().stream()
                .map(CodebuildProjectArtifacts::toProjectArtifacts)
                .collect(
                    Collectors.toList()))
            .secondarySources(getSecondarySources().stream()
                .map(CodebuildProjectSource::toProjectSource)
                .collect(Collectors.toList()))
            .sourceVersion(getSourceVersion() != null ? getSourceVersion() : null)
            .timeoutInMinutes(getTimeoutInMinutes())
            .vpcConfig(getVpcConfig() != null ? getVpcConfig().toProjectVpcConfig() : null)
        );
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        CodeBuildClient client = createClient(CodeBuildClient.class);

        client.deleteProject(r -> r.name(getName()));
    }
}
