/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.ec2;

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
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.ec2.model.TagDescription;
import software.amazon.awssdk.services.ec2.paginators.DescribeTagsIterable;

public abstract class Ec2TaggableResource<T> extends AwsResource {

    private static final String NAME_KEY = "Name";

    private Map<String, String> tags;
    private boolean tagsLoaded = false;

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

    protected abstract String getResourceId();

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
        Ec2Client client = createClient(Ec2Client.class);

        Map<String, String> tags = new HashMap<>();

        DescribeTagsIterable response = client.describeTagsPaginator(
            r -> r.filters(
                f -> f.name("resource-id")
                    .values(getResourceId())
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
        State state, AwsResource config,
        Set<String> changedProperties);

    @Override
    public final void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        doUpdate(ui, state, (AwsResource) current, changedFieldNames);
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
                client.deleteTags(r -> r.resources(getResourceId()).tags(tagObjects));
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
                client.createTags(r -> r.resources(getResourceId()).tags(tagObjects));
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
                client.createTags(r -> r.resources(getResourceId()).tags(tagObjects));
                return null;
            });
        }
    }
}
