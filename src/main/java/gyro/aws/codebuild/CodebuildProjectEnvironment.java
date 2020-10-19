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
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.codebuild.model.ComputeType;
import software.amazon.awssdk.services.codebuild.model.EnvironmentType;
import software.amazon.awssdk.services.codebuild.model.EnvironmentVariable;
import software.amazon.awssdk.services.codebuild.model.ImagePullCredentialsType;
import software.amazon.awssdk.services.codebuild.model.ProjectEnvironment;

public class CodebuildProjectEnvironment extends Diffable implements Copyable<ProjectEnvironment> {

    private String certificate;
    private ComputeType computeType;
    private List<CodebuildProjectEnvironmentVariable> environmentVariables;
    private String image;
    private ImagePullCredentialsType imagePullCredentialsType;
    private Boolean privilegedMode;
    private CodebuildRegistryCredential registryCredential;
    private EnvironmentType type;

    /**
     * The compute resources used by the build project. Valid values are ``BUILD_GENERAL1_SMALL``,
     * ``BUILD_GENERAL1_MEDIUM``, ``BUILD_GENERAL1_LARGE``, or ``BUILD_GENERAL1_2XLARGE``. (Required)
     */
    @Updatable
    @Required
    public ComputeType getComputeType() {
        return computeType;
    }

    public void setComputeType(ComputeType computeType) {
        this.computeType = computeType;
    }

    /**
     * The image tag or image digest that identifies the Docker image used for the build project. (Required)
     */
    @Updatable
    @Required
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    /**
     * The type of build environment used for related builds. Valid values are ``WINDOWS_CONTAINER``,
     * ``LINUX_CONTAINER``, ``LINUX_GPU_CONTAINER``, ``ARM_CONTAINER``, or ``WINDOWS_SERVER_2019_CONTAINER``.
     * (Required)
     */
    @Updatable
    @Required
    public EnvironmentType getType() {
        return type;
    }

    public void setType(EnvironmentType type) {
        this.type = type;
    }

    /**
     * The certificate used with the build project.
     */
    @Updatable
    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    /**
     * The list of environment variables available to builds for the build project.
     *
     * @subresource gyro.aws.codebuild.CodebuildProjectEnvironmentVariable
     */
    @Updatable
    public List<CodebuildProjectEnvironmentVariable> getEnvironmentVariables() {
        if (environmentVariables == null) {
            environmentVariables = new ArrayList<>();
        }

        return environmentVariables;
    }

    public void setEnvironmentVariables(List<CodebuildProjectEnvironmentVariable> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    /**
     * The type of credentials the build project uses to pull images in the build. Valid values are ``CODEBUILD``,
     * ``SERVICE_ROLE``.
     */
    @Updatable
    public ImagePullCredentialsType getImagePullCredentialsType() {
        return imagePullCredentialsType;
    }

    public void setImagePullCredentialsType(ImagePullCredentialsType imagePullCredentialsType) {
        this.imagePullCredentialsType = imagePullCredentialsType;
    }

    /**
     * When set to ``true`` the Docker daemon is run inside a Docker container. When set to ``false`` the Docker daemon
     * is run outside a Docker container.
     */
    @Updatable
    public Boolean getPrivilegedMode() {
        return privilegedMode;
    }

    public void setPrivilegedMode(Boolean privilegedMode) {
        this.privilegedMode = privilegedMode;
    }

    /**
     * The credentials for access to a private registry.
     *
     * @subresource gyro.aws.codebuild.CodebuildRegistryCredential
     */
    @Updatable
    public CodebuildRegistryCredential getRegistryCredential() {
        return registryCredential;
    }

    public void setRegistryCredential(CodebuildRegistryCredential registryCredential) {
        this.registryCredential = registryCredential;
    }

    @Override
    public void copyFrom(ProjectEnvironment model) {
        setComputeType(model.computeType());
        setImage(model.image());
        setType(model.type());
        setCertificate(model.certificate());
        setImagePullCredentialsType(model.imagePullCredentialsType());
        setPrivilegedMode(model.privilegedMode());

        getEnvironmentVariables().clear();
        if (model.environmentVariables() != null) {
            model.environmentVariables().forEach(variable -> {
                CodebuildProjectEnvironmentVariable environmentVariable = newSubresource(
                    CodebuildProjectEnvironmentVariable.class);
                environmentVariable.copyFrom(variable);
                getEnvironmentVariables().add(environmentVariable);
            });
        }

        if (model.registryCredential() != null) {
            CodebuildRegistryCredential registryCredential = newSubresource(CodebuildRegistryCredential.class);
            registryCredential.copyFrom(model.registryCredential());
            setRegistryCredential(registryCredential);
        } else {
            setRegistryCredential(null);
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getRegistryCredential() != null && getImagePullCredentialsType() != null
            && !getImagePullCredentialsType().equals("SERVICE_ROLE")) {
            errors.add(new ValidationError(
                this,
                "registry-credential",
                "'registry-credential' can only be set if 'image-pull-credentials-type' is set to 'SERVICE_ROLE'."
            ));
        }

        return errors;
    }

    public ProjectEnvironment toProjectEnvironment() {
        List<EnvironmentVariable> environmentVariables = new ArrayList<>();

        for (CodebuildProjectEnvironmentVariable var : getEnvironmentVariables()) {
            environmentVariables.add(var.toEnvironmentVariable());
        }

        return ProjectEnvironment.builder()
            .computeType(getComputeType())
            .image(getImage())
            .type(getType())
            .certificate(getCertificate())
            .environmentVariables(getEnvironmentVariables().stream()
                .map(CodebuildProjectEnvironmentVariable::toEnvironmentVariable)
                .collect(Collectors.toList()))
            .imagePullCredentialsType(getImagePullCredentialsType())
            .privilegedMode(getPrivilegedMode())
            .registryCredential(getRegistryCredential() != null ? getRegistryCredential().toRegistryCredential() : null)
            .build();
    }
}
