/*
 * Copyright 2022, Brightspot.
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

package gyro.aws.eventbridge;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.eventbridge.model.Tag;

public abstract class EventBridgeTaggableResource extends AwsResource {

    private Map<String, String> tags;

    public abstract void doCreate(GyroUI ui, State state) throws Exception;
    public abstract void doUpdate(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception;

    public abstract String resourceArn();

    /**
     * Tags for the resource.
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

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        doCreate(ui, state);

        state.save();

        if (!getTags().isEmpty()) {
            EventBridgeClient client = createClient(EventBridgeClient.class);

            client.tagResource(r -> r.resourceARN(resourceArn()).tags(getTags().entrySet().stream()
                .map(o -> Tag.builder().key(o.getKey()).value(o.getValue()).build())
                .collect(Collectors.toList())));
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        doUpdate(ui, state, current, changedFieldNames);

        if (changedFieldNames.contains("tags")) {
            EventBridgeClient client = createClient(EventBridgeClient.class);

            Map<String, String> oldTags = ((EventBridgeTaggableResource) current).getTags();
            if (!oldTags.isEmpty()) {
                client.untagResource(r -> r.resourceARN(resourceArn()).tagKeys(oldTags.keySet()));
            }

            if (!getTags().isEmpty()) {
                client.tagResource(r -> r.resourceARN(resourceArn()).tags(getTags().entrySet().stream()
                    .map(o -> Tag.builder().key(o.getKey()).value(o.getValue()).build())
                    .collect(Collectors.toList())));
            }
        }
    }

    protected void refreshTags() {
        EventBridgeClient client = createClient(EventBridgeClient.class);

        ListTagsForResourceResponse response = client.listTagsForResource(r -> r.resourceARN(resourceArn()));

        setTags(response.tags().stream().collect(Collectors.toMap(Tag::key, Tag::value)));
    }
}
