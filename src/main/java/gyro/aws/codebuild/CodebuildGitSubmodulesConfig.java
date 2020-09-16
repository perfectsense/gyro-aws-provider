package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.codebuild.model.GitSubmodulesConfig;

public class CodebuildGitSubmodulesConfig extends Diffable implements Copyable<GitSubmodulesConfig> {

    private Boolean fetchSubmodules;

    @Updatable
    public Boolean getFetchSubmodules() {
        return fetchSubmodules;
    }

    public void setFetchSubmodules(Boolean fetchSubmodules) {
        this.fetchSubmodules = fetchSubmodules;
    }

    @Override
    public void copyFrom(GitSubmodulesConfig model) {
        setFetchSubmodules(model.fetchSubmodules());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
