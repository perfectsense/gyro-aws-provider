package gyro.aws.codebuild;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.codebuild.model.Tag;

public class CodebuildProjectTag extends Diffable implements Copyable<Tag> {

    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void copyFrom(Tag model) {
        setKey(model.key());
        setValue(model.value());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public static List<Tag> toProjectTags(Map<String, String> tags) {
        List<Tag> projectTags = new ArrayList<>();

        for (String key : tags.keySet()) {
            projectTags.add(Tag.builder()
                .key(key)
                .value(tags.get(key))
                .build());
        }

        return projectTags;
    }
}
