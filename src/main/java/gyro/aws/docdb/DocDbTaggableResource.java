package gyro.aws.docdb;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import gyro.aws.AwsResource;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.docdb.DocDbClient;
import software.amazon.awssdk.services.docdb.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.docdb.model.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class DocDbTaggableResource<T> extends AwsResource {
    private Map<String, String> tags;

    protected abstract String getId();

    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    protected abstract boolean doRefresh();

    @Override
    public boolean refresh() {
        boolean refreshed = doRefresh();

        if (refreshed) {
            loadTags();
        }

        return refreshed;
    }

    protected abstract void doCreate();

    @Override
    public void create() {
        doCreate();
        saveTags();
    }

    protected abstract void doUpdate(Resource current, Set<String> changedProperties);

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        doUpdate(current, changedProperties);
        saveTags();
    }

    private void saveTags() {
        Map<String, String> pendingTags = getTags();
        Map<String, String> currentTags = getDocDbTags();

        MapDifference<String, String> diff = Maps.difference(currentTags, pendingTags);

        DocDbClient client = createClient(DocDbClient.class);

        if (!diff.entriesOnlyOnLeft().isEmpty()) {
            client.removeTagsFromResource(
                r -> r.resourceName(getId())
                    .tagKeys(diff.entriesOnlyOnLeft().keySet())
            );
        }

        if (!diff.entriesOnlyOnRight().isEmpty()) {
            client.addTagsToResource(
                r -> r.resourceName(getId())
                    .tags(toDocDbTags(diff.entriesOnlyOnRight()))
            );
        }

        if (!diff.entriesDiffering().isEmpty()) {
            client.removeTagsFromResource(
                r -> r.resourceName(getId())
                    .tagKeys(diff.entriesDiffering().keySet())
            );

            Map<String, String> addTags = new HashMap<>();
            diff.entriesDiffering().keySet().forEach(o -> addTags.put(o, diff.entriesDiffering().get(o).rightValue()));

            client.addTagsToResource(
                r -> r.resourceName(getId())
                    .tags(toDocDbTags(addTags))
            );
        }
    }

    protected void loadTags() {
        setTags(getDocDbTags());
    }

    private Map<String, String> getDocDbTags() {
        DocDbClient client = createClient(DocDbClient.class);

        ListTagsForResourceResponse response = client.listTagsForResource(
            r -> r.resourceName(getId())
        );

        Map<String, String> tags = new HashMap<>();

        response.tagList().forEach(o -> tags.put(o.key(), o.value()));

        return tags;
    }

    List<Tag> toDocDbTags(Map<String, String> tagMap) {
        List<Tag> tags = new ArrayList<>();
        for (String key : tagMap.keySet()) {
            tags.add(
                Tag.builder()
                    .key(key)
                    .value(tagMap.get(key))
                    .build()
            );
        }

        return tags;
    }

}
