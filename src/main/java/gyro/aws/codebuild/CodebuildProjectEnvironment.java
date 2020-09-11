package gyro.aws.codebuild;

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.codebuild.model.EnvironmentVariable;
import software.amazon.awssdk.services.codebuild.model.ProjectEnvironment;

public class CodebuildProjectEnvironment extends Diffable implements Copyable<ProjectEnvironment> {

    private String computeType;
    private String image;
    private String type;

    private String certificate;
    private List<CodebuildProjectEnvironmentVariable> environmentVariables;
    private String imagePullCredentialsType;
    private Boolean privalegedMode;

    @Updatable
    public String getComputeType() {
        return computeType;
    }

    public void setComputeType(String computeType) {
        this.computeType = computeType;
    }

    @Updatable
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Updatable
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Updatable
    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    @Updatable
    public List<CodebuildProjectEnvironmentVariable> getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setEnvironmentVariables(List<CodebuildProjectEnvironmentVariable> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    @Updatable
    public String getImagePullCredentialsType() {
        return imagePullCredentialsType;
    }

    public void setImagePullCredentialsType(String imagePullCredentialsType) {
        this.imagePullCredentialsType = imagePullCredentialsType;
    }

    @Updatable
    public Boolean getPrivalegedMode() {
        return privalegedMode;
    }

    public void setPrivalegedMode(Boolean privalegedMode) {
        this.privalegedMode = privalegedMode;
    }

    @Override
    public void copyFrom(ProjectEnvironment model) {
        setComputeType(model.computeTypeAsString());
        setImage(model.image());
        setType(model.typeAsString());
        setCertificate(model.certificate());
        setImagePullCredentialsType(model.imagePullCredentialsTypeAsString());
        setPrivalegedMode(model.privilegedMode());

        if (model.environmentVariables() != null) {
            List<CodebuildProjectEnvironmentVariable> environmentVariables = new ArrayList<>();

            CodebuildProjectEnvironmentVariable environmentVariable = newSubresource(
                CodebuildProjectEnvironmentVariable.class);

            for (EnvironmentVariable variable : model.environmentVariables()) {
                environmentVariable.copyFrom(variable);
                environmentVariables.add(environmentVariable);
            }
        }

        setEnvironmentVariables(getEnvironmentVariables());
    }

    @Override
    public String primaryKey() {
        return null;
    }
}
