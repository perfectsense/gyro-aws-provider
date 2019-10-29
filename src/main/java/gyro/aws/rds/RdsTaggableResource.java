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

package gyro.aws.rds;

import gyro.aws.AwsResource;
import gyro.core.GyroUI;
import gyro.core.resource.Updatable;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.rds.model.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class RdsTaggableResource extends AwsResource {

    private String arn;
    private Map<String, String> tags;

    /**
     * The ARN of the RDS resource.
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
    public final void create(GyroUI ui, State state) {
        doCreate(ui, state);
        addTags();
    }

    protected abstract void doUpdate(Resource config, Set<String> changedProperties);

    @Override
    public final void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        doUpdate(current, changedFieldNames);
        ((RdsTaggableResource) current).removeTags();
        addTags();
    }

    private void loadTags() {
        RdsClient client = createClient(RdsClient.class);

        ListTagsForResourceResponse tagResponse = client.listTagsForResource(
            r -> r.resourceName(getArn())
        );

        tagResponse.tagList().stream().forEach(t -> getTags().put(t.key(), t.value()));
    }

    private void addTags() {
        RdsClient client = createClient(RdsClient.class);
        executeService(() ->
            client.addTagsToResource(
                r -> r.resourceName(getArn())
                    .tags(getTags().entrySet().stream()
                        .map(e -> Tag.builder().key(e.getKey()).value(e.getValue()).build())
                        .collect(Collectors.toList()))
            ));
    }

    private void removeTags() {
        RdsClient client = createClient(RdsClient.class);
        executeService(() ->
            client.removeTagsFromResource(
                r -> r.resourceName(getArn())
                    .tagKeys(getTags().keySet())
            ));
    }
}
