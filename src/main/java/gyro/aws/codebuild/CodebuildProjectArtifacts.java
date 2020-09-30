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
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.codebuild.model.ProjectArtifacts;

public class CodebuildProjectArtifacts extends Diffable implements Copyable<ProjectArtifacts> {

    private String type;
    private Boolean encryptionDisabled;
    private String location;
    private String name;
    private String namespaceType;
    private Boolean overrideArtifactName;
    private String packaging;
    private String path;

    // Read-only
    private String artifactIdentifier;

    /**
     * the type of the build output artifact.
     */
    @Required
    @Updatable
    @ValidStrings({ "CODEPIPELINE", "S3", "NO_ARTIFACTS" })
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The field that specifies for the output artifacts to be encrypted.
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
     * The pattern that is used to determine the name and location to store the output artifact.
     */
    @Updatable
    @ValidStrings({ "NONE", "BUILD_ID" })
    public String getNamespaceType() {
        return namespaceType;
    }

    public void setNamespaceType(String namespaceType) {
        this.namespaceType = namespaceType;
    }

    /**
     * The field that specifies if a name specified in the buildspec file overrides the artifact name.
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
    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    /**
     * The pattern that is used to name and store the output artifact.
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
        setNamespaceType(model.namespaceTypeAsString());
        setOverrideArtifactName(model.overrideArtifactName());
        setPackaging(model.packagingAsString());
        setPath(model.path());
        setType(model.typeAsString());

    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getLocation() == null && getType().equals("S3")) {
            errors.add(new ValidationError(
                this,
                null,
                "'location' cannot be empty if 'type' is 'S3'. Needs to specify the name of the output bucket."));
        }

        if (getName() == null && getType().equals("S3")) {
            errors.add(new ValidationError(
                this,
                null,
                "'name' cannot be empty if 'type' is 'S3'. Needs to specify the name of the output artifact object."));
        }

        if (getEncryptionDisabled() != null && !getType().equals("S3")) {
            errors.add(new ValidationError(
                this,
                null,
                "'encryption-disabled' option is valid only if 'type' is 'S3'."));
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
