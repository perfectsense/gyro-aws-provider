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

package gyro.aws.codebuild;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.codebuild.CodeBuildClient;
import software.amazon.awssdk.services.codebuild.model.BatchGetProjectsResponse;
import software.amazon.awssdk.services.codebuild.model.ResourceNotFoundException;
import software.amazon.awssdk.services.codebuild.model.Webhook;
import software.amazon.awssdk.services.codebuild.model.WebhookFilter;

@Type("webhook")
public class WebhookResource extends AwsResource implements Copyable<BatchGetProjectsResponse> {

    private String projectName;
    private String branchFilter;
    private String buildType;
    private List<CodebuildWebhookFilter> filterGroups;
    private Boolean rotateSecret;

    // Read-only
    private String lastModifiedSecret;
    private String payloadUrl;
    private String secret;
    private String url;

    /**
     * The name of the build project.
     */
    @Id
    @Required
    @Regex("[A-Za-z0-9][A-Za-z0-9\\-_]{1,254}")
    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

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
     * The list of filter groups to determine which webhooks are triggered.
     */
    @Updatable
    public List<CodebuildWebhookFilter> getFilterGroups() {
        if (filterGroups == null) {
            filterGroups = new ArrayList<>();
        }
        return filterGroups;
    }

    public void setFilterGroups(List<CodebuildWebhookFilter> filterGroups) {
        this.filterGroups = filterGroups;
    }

    /**
     * The rotate secret field that specifies whether the associated GitHub repository's secret token should be updated.
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
    @Output
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void copyFrom(BatchGetProjectsResponse model) {
        if (model != null && !model.projects().isEmpty()) {
            Webhook webhook = model.projects().get(0).webhook();

            if (webhook != null) {
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

                    setFilterGroups(filterList);
                }
            }
        }
    }

    @Override
    public boolean refresh() {
        CodeBuildClient client = createClient(CodeBuildClient.class);
        BatchGetProjectsResponse response = null;

        try {
            response = client.batchGetProjects(r -> r.names(getProjectName()));
        } catch (ResourceNotFoundException ex) {
            // No Resource found
        }

        if (response == null) {
            return false;
        }

        copyFrom(response);
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        CodeBuildClient client = createClient(CodeBuildClient.class);

        client.createWebhook(r -> r
            .projectName(getProjectName())
            .branchFilter(getBranchFilter())
            .buildType(getBuildType())
            .filterGroups(convertFilterGroups(getFilterGroups()))
        );

        refresh();

        if (getRotateSecret() != null) {
            client.updateWebhook(r -> r.projectName(getProjectName()).rotateSecret(getRotateSecret()));
        }
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        CodeBuildClient client = createClient(CodeBuildClient.class);

        if (!changedFieldNames.isEmpty()) {
            client.updateWebhook(r -> r.projectName(getProjectName())
                .branchFilter(getBranchFilter())
                .buildType(getBuildType())
                .filterGroups(convertFilterGroups(getFilterGroups()))
                .rotateSecret(getRotateSecret())
            );
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        CodeBuildClient client = createClient(CodeBuildClient.class);

        client.deleteWebhook(r -> r.projectName(getProjectName()));
    }

    private List<List<WebhookFilter>> convertFilterGroups(List<CodebuildWebhookFilter> filterGroups) {
        List<List<WebhookFilter>> projectFilterGroups = new ArrayList<>();
        List<WebhookFilter> filterList = new ArrayList<>();

        for (CodebuildWebhookFilter filter : filterGroups) {
            filterList.add(WebhookFilter.builder()
                .pattern(filter.getPattern())
                .type(filter.getType())
                .excludeMatchedPattern(filter.getExcludeMatchedPattern())
                .build());
        }
        projectFilterGroups.add(filterList);

        return projectFilterGroups;
    }
}
