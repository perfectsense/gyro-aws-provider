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

package gyro.aws.docdb;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import gyro.aws.AwsResource;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
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

    protected abstract String getResourceId();

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

    protected abstract void doCreate(GyroUI ui, State state);

    @Override
    public void create(GyroUI ui, State state) {
        doCreate(ui, state);
        saveTags();
    }

    protected abstract void doUpdate(Resource current, Set<String> changedProperties);

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
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
                r -> r.resourceName(getResourceId())
                    .tagKeys(diff.entriesOnlyOnLeft().keySet())
            );
        }

        if (!diff.entriesOnlyOnRight().isEmpty()) {
            client.addTagsToResource(
                r -> r.resourceName(getResourceId())
                    .tags(toDocDbTags(diff.entriesOnlyOnRight()))
            );
        }

        if (!diff.entriesDiffering().isEmpty()) {
            client.removeTagsFromResource(
                r -> r.resourceName(getResourceId())
                    .tagKeys(diff.entriesDiffering().keySet())
            );

            Map<String, String> addTags = new HashMap<>();
            diff.entriesDiffering().keySet().forEach(o -> addTags.put(o, diff.entriesDiffering().get(o).rightValue()));

            client.addTagsToResource(
                r -> r.resourceName(getResourceId())
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
            r -> r.resourceName(getResourceId())
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
