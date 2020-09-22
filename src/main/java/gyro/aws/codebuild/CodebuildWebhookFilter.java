package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.codebuild.model.WebhookFilter;

public class CodebuildWebhookFilter extends Diffable implements Copyable<WebhookFilter> {

    private String pattern;
    private String type;
    private Boolean excludeMatchedPattern;

    @Updatable
    @Required
    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Updatable
    @Required
    @ValidStrings({"EVENT", "BASE_REF", "HEAD_REF", "ACTOR_ACCOUNT_ID", "FILE_PATH", "COMMIT_MESSAGE"})
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Updatable
    public Boolean getExcludeMatchedPattern() {
        return excludeMatchedPattern;
    }

    public void setExcludeMatchedPattern(Boolean excludeMatchedPattern) {
        this.excludeMatchedPattern = excludeMatchedPattern;
    }

    @Override
    public void copyFrom(WebhookFilter model) {
        setPattern(model.pattern());
        setType(model.typeAsString());
        setExcludeMatchedPattern(model.excludeMatchedPattern());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
