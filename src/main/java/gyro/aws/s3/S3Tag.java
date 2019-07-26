package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.s3.model.Tag;

public class S3Tag extends Diffable implements Copyable<Tag> {
    private String key;
    private String value;

    /**
     * The tags key value. (Required)
     */
    @Updatable
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * The value for the tag. (Required)
     */
    @Updatable
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void copyFrom(Tag tag) {
        setKey(tag.key());
        setValue(tag.value());
    }

    @Override
    public String primaryKey() {
        return getKey() + " - " + getValue();
    }

    Tag toTag(){
        return Tag.builder()
                .key(getKey())
                .value(getValue())
                .build();
    }
}