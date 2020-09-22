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

    @Required
    @Updatable
    @ValidStrings({ "CODEPIPELINE", "S3", "NO_ARTIFACTS" })
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Updatable
    public Boolean getEncryptionDisabled() {
        return encryptionDisabled;
    }

    public void setEncryptionDisabled(Boolean encryptionDisabled) {
        this.encryptionDisabled = encryptionDisabled;
    }

    @Updatable
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Updatable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Updatable
    @ValidStrings({ "NONE", "BUILD_ID" })
    public String getNamespaceType() {
        return namespaceType;
    }

    public void setNamespaceType(String namespaceType) {
        this.namespaceType = namespaceType;
    }

    public Boolean getOverrideArtifactName() {
        return overrideArtifactName;
    }

    public void setOverrideArtifactName(Boolean overrideArtifactName) {
        this.overrideArtifactName = overrideArtifactName;
    }

    @Updatable
    @ValidStrings({ "NONE", "ZIP" })
    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    @Updatable
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

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
