package gyro.aws.codebuild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.RoleResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.codebuild.CodeBuildClient;
import software.amazon.awssdk.services.codebuild.model.BatchGetProjectsResponse;
import software.amazon.awssdk.services.codebuild.model.BatchRestrictions;
import software.amazon.awssdk.services.codebuild.model.BuildStatusConfig;
import software.amazon.awssdk.services.codebuild.model.CloudWatchLogsConfig;
import software.amazon.awssdk.services.codebuild.model.EnvironmentVariable;
import software.amazon.awssdk.services.codebuild.model.GitSubmodulesConfig;
import software.amazon.awssdk.services.codebuild.model.LogsConfig;
import software.amazon.awssdk.services.codebuild.model.Project;
import software.amazon.awssdk.services.codebuild.model.ProjectArtifacts;
import software.amazon.awssdk.services.codebuild.model.ProjectBuildBatchConfig;
import software.amazon.awssdk.services.codebuild.model.ProjectCache;
import software.amazon.awssdk.services.codebuild.model.ProjectEnvironment;
import software.amazon.awssdk.services.codebuild.model.ProjectSource;
import software.amazon.awssdk.services.codebuild.model.ResourceNotFoundException;
import software.amazon.awssdk.services.codebuild.model.S3LogsConfig;
import software.amazon.awssdk.services.codebuild.model.Tag;

@Type("project")
public class ProjectResource extends AwsResource implements Copyable<BatchGetProjectsResponse> {

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

        Map<String, Object> projectFields = getProjectFields();

        client.createProject(r -> r
            .name(getName())
            .badgeEnabled(getBadge().getBadgeEnabled())
            .encryptionKey(getEncryptionKey())
            .buildBatchConfig(
                projectFields.get("build-batch-config") != null ? (ProjectBuildBatchConfig) projectFields.get(
                    "build-batch-config") : null)
            .logsConfig(projectFields.get("logs-config") != null ? (LogsConfig) projectFields.get(
                "logs-config") : null)
            .description(getDescription())
            .serviceRole(getServiceRole() != null ? getServiceRole().getArn() : null)
            .environment(projectFields.get("environment") != null ? (ProjectEnvironment) projectFields.get(
                "environment") : null)
            .source(projectFields.get("source") != null ? (ProjectSource) projectFields.get(
                "source") : null)
            .artifacts(projectFields.get("artifacts") != null ? (ProjectArtifacts) projectFields.get(
                "artifacts") : null)
            .tags(projectFields.get("tags") != null ? (List<Tag>) projectFields.get(
                "tags") : null)
            .cache(projectFields.get("cache") != null ? (ProjectCache) projectFields.get("cache") : null)
        );
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

        CodeBuildClient client = createClient(CodeBuildClient.class);

        Map<String, Object> projectFields = getProjectFields();

        if (changedFieldNames.contains("badge-enabled")) {
            client.updateProject(r -> r.name(getName())
                .badgeEnabled(getBadge().getBadgeEnabled())
            );
        }

        if (changedFieldNames.contains("build-batch-config") && projectFields.get("build-batch-config") != null) {
            client.updateProject(r -> r.name(getName())
                .buildBatchConfig((ProjectBuildBatchConfig) projectFields.get("build-batch-config"))
            );
        }

        if (changedFieldNames.contains("logs-config") && projectFields.get("logs-config") != null) {
            client.updateProject(r -> r.name(getName())
                .logsConfig((LogsConfig) projectFields.get("logs-config"))
            );
        }

        if (changedFieldNames.contains("description")) {
            client.updateProject(r -> r.name(getName())
                .description(getDescription())
            );
        }

        if (changedFieldNames.contains("service-role")) {
            client.updateProject(r -> r.name(getName())
                .serviceRole(getServiceRole() != null ? getServiceRole().getArn() : null)
            );
        }

        if (changedFieldNames.contains("environment") && projectFields.get("environment") != null) {
            client.updateProject(r -> r.name(getName())
                .environment((ProjectEnvironment) projectFields.get("environment"))
            );
        }

        if (changedFieldNames.contains("source") && projectFields.get("source") != null) {
            client.updateProject(r -> r.name(getName())
                .source((ProjectSource) projectFields.get("source"))
            );
        }

        if (changedFieldNames.contains("artifacts") && projectFields.get("artifacts") != null) {
            client.updateProject(r -> r.name(getName())
                .artifacts((ProjectArtifacts) projectFields.get("artifacts"))
            );
        }

        if (changedFieldNames.contains("tags") && projectFields.get("tags") != null) {
            client.updateProject(r -> r.name(getName())
                .tags((List<Tag>) projectFields.get("tags"))
            );
        }

        if (changedFieldNames.contains("cache") && projectFields.get("cache") != null) {
            client.updateProject(r -> r.name(getName())
                .cache((ProjectCache) projectFields.get("cache"))
            );
        }

        if (changedFieldNames.contains("encryption-key")) {
            client.updateProject(r -> r.name(getName())
                .encryptionKey(getEncryptionKey())
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
        Project project = model.projects().get(0);

        setDescription(project.description());
        setName(project.name());
        setServiceRole(!ObjectUtils.isBlank(project.serviceRole())
            ? findById(RoleResource.class, project.serviceRole())
            : null);
        setEncryptionKey(project.encryptionKey());

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
    }

    private Map<String, Object> getProjectFields() {
        Map<String, Object> fields = new HashMap<>();

        BuildStatusConfig buildStatusConfig = BuildStatusConfig.builder()
            .context(source.getBuildStatusConfig().getContext())
            .targetUrl(source.getBuildStatusConfig().getTargetUrl())
            .build();

        GitSubmodulesConfig gitSubmodulesConfig = GitSubmodulesConfig.builder()
            .fetchSubmodules(source.getGitSubmodulesConfig().getFetchSubmodules()).build();

        CodebuildProjectSource source = getSource();
        ProjectSource projectSource = ProjectSource.builder()
            .type(source.getType())
            .location(source.getLocation())
            .buildspec(source.getBuildspec())
            .buildStatusConfig(buildStatusConfig)
            .gitCloneDepth(source.getGitCloneDepth())
            .gitSubmodulesConfig(gitSubmodulesConfig)
            .insecureSsl(source.getInsecureSsl())
            .reportBuildStatus(source.getReportBuildStatus())
            .sourceIdentifier(source.getSourceIdentifier())
            .build();

        CodebuildProjectArtifacts artifacts = getArtifacts();
        ProjectArtifacts projectArtifacts = ProjectArtifacts.builder()
            .type(artifacts.getType())
            .location(artifacts.getLocation())
            .name(artifacts.getName())
            .namespaceType(artifacts.getNamespaceType())
            .encryptionDisabled(artifacts.getEncryptionDisabled())
            .path(artifacts.getPath())
            .packaging(artifacts.getPackaging())
            .build();

        List<EnvironmentVariable> environmentVariables = new ArrayList<>();

        for (CodebuildProjectEnvironmentVariable var : environment.getEnvironmentVariables()) {
            environmentVariables.add(EnvironmentVariable.builder()
                .name(var.getName())
                .type(var.getType())
                .value(var.getValue())
                .build());
        }

        CodebuildProjectEnvironment environment = getEnvironment();
        ProjectEnvironment projectEnvironment = ProjectEnvironment.builder()
            .computeType(environment.getComputeType())
            .image(environment.getImage())
            .type(environment.getType())
            .certificate(environment.getCertificate())
            .environmentVariables(environmentVariables)
            .imagePullCredentialsType(environment.getImagePullCredentialsType())
            .privilegedMode(environment.getPrivalegedMode())
            .build();

        Map<String, String> tags = getTags();
        List<Tag> projectTags = new ArrayList<>();

        for (String key : tags.keySet()) {
            projectTags.add(Tag.builder()
                .key(key)
                .value(tags.get(key))
                .build());
        }

        BatchRestrictions batchRestrictions = BatchRestrictions.builder()
            .computeTypesAllowed(buildBatchConfig.getRestrictions().getComputedTypesAllowed())
            .maximumBuildsAllowed(buildBatchConfig.getRestrictions().getMaximumBuildsAllowed())
            .build();

        CodebuildProjectBuildBatchConfig buildBatchConfig = getBuildBatchConfig();
        ProjectBuildBatchConfig projectBuildBatchConfig = ProjectBuildBatchConfig.builder()
            .combineArtifacts(buildBatchConfig.getCombineArtifacts())
            .restrictions(batchRestrictions)
            .serviceRole(buildBatchConfig.getServiceRole())
            .timeoutInMins(buildBatchConfig.getTimeoutInMins())
            .build();

        CodebuildLogsConfig logsConfig = getLogsConfig();
        CodebuildCloudWatchLogsConfig cloudWatchLogs = logsConfig.getCloudWatchLogs();
        CodebuildS3LogsConfig s3Logs = logsConfig.getS3Logs();

        CloudWatchLogsConfig cloudWatchLogsConfig = CloudWatchLogsConfig.builder()
            .groupName(cloudWatchLogs.getGroupName())
            .status(cloudWatchLogs.getStatus())
            .streamName(cloudWatchLogs.getStreamName())
            .build();

        S3LogsConfig s3LogsConfig = S3LogsConfig.builder()
            .encryptionDisabled(s3Logs.getEncryptionDisabled())
            .location(s3Logs.getLocation())
            .status(s3Logs.getStatus())
            .build();

        LogsConfig projectLogsConfig = LogsConfig.builder()
            .cloudWatchLogs(cloudWatchLogsConfig)
            .s3Logs(s3LogsConfig)
            .build();

        CodebuildProjectCache cache = getCache();
        ProjectCache projectCache = ProjectCache.builder()
            .type(cache.getType())
            .location(cache.getLocation())
            .modesWithStrings(cache.getModes())
            .build();

        fields.put("source", projectSource);
        fields.put("artifacts", projectArtifacts);
        fields.put("environment", projectEnvironment);
        fields.put("tags", projectTags);
        fields.put("build-batch-config", projectBuildBatchConfig);
        fields.put("logs-config", projectLogsConfig);
        fields.put("cache", projectCache);

        return fields;
    }
}
