package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
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

    @Updatable
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
        return "project artifacts";
    }
}
