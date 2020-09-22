package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.codebuild.model.ProjectSourceVersion;

public class CodebuildProjectSourceVersion extends Diffable implements Copyable<ProjectSourceVersion> {

    private String sourceIdentifier;
    private String sourceVersion;

    @Updatable
    public String getSourceIdentifier() {
        return sourceIdentifier;
    }

    public void setSourceIdentifier(String sourceIdentifier) {
        this.sourceIdentifier = sourceIdentifier;
    }

    @Updatable
    public String getSourceVersion() {
        return sourceVersion;
    }

    public void setSourceVersion(String sourceVersion) {
        this.sourceVersion = sourceVersion;
    }

    @Override
    public void copyFrom(ProjectSourceVersion model) {
        setSourceIdentifier(model.sourceIdentifier());
        setSourceVersion(model.sourceVersion());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
