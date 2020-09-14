package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.codebuild.model.WebhookFilter;

public class CodebuildWebhookFilter extends Diffable implements Copyable<WebhookFilter> {

    private String pattern;
    private String type;
    private Boolean excludeMatchedPattern;

    @Updatable
    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Updatable
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
        return null;
    }
}
