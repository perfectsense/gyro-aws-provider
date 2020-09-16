package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.codebuild.model.ProjectBadge;

public class CodebuildProjectBadge extends Diffable implements Copyable<ProjectBadge> {

    private Boolean badgeEnabled;
    private String badgeRequestUrl;

    @Updatable
    public Boolean getBadgeEnabled() {
        return badgeEnabled;
    }

    public void setBadgeEnabled(Boolean badgeEnabled) {
        this.badgeEnabled = badgeEnabled;
    }

    @Output
    public String getBadgeRequestUrl() {
        return badgeRequestUrl;
    }

    public void setBadgeRequestUrl(String badgeRequestUrl) {
        this.badgeRequestUrl = badgeRequestUrl;
    }

    @Override
    public void copyFrom(ProjectBadge model) {
        setBadgeEnabled(model.badgeEnabled());
        setBadgeRequestUrl(model.badgeRequestUrl());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
