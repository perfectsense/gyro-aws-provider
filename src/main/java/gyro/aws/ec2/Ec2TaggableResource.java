package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.core.resource.Updatable;
import gyro.core.resource.Resource;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.psddev.dari.util.CompactMap;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.ec2.model.TagDescription;
import software.amazon.awssdk.services.ec2.paginators.DescribeTagsIterable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Ec2TaggableResource<T> extends AwsResource {

    private static final String NAME_KEY = "Name";

    private Map<String, String> tags;

    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new CompactMap<>();
        }
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        if (this.tags != null && tags != null) {
            this.tags.putAll(tags);

        } else {
            this.tags = tags;
        }
    }

    @Updatable
    public String getName() {
        return getTags().get(NAME_KEY);
    }

    public void setName(String name) {
        if (name != null) {
            getTags().put(NAME_KEY, name);

        } else {
            getTags().remove(NAME_KEY);
        }
    }

    protected abstract String getId();

    protected boolean doRefresh() {
        return true;
    }

    private Map<String, String> loadTags() {
        Ec2Client client = createClient(Ec2Client.class);

        Map<String, String> tags = new HashMap<>();

        DescribeTagsIterable response = client.describeTagsPaginator(
            r -> r.filters(
                f -> f.name("resource-id")
                    .values(getId())
                    .build())
                .build());

        for (TagDescription tagDescription : response.tags()) {
            if (!tagDescription.key().startsWith("aws:")) {
                tags.put(tagDescription.key(), tagDescription.value());
            }
        }

        return tags;
    }

    @Override
    public final boolean refresh() {
        boolean refreshed = doRefresh();

        getTags().clear();
        getTags().putAll(loadTags());

        return refreshed;
    }

    protected void doAfterCreate() {
    }

    @Override
    public final void afterCreate() {
        doAfterCreate();
        createTags();
    }

    protected abstract void doUpdate(AwsResource config, Set<String> changedProperties);

    @Override
    public final void update(Resource current, Set<String> changedFieldNames) {
        doUpdate((AwsResource) current, changedFieldNames);
        createTags();
    }

    protected List<Filter> queryFilters(Set<String> filterableFields, Map<String, String> query) {
        List<Filter> apiFilters = new ArrayList<>();
        for (String key : query.keySet()) {
            if (!filterableFields.contains(key)) {
                // Unable to filter using this key.
                return null;
            }

            Filter filter = Filter.builder()
                .name(key)
                .values(query.get(key))
                .build();

            apiFilters.add(filter);
        }

        return apiFilters;
    }

    private void createTags() {
        Ec2Client client = createClient(Ec2Client.class);

        Map<String, String> pendingTags = getTags();
        Map<String, String> currentTags = loadTags();

        MapDifference<String, String> diff = Maps.difference(currentTags, pendingTags);

        // Remove tags
        if (!diff.entriesOnlyOnLeft().isEmpty()) {
            
            List<Tag> tagObjects = new ArrayList<>();
            for (Map.Entry<String, String> entry : diff.entriesOnlyOnLeft().entrySet()) {
                tagObjects.add(Tag.builder().key(entry.getKey()).value(entry.getValue()).build());
            }

            executeService(() -> {
                client.deleteTags(r -> r.resources(getId()).tags(tagObjects));
                return null;
            });
        }

        // Add tags
        if (!diff.entriesOnlyOnRight().isEmpty()) {
            List<Tag> tagObjects = new ArrayList<>();
            for (Map.Entry<String, String> entry : diff.entriesOnlyOnRight().entrySet()) {
                tagObjects.add(Tag.builder().key(entry.getKey()).value(entry.getValue()).build());
            }

            executeService(() -> {
                client.createTags(r -> r.resources(getId()).tags(tagObjects));
                return null;
            });
        }

        // Changed tags
        if (!diff.entriesDiffering().isEmpty()) {
            List<Tag> tagObjects = new ArrayList<>();

            for (Map.Entry<String, MapDifference.ValueDifference<String>> entry : diff.entriesDiffering().entrySet()) {
                tagObjects.add(Tag.builder().key(entry.getKey()).value(entry.getValue().rightValue()).build());
            }

            executeService(() -> {
                client.createTags(r -> r.resources(getId()).tags(tagObjects));
                return null;
            });
        }
    }
}
