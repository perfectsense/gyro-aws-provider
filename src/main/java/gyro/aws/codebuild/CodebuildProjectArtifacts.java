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

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.codebuild.model.ArtifactNamespace;
import software.amazon.awssdk.services.codebuild.model.ArtifactPackaging;
import software.amazon.awssdk.services.codebuild.model.ArtifactsType;
import software.amazon.awssdk.services.codebuild.model.ProjectArtifacts;

public class CodebuildProjectArtifacts extends Diffable implements Copyable<ProjectArtifacts> {

    private ArtifactsType type;
    private Boolean encryptionDisabled;
    private String location;
    private String name;
    private ArtifactNamespace namespaceType;
    private Boolean overrideArtifactName;
    private ArtifactPackaging packaging;
    private String path;

    // Read-only
    private String artifactIdentifier;

    /**
     * The type of the build output artifact.
     */
    @Required
    @Updatable
    @ValidStrings({ "CODEPIPELINE", "S3", "NO_ARTIFACTS" })
    public ArtifactsType getType() {
        return type;
    }

    public void setType(ArtifactsType type) {
        this.type = type;
    }

    /**
     * When set to ``true`` the output artifacts are not encrypted.
     */
    @Updatable
    public Boolean getEncryptionDisabled() {
        return encryptionDisabled;
    }

    public void setEncryptionDisabled(Boolean encryptionDisabled) {
        this.encryptionDisabled = encryptionDisabled;
    }

    /**
     * The build output artifact location.
     */
    @Updatable
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * The pattern that is used to name and store the output artifact.
     */
    @Updatable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The type that is used to determine the name and location to store the output artifact.
     */
    @Updatable
    @ValidStrings({ "NONE", "BUILD_ID" })
    public ArtifactNamespace getNamespaceType() {
        return namespaceType;
    }

    public void setNamespaceType(ArtifactNamespace namespaceType) {
        this.namespaceType = namespaceType;
    }

    /**
     * When set to ``true`` the name specified in the buildspec file overrides the artifact name.
     */
    public Boolean getOverrideArtifactName() {
        return overrideArtifactName;
    }

    public void setOverrideArtifactName(Boolean overrideArtifactName) {
        this.overrideArtifactName = overrideArtifactName;
    }

    /**
     * The type of build output artifact to create.
     */
    @Updatable
    @ValidStrings({ "NONE", "ZIP" })
    public ArtifactPackaging getPackaging() {
        return packaging;
    }

    public void setPackaging(ArtifactPackaging packaging) {
        this.packaging = packaging;
    }

    /**
     * The path that is used to name and store the output artifact.
     */
    @Updatable
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * The identifier for this artifact definition.
     */
    @Output
    public String getArtifactIdentifier() {
        return artifactIdentifier;
    }

    public void setArtifactIdentifier(String artifactIdentifier) {
        this.artifactIdentifier = artifactIdentifier;
    }

    @Override
    public void copyFrom(ProjectArtifacts model) {
        setArtifactIdentifier(model.artifactIdentifier());
        setEncryptionDisabled(model.encryptionDisabled());
        setLocation(model.location());
        setName(model.name());
        setNamespaceType(model.namespaceType());
        setOverrideArtifactName(model.overrideArtifactName());
        setPackaging(model.packaging());
        setPath(model.path());
        setType(model.type());
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();
        String type = getType().toString();

        if (!ObjectUtils.isBlank(getLocation()) && type.equals("S3")) {
            errors.add(new ValidationError(
                this,
                null,
                "'location' cannot be empty if 'type' is set to 'S3'."));
        }

        if (!ObjectUtils.isBlank(getName()) && type.equals("S3")) {
            errors.add(new ValidationError(
                this,
                null,
                "'name' cannot be empty if 'type' is 'S3'. "));

        }

        if (getEncryptionDisabled() != null && !getEncryptionDisabled() && !type.equals("S3")) {
            errors.add(new ValidationError(
                this,
                null,
                "'encryption-disabled' can only be set if 'type' is set to 'S3'."));
        }

        return errors;
    }

    public ProjectArtifacts toProjectArtifacts() {
        return ProjectArtifacts.builder()
            .type(getType())
            .location(getLocation())
            .name(getName())
            .namespaceType(getNamespaceType())
            .encryptionDisabled(getEncryptionDisabled())
            .path(getPath())
            .packaging(getPackaging())
            .build();
    }
}
