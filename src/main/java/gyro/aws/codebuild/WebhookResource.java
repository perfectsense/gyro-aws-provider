/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.codebuild;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.codebuild.CodeBuildClient;
import software.amazon.awssdk.services.codebuild.model.Webhook;
import software.amazon.awssdk.services.codebuild.model.WebhookFilter;

public class WebhookResource extends AwsResource implements Copyable<Webhook> {

    private String branchFilter;
    private String buildType;
    private List<CodebuildWebhookFilter> filterGroup;
    private Boolean rotateSecret;

    // Read-only
    private String lastModifiedSecret;
    private String payloadUrl;
    private String secret;
    private String url;

    /**
     * The regular expression used to determine which repository branches are built when a webhook is triggered.
     */
    @Updatable
    public String getBranchFilter() {
        return branchFilter;
    }

    public void setBranchFilter(String branchFilter) {
        this.branchFilter = branchFilter;
    }

    /**
     * The type of build the webhook will trigger.
     */
    @Updatable
    @ValidStrings({ "BUILD", "BUILD_BATCH" })
    public String getBuildType() {
        return buildType;
    }

    public void setBuildType(String buildType) {
        this.buildType = buildType;
    }

    /**
     * The list of filter group configuration to determine which webhooks are triggered.
     *
     * @subresource gyro.aws.codebuild.CodebuildWebhookFilter
     */
    @Updatable
    public List<CodebuildWebhookFilter> getFilterGroup() {
        if (filterGroup == null) {
            filterGroup = new ArrayList<>();
        }

        return filterGroup;
    }

    public void setFilterGroup(List<CodebuildWebhookFilter> filterGroup) {
        this.filterGroup = filterGroup;
    }

    /**
     * When set to ``true`` the associated GitHub repository's secret token is updated. When set to ``false`` the secret
     * token is not updated.
     */
    @Updatable
    public Boolean getRotateSecret() {
        return rotateSecret;
    }

    public void setRotateSecret(Boolean rotateSecret) {
        this.rotateSecret = rotateSecret;
    }

    /**
     * The time that indicates the last time a repository's secret token was modified.
     */
    @Output
    public String getLastModifiedSecret() {
        return lastModifiedSecret;
    }

    public void setLastModifiedSecret(String lastModifiedSecret) {
        this.lastModifiedSecret = lastModifiedSecret;
    }

    /**
     * The build project endpoint where webhook events are sent.
     */
    @Output
    public String getPayloadUrl() {
        return payloadUrl;
    }

    public void setPayloadUrl(String payloadUrl) {
        this.payloadUrl = payloadUrl;
    }

    /**
     * The secret token of the associated repository.
     */
    @Output
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * The URL to the webhook.
     */
    @Id
    @Output
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void copyFrom(Webhook webhook) {
        setBranchFilter(webhook.branchFilter());
        setBuildType(webhook.buildTypeAsString());
        setBranchFilter(webhook.branchFilter() != null ? webhook.branchFilter() : null);
        setLastModifiedSecret(
            webhook.lastModifiedSecret() != null ? webhook.lastModifiedSecret().toString() : null);
        setPayloadUrl(webhook.payloadUrl());
        setSecret(webhook.secret());
        setUrl(webhook.url());

        if (webhook.filterGroups() != null) {
            CodebuildWebhookFilter webhookFilter = newSubresource(CodebuildWebhookFilter.class);
            List<CodebuildWebhookFilter> filterList = new ArrayList<>();

            for (List<WebhookFilter> filters : webhook.filterGroups()) {
                for (WebhookFilter filter : filters) {
                    webhookFilter.copyFrom(filter);
                    filterList.add(webhookFilter);
                }
            }

            setFilterGroup(filterList);
        } else {
            setFilterGroup(null);
        }
    }

    @Override
    public boolean refresh() {
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        CodeBuildClient client = createClient(CodeBuildClient.class);

        ProjectResource project = (ProjectResource) parent();

        client.createWebhook(r -> r.projectName(project.getName())
            .branchFilter(getBranchFilter())
            .buildType(getBuildType())
            .filterGroups(getFilterGroup().stream()
                .map(CodebuildWebhookFilter::toWebhookFilter)
                .collect(Collectors.toList()))
        );

        if (getRotateSecret() != null) {
            client.updateWebhook(r -> r.projectName(project.getName()).rotateSecret(getRotateSecret()));
        }
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        CodeBuildClient client = createClient(CodeBuildClient.class);

        ProjectResource project = (ProjectResource) parent();

        client.updateWebhook(r -> r.projectName(project.getName())
            .branchFilter(getBranchFilter())
            .buildType(getBuildType())
            .filterGroups(getFilterGroup().stream()
                .map(CodebuildWebhookFilter::toWebhookFilter)
                .collect(Collectors.toList()))
            .rotateSecret(getRotateSecret())
        );
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        CodeBuildClient client = createClient(CodeBuildClient.class);

        ProjectResource project = (ProjectResource) parent();

        client.deleteWebhook(r -> r.projectName(project.getName()));
    }
}
