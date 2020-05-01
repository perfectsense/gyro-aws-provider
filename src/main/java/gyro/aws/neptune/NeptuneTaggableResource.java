/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.neptune;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import gyro.aws.AwsResource;
import gyro.core.GyroUI;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.neptune.NeptuneClient;
import software.amazon.awssdk.services.neptune.model.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class NeptuneTaggableResource extends AwsResource {

    private String arn;
    private Map<String, String> tags;

    /**
     * The ARN of the Neptune resource.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * A list of tags.
     */
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
    public final boolean refresh() {
        boolean refreshed = doRefresh();

        if (refreshed) {
            getTags().clear();
            loadTags();
        }

        return refreshed;
    }

    protected abstract void doCreate(GyroUI ui, State state);

    @Override
    public final void create(GyroUI ui, State state) throws Exception {
        doCreate(ui, state);
        addTags();
    }

    protected abstract void doUpdate(Resource current, Set<String> changedProperties);

    @Override
    public final void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        doUpdate(current, changedFieldNames);
        saveTags();
    }

    private void loadTags() {
        NeptuneClient client = createClient(NeptuneClient.class);

        client.listTagsForResource(r -> r.resourceName(getArn())).tagList().stream()
                .forEach(o -> getTags().put(o.key(), o.value()));
    }

    private void addTags() {
        NeptuneClient client = createClient(NeptuneClient.class);
        client.addTagsToResource(
                r -> r.resourceName(getArn()).tags(
                        getTags().entrySet().stream()
                                .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
                                .collect(Collectors.toList())
                )
        );
    }

    private void saveTags() {
        NeptuneClient client = createClient(NeptuneClient.class);

        Map<String, String> currentTags = new HashMap<>();
        client.listTagsForResource(r -> r.resourceName(getArn())).tagList().forEach(o -> currentTags.put(o.key(), o.value()));
        Map<String, String> pendingTags = getTags();
        MapDifference<String, String> diff = Maps.difference(currentTags, pendingTags);

        if (!diff.entriesOnlyOnLeft().isEmpty()) {
            client.removeTagsFromResource(
                    r -> r.resourceName(getArn())
                            .tagKeys(diff.entriesOnlyOnLeft().keySet())
            );
        }

        if (!diff.entriesOnlyOnRight().isEmpty()) {
            client.addTagsToResource(
                    r -> r.resourceName(getArn()).tags(
                            diff.entriesOnlyOnRight().entrySet().stream()
                                    .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
                                    .collect(Collectors.toList())
                    )
            );
        }

        if (!diff.entriesDiffering().isEmpty()) {
            client.removeTagsFromResource(
                    r -> r.resourceName(getArn())
                            .tagKeys(diff.entriesDiffering().keySet())
            );

            client.addTagsToResource(
                    r -> r.resourceName(getArn()).tags(
                            diff.entriesDiffering().keySet().stream()
                                    .map(k -> Tag.builder().key(k).value(diff.entriesDiffering().get(k).rightValue()).build())
                                    .collect(Collectors.toList())
                    )
            );
        }
    }
}
