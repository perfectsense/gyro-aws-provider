package gyro.aws.wafv2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.psddev.dari.util.CompactMap;
import gyro.aws.AwsResource;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.wafv2.model.Tag;

public abstract class WafTaggableResource extends AwsResource {

    private String scope;
    private Map<String, String> tags;
    private boolean tagsLoaded = false;

    @Required
    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

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

    protected abstract String getResourceArn();

    protected boolean doRefresh() {
        return true;
    }

    protected void refreshTags() {
        if (tagsLoaded) {
            return;
        }

        getTags().clear();
        getTags().putAll(loadTags());

        tagsLoaded = true;
    }

    private Map<String, String> loadTags() {
        Wafv2Client client = createClient(Wafv2Client.class);

        Map<String, String> tags = new HashMap<>();

        ListTagsForResourceResponse response = client.listTagsForResource(
            r -> r.resourceARN(getResourceArn())
        );

        if (response.tagInfoForResource() != null && response.tagInfoForResource().tagList() != null) {
            for (Tag tagDescription : response.tagInfoForResource().tagList()) {
                    tags.put(tagDescription.key(), tagDescription.value());
            }
        }

        return tags;
    }

    @Override
    public final boolean refresh() {
        boolean refreshed = doRefresh();

        refreshTags();

        return refreshed;
    }

    protected abstract void doCreate(GyroUI ui, State state);

    @Override
    public final void create(GyroUI ui, State state) {
        doCreate(ui, state);
        createTags();
    }

    protected abstract void doUpdate(
        GyroUI ui,
        State state, Resource config,
        Set<String> changedProperties);

    @Override
    public final void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        doUpdate(ui, state, current, changedFieldNames);

        if (changedFieldNames.contains("tags")) {
            createTags();
        }
    }

    private void createTags() {
        Wafv2Client client = createClient(Wafv2Client.class);

        Map<String, String> pendingTags = getTags();
        Map<String, String> currentTags = loadTags();

        MapDifference<String, String> diff = Maps.difference(currentTags, pendingTags);

        // Remove tags
        if (!diff.entriesOnlyOnLeft().isEmpty()) {

            List<String> tagObjects = new ArrayList<>();
            for (Map.Entry<String, String> entry : diff.entriesOnlyOnLeft().entrySet()) {
                tagObjects.add(entry.getKey());
            }

            executeService(() -> {
                client.untagResource(r -> r.resourceARN(getResourceArn()).tagKeys(tagObjects));
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
                client.tagResource(r -> r.resourceARN(getResourceArn()).tags(tagObjects));
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
                client.tagResource(r -> r.resourceARN(getResourceArn()).tags(tagObjects));
                return null;
            });
        }
    }
}
