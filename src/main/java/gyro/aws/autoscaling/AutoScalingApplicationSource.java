package gyro.aws.autoscaling;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.CollectionMax;
import gyro.core.validation.Regex;
import software.amazon.awssdk.services.autoscalingplans.model.ApplicationSource;

public class AutoScalingApplicationSource extends Diffable implements Copyable<ApplicationSource> {

    private String cloudFormationStackArn;
    private List<AutoScalingTagFilter> tagFilters;

    /**
     * The Amazon Resource Name of a CloudFormation stack.
     */
    @Updatable
    @Regex(value = "[\\u0020-\\uD7FF\\uE000-\\uFFFD\\uD800\\uDC00-\\uDBFF\\uDFFF\\r\\n\\t]*", message = "Alphanumeric characters and symbols excluding basic ASCII control characters.")
    public String getCloudFormationStackArn() {
        return cloudFormationStackArn;
    }

    public void setCloudFormationStackArn(String cloudFormationStackArn) {
        this.cloudFormationStackArn = cloudFormationStackArn;
    }

    /**
     * The tags for the application source.
     */
    @Updatable
    @CollectionMax(50)
    public List<AutoScalingTagFilter> getTagFilters() {
        if (tagFilters == null) {
            tagFilters = new ArrayList<>();
        }
        return tagFilters;
    }

    public void setTagFilters(List<AutoScalingTagFilter> tagFilters) {
        this.tagFilters = tagFilters;
    }

    @Override
    public void copyFrom(ApplicationSource model) {
        setCloudFormationStackArn(model.cloudFormationStackARN());

        getTagFilters().clear();
        if (model.tagFilters() != null) {
            model.tagFilters().forEach(filter -> {
                AutoScalingTagFilter tagFilter = newSubresource(AutoScalingTagFilter.class);
                tagFilter.copyFrom(filter);
                getTagFilters().add(tagFilter);
            });
        } else {
            setTagFilters(null);
        }
    }

    @Override
    public String primaryKey() {
        return null;
    }

    public ApplicationSource toApplicationSource() {
        return ApplicationSource.builder()
            .cloudFormationStackARN(getCloudFormationStackArn())
            .tagFilters(getTagFilters().stream()
                .map(AutoScalingTagFilter::toTagFilter)
                .collect(Collectors.toList()))
            .build();
    }
}
