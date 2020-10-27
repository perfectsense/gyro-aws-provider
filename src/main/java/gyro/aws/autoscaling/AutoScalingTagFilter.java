package gyro.aws.autoscaling;

import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Range;
import gyro.core.validation.Regex;
import software.amazon.awssdk.services.autoscalingplans.model.TagFilter;

public class AutoScalingTagFilter extends Diffable implements Copyable<TagFilter> {

    private String key;
    private List<String> values;

    /**
     * The tag key.
     */
    @Regex(value = "[\\u0020-\\uD7FF\\uE000-\\uFFFD\\uD800\\uDC00-\\uDBFF\\uDFFF\\r\\n\\t]*", message = "Alphanumeric characters and symbols excluding basic ASCII control characters.")
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * The tag values.
     */
    @Range(min = 0, max = 20)
    @Regex(value = "[\\u0020-\\uD7FF\\uE000-\\uFFFD\\uD800\\uDC00-\\uDBFF\\uDFFF\\r\\n\\t]*", message = "Alphanumeric characters and symbols excluding basic ASCII control characters.")
    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public void copyFrom(TagFilter model) {
        setKey(model.key());
        setValues(model.values());
    }

    @Override
    public String primaryKey() {
        return null;
    }

    public TagFilter toTagFilter() {
        return TagFilter.builder()
            .key(getKey())
            .values(getValues())
            .build();
    }
}
