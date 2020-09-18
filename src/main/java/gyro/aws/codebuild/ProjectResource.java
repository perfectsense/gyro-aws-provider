package gyro.aws.codebuild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.RoleResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.codebuild.CodeBuildClient;
import software.amazon.awssdk.services.codebuild.model.BatchGetProjectsResponse;
import software.amazon.awssdk.services.codebuild.model.CreateProjectResponse;
import software.amazon.awssdk.services.codebuild.model.Project;
import software.amazon.awssdk.services.codebuild.model.ProjectArtifacts;
import software.amazon.awssdk.services.codebuild.model.ProjectFileSystemLocation;
import software.amazon.awssdk.services.codebuild.model.ProjectSource;
import software.amazon.awssdk.services.codebuild.model.ResourceNotFoundException;
import software.amazon.awssdk.services.codebuild.model.Tag;

@Type("project")
public class ProjectResource extends AwsResource implements Copyable<BatchGetProjectsResponse> {

    // Minimum required fields
    private CodebuildProjectArtifacts artifacts;
    private CodebuildProjectEnvironment environment;
    private String name;
    private RoleResource serviceRole;
    private CodebuildProjectSource source;

    // Project configuration
    private CodebuildProjectBadge badge;
    private String description;
    private Map<String, String> tags;

    // Batch configuration
    private CodebuildProjectBuildBatchConfig buildBatchConfig;

    // Logs
    private CodebuildLogsConfig logsConfig;

    // Artifacts - Cache
    private CodebuildProjectCache cache;

    // Artifacts -- Encryption key
    private String encryptionKey;

    // Environment - File systems
    private List<CodebuildProjectFileSystemLocation> fileSystemLocations;

    // Environment -- Queued timeout
    private Integer queuedTimeoutInMinutes;

    private List<CodebuildProjectArtifacts> secondaryArtifacts;
    private List<CodebuildProjectSource> secondarySources;

    // Source - Source version
    private String sourceVersion;

    // Environment - Timeout
    private Integer timeoutInMinutes;

    // Environment - VPC
    private CodebuildVpcConfig vpcConfig;

    // Source - Primary source webhook events - only when source is GitHub
    private WebhookResource webhook;

    // Read-only
    private String arn;

    @Updatable
    public CodebuildProjectArtifacts getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(CodebuildProjectArtifacts artifacts) {
        this.artifacts = artifacts;
    }

    @Updatable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Updatable
    public RoleResource getServiceRole() {
        return serviceRole;
    }

    public void setServiceRole(RoleResource serviceRole) {
        this.serviceRole = serviceRole;
    }

    @Updatable
    public CodebuildProjectSource getSource() {
        return source;
    }

    public void setSource(CodebuildProjectSource source) {
        this.source = source;
    }

    @Updatable
    public CodebuildProjectEnvironment getEnvironment() {
        return environment;
    }

    public void setEnvironment(CodebuildProjectEnvironment environment) {
        this.environment = environment;
    }

    @Updatable
    public CodebuildProjectBadge getBadge() {
        return badge;
    }

    public void setBadge(CodebuildProjectBadge badge) {
        this.badge = badge;
    }

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

    @Updatable
    public CodebuildProjectBuildBatchConfig getBuildBatchConfig() {
        return buildBatchConfig;
    }

    public void setBuildBatchConfig(CodebuildProjectBuildBatchConfig buildBatchConfig) {
        this.buildBatchConfig = buildBatchConfig;
    }

    @Updatable
    public CodebuildLogsConfig getLogsConfig() {
        return logsConfig;
    }

    public void setLogsConfig(CodebuildLogsConfig logsConfig) {
        this.logsConfig = logsConfig;
    }

    @Updatable
    public CodebuildProjectCache getCache() {
        return cache;
    }

    public void setCache(CodebuildProjectCache cache) {
        this.cache = cache;
    }

    @Updatable
    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

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

    @Updatable
    public Integer getQueuedTimeoutInMinutes() {
        return queuedTimeoutInMinutes;
    }

    public void setQueuedTimeoutInMinutes(Integer queuedTimeoutInMinutes) {
        this.queuedTimeoutInMinutes = queuedTimeoutInMinutes;
    }

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

    @Updatable
    public String getSourceVersion() {
        return sourceVersion;
    }

    public void setSourceVersion(String sourceVersion) {
        this.sourceVersion = sourceVersion;
    }

    @Updatable
    public Integer getTimeoutInMinutes() {
        return timeoutInMinutes;
    }

    public void setTimeoutInMinutes(Integer timeoutInMinutes) {
        this.timeoutInMinutes = timeoutInMinutes;
    }

    @Updatable
    public CodebuildVpcConfig getVpcConfig() {
        return vpcConfig;
    }

    public void setVpcConfig(CodebuildVpcConfig vpcConfig) {
        this.vpcConfig = vpcConfig;
    }

    @Updatable
    public WebhookResource getWebhook() {
        return webhook;
    }

    public void setWebhook(WebhookResource webhook) {
        this.webhook = webhook;
    }

    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
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

        if (response == null) {
            return false;
        }

        copyFrom(response);
        return true;

    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        CodeBuildClient client = createClient(CodeBuildClient.class);

        CreateProjectResponse response = client.createProject(r -> r
            .name(getName())
            .badgeEnabled(getBadge().getBadgeEnabled())
            .encryptionKey(getEncryptionKey())
            .queuedTimeoutInMinutes(getQueuedTimeoutInMinutes())
            .sourceVersion(getSourceVersion())
            .timeoutInMinutes(getTimeoutInMinutes())
            .buildBatchConfig(getBuildBatchConfig().toProjectBuildBatchConfig())
            .logsConfig(getLogsConfig().toProjectLogsConfig())
            .description(getDescription())
            .serviceRole(getServiceRole() != null ? getServiceRole().getArn() : null)
            .environment(getEnvironment().toProjectEnvironment())
            .source(getSource().toProjectSource())
            .artifacts(getArtifacts().toProjectArtifacts())
            .tags(CodebuildProjectTag.toProjectTags(getTags()))
            .cache(getCache().toProjectCache())
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
            .vpcConfig(getVpcConfig().toProjectVpcConfig())
        );

        setArn(response.project().arn());
        refresh();
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

        CodeBuildClient client = createClient(CodeBuildClient.class);

        if (changedFieldNames.size() > 1) {
            client.updateProject(r -> r.name(getName())
                .badgeEnabled(getBadge().getBadgeEnabled())
                .buildBatchConfig(getBuildBatchConfig().toProjectBuildBatchConfig())
                .logsConfig(getLogsConfig().toProjectLogsConfig())
                .description(getDescription())
                .serviceRole(getServiceRole() != null ? getServiceRole().getArn() : null)
                .environment(getEnvironment().toProjectEnvironment())
                .source(getSource().toProjectSource())
                .artifacts(getArtifacts().toProjectArtifacts())
                .tags(CodebuildProjectTag.toProjectTags(getTags()))
                .cache(getCache().toProjectCache())
                .encryptionKey(getEncryptionKey())
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
                .sourceVersion(getSourceVersion())
                .timeoutInMinutes(getTimeoutInMinutes())
                .vpcConfig(getVpcConfig().toProjectVpcConfig())
            );
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        CodeBuildClient client = createClient(CodeBuildClient.class);

        client.deleteProject(r -> r.name(getName()));
    }

    @Override
    public void copyFrom(BatchGetProjectsResponse model) {
        if (!model.projects().isEmpty()) {
            Project project = model.projects().get(0);

            setDescription(project.description());
            setName(project.name());
            setServiceRole(!ObjectUtils.isBlank(project.serviceRole())
                ? findById(RoleResource.class, project.serviceRole())
                : null);
            setEncryptionKey(project.encryptionKey());
            setQueuedTimeoutInMinutes(project.queuedTimeoutInMinutes());
            setSourceVersion(project.sourceVersion());
            setTimeoutInMinutes(project.timeoutInMinutes());
            setWebhook(findById(WebhookResource.class, project.webhook()) != null ? findById(
                WebhookResource.class,
                project.webhook()) : null);
            setArn(project.arn());

            if (project.artifacts() != null) {
                CodebuildProjectArtifacts projectArtifacts = newSubresource(CodebuildProjectArtifacts.class);
                projectArtifacts.copyFrom(project.artifacts());
                setArtifacts(projectArtifacts);
            }

            if (project.source() != null) {
                CodebuildProjectSource projectSource = newSubresource(CodebuildProjectSource.class);
                projectSource.copyFrom(project.source());
                setSource(projectSource);
            }

            if (project.environment() != null) {
                CodebuildProjectEnvironment environment = newSubresource(CodebuildProjectEnvironment.class);
                environment.copyFrom(project.environment());
                setEnvironment(environment);
            }

            if (project.badge() != null) {
                CodebuildProjectBadge badge = newSubresource(CodebuildProjectBadge.class);
                badge.copyFrom(project.badge());
                setBadge(badge);
            }

            if (project.tags() != null) {
                Map<String, String> tags = new HashMap<>();
                CodebuildProjectTag tag = newSubresource(CodebuildProjectTag.class);

                for (Tag t : project.tags()) {
                    tag.copyFrom(t);
                    tags.put(t.key(), t.value());
                }

                setTags(tags);
            }

            if (project.buildBatchConfig() != null) {
                CodebuildProjectBuildBatchConfig buildBatchConfig = newSubresource(
                    CodebuildProjectBuildBatchConfig.class);
                buildBatchConfig.copyFrom(project.buildBatchConfig());
                setBuildBatchConfig(buildBatchConfig);
            }

            if (project.logsConfig() != null) {
                CodebuildLogsConfig logsConfig = newSubresource(CodebuildLogsConfig.class);
                logsConfig.copyFrom(project.logsConfig());
                setLogsConfig(logsConfig);
            }

            if (project.cache() != null) {
                CodebuildProjectCache cache = newSubresource(CodebuildProjectCache.class);
                cache.copyFrom(project.cache());
                setCache(cache);
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
            }

            if (project.secondaryArtifacts() != null) {
                List<CodebuildProjectArtifacts> secondaryArtifacts = new ArrayList<>();
                CodebuildProjectArtifacts artifact = newSubresource(CodebuildProjectArtifacts.class);

                for (ProjectArtifacts a : project.secondaryArtifacts()) {
                    artifact.copyFrom(a);
                    secondaryArtifacts.add(artifact);
                }

                setSecondaryArtifacts(secondaryArtifacts);
            }

            if (project.secondarySources() != null) {
                List<CodebuildProjectSource> secondarySources = new ArrayList<>();
                CodebuildProjectSource source = newSubresource(CodebuildProjectSource.class);

                for (ProjectSource s : project.secondarySources()) {
                    source.copyFrom(s);
                    secondarySources.add(source);
                }

                setSecondarySources(secondarySources);
            }

            if (project.vpcConfig() != null) {
                CodebuildVpcConfig vpcConfig = newSubresource(CodebuildVpcConfig.class);
                vpcConfig.copyFrom(project.vpcConfig());
                setVpcConfig(vpcConfig);
            }
        }
    }
}
