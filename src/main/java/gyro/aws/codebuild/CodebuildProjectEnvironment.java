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
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.codebuild.model.EnvironmentVariable;
import software.amazon.awssdk.services.codebuild.model.ProjectEnvironment;

public class CodebuildProjectEnvironment extends Diffable implements Copyable<ProjectEnvironment> {

    private String certificate;
    private String computeType;
    private List<CodebuildProjectEnvironmentVariable> environmentVariables;
    private String image;
    private String imagePullCredentialsType;
    private Boolean privalegedMode;
    private CodebuildRegistryCredential registryCredential;
    private String type;

    /**
     * The compute resources used by the build project.
     */
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

    /**
     * The image tag or image digest that identifies the Docker image used for the build project.
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
     * The type of build environment used for related builds.
     */
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
     */
    @Updatable
    public List<CodebuildProjectEnvironmentVariable> getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setEnvironmentVariables(List<CodebuildProjectEnvironmentVariable> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    /**
     * The type of credentials the build project uses to pull images in the build.
     */
    @Updatable
    @ValidStrings({ "CODEBUILD", "SERVICE_ROLE" })
    public String getImagePullCredentialsType() {
        return imagePullCredentialsType;
    }

    public void setImagePullCredentialsType(String imagePullCredentialsType) {
        this.imagePullCredentialsType = imagePullCredentialsType;
    }

    /**
     * The field that specifies running the Docker daemon inside a Docker container.
     */
    @Updatable
    public Boolean getPrivalegedMode() {
        return privalegedMode;
    }

    public void setPrivalegedMode(Boolean privalegedMode) {
        this.privalegedMode = privalegedMode;
    }

    /**
     * The credentials for access to a private registry.
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
