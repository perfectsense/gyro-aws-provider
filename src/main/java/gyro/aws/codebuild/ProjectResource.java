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
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.codebuild.CodeBuildClient;
import software.amazon.awssdk.services.codebuild.model.BatchGetProjectsResponse;
import software.amazon.awssdk.services.codebuild.model.Project;
import software.amazon.awssdk.services.codebuild.model.ProjectArtifacts;
import software.amazon.awssdk.services.codebuild.model.ProjectEnvironment;
import software.amazon.awssdk.services.codebuild.model.ProjectSource;
import software.amazon.awssdk.services.codebuild.model.ResourceNotFoundException;
import software.amazon.awssdk.services.codebuild.model.Tag;
@Type("project")
public class ProjectResource extends AwsResource implements Copyable<BatchGetProjectsResponse> {

    private CodebuildProjectArtifacts artifacts;
    private CodebuildProjectEnvironment environment;
    private String name;
    private RoleResource serviceRole;
    private CodebuildProjectSource source;

    @Updatable
    public CodebuildProjectArtifacts getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(CodebuildProjectArtifacts artifacts) {
        this.artifacts = artifacts;
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

        CodebuildProjectSource codebuildProjectSource = getSource();
        ProjectSource projectSource = ProjectSource.builder()
            .type(codebuildProjectSource.getType())
            .location(codebuildProjectSource.getLocation())
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

        CodebuildProjectEnvironment environment = getEnvironment();
        ProjectEnvironment projectEnvironment = ProjectEnvironment.builder()
            .computeType(environment.getComputeType())
            .image(environment.getImage())
            .type(environment.getType())
            .build();

        client.createProject(r -> r
                .name(getName())
                .serviceRole(getServiceRole() != null ? getServiceRole().getArn() : null)
                .source(projectSource)
                .artifacts(projectArtifacts)
                .environment(projectEnvironment)
        );
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

        CodeBuildClient client = createClient(CodeBuildClient.class);

        CodebuildProjectSource source = getSource();
        ProjectSource projectSource = ProjectSource.builder()
            .type(source.getType())
            .location(source.getLocation())
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

        CodebuildProjectEnvironment environment = getEnvironment();
        ProjectEnvironment projectEnvironment = ProjectEnvironment.builder()
            .computeType(environment.getComputeType())
            .image(environment.getImage())
            .type(environment.getType())
            .build();

        client.updateProject(r -> r
            .name(getName())
            .serviceRole(getServiceRole() != null ? getServiceRole().getArn() : null)
            .environment(projectEnvironment)
            .source(projectSource)
            .artifacts(projectArtifacts)
        );
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        CodeBuildClient client = createClient(CodeBuildClient.class);

        client.deleteProject(r -> r.name(getName()));
    }

    @Override
    public void copyFrom(BatchGetProjectsResponse model) {
        Project project = model.projects().get(0);

        setName(project.name());
        setServiceRole(!ObjectUtils.isBlank(project.serviceRole())
            ? findById(RoleResource.class, project.serviceRole())
            : null);

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

        }

    }
}
