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
    private CodebuildRegistryCredential registryCredential;

    @Updatable
    @Required
    @ValidStrings({
        "BUILD_GENERAL1_SMALL",
        "BUILD_GENERAL1_MEDIUM",
        "BUILD_GENERAL1_LARGE",
        "BUILD_GENERAL1_2XLARGE" })
    public String getComputeType() {
        return computeType;
    }

    public void setComputeType(String computeType) {
        this.computeType = computeType;
    }

    @Updatable
    @Required
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Updatable
    @Required
    @ValidStrings({
        "WINDOWS_CONTAINER",
        "LINUX_CONTAINER",
        "LINUX_GPU_CONTAINER",
        "ARM_CONTAINER",
        "WINDOWS_SERVER_2019_CONTAINER" })
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
    @ValidStrings({ "CODEBUILD", "SERVICE_ROLE" })
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

    @Updatable
    public CodebuildRegistryCredential getRegistryCredential() {
        return registryCredential;
    }

    public void setRegistryCredential(CodebuildRegistryCredential registryCredential) {
        this.registryCredential = registryCredential;
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

            setEnvironmentVariables(environmentVariables);
        }

        if (model.registryCredential() != null) {
            CodebuildRegistryCredential registryCredential = newSubresource(CodebuildRegistryCredential.class);
            registryCredential.copyFrom(model.registryCredential());
            setRegistryCredential(registryCredential);
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getRegistryCredential() != null && !getImagePullCredentialsType().equals("SERVICE_ROLE")) {
            errors.add(new ValidationError(
                this,
                null,
                "'image-pull-credentials-type' must be set to 'SERVICE_ROLE' to set 'registry-credential'."
            ));
        }

        return errors;
    }

    public ProjectEnvironment toProjectEnvironment() {
        List<EnvironmentVariable> environmentVariables = new ArrayList<>();

        for (CodebuildProjectEnvironmentVariable var : getEnvironmentVariables()) {
            environmentVariables.add(EnvironmentVariable.builder()
                .name(var.getName())
                .type(var.getType())
                .value(var.getValue())
                .build());
        }

        return ProjectEnvironment.builder()
            .computeType(getComputeType())
            .image(getImage())
            .type(getType())
            .certificate(getCertificate())
            .environmentVariables(environmentVariables)
            .imagePullCredentialsType(getImagePullCredentialsType())
            .privilegedMode(getPrivalegedMode())
            .registryCredential(getRegistryCredential().toRegistryCredential())
            .build();
    }
}
