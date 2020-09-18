package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.codebuild.model.BuildStatusConfig;

public class CodebuildBuildStatusConfig extends Diffable implements Copyable<BuildStatusConfig> {

    private String context;
    private String targetUrl;

    @Updatable
    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Updatable
    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    @Override
    public void copyFrom(BuildStatusConfig model) {
        setContext(model.context());
        setTargetUrl(model.targetUrl());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public BuildStatusConfig toBuildStatusConfig() {
        return BuildStatusConfig.builder()
            .context(getContext())
            .targetUrl(getTargetUrl())
            .build();
    }
}
