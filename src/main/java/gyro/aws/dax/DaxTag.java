package gyro.aws.dax;

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.dax.model.Tag;

public class DaxTag extends Diffable implements Copyable<Tag> {

    private String key;
    private String value;

    @Required
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
        return String.format("%s", getKey());
    }

    public static List<Tag> toTags(List<DaxTag> daxTags) {
        List<Tag> tags = new ArrayList<>();

        for (DaxTag t : daxTags) {
            tags.add(Tag.builder()
                .key(t.key)
                .value(t.value)
                .build()
            );
        }

        return tags;
    }
}
