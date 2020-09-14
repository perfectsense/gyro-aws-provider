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
    private List<List<CodebuildWebhookFilter>> filterGroups;
    private String lastModifiedSecret;
    private String payloadUrl;
    private String secret;
    private String url;

    @Id
    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    @Updatable
    public String getBranchFilter() {
        return branchFilter;
    }

    public void setBranchFilter(String branchFilter) {
        this.branchFilter = branchFilter;
    }

    @Updatable
    public String getBuildType() {
        return buildType;
    }

    public void setBuildType(String buildType) {
        this.buildType = buildType;
    }

    @Updatable
    public List<List<CodebuildWebhookFilter>> getFilterGroups() {
        return filterGroups;
    }

    public void setFilterGroups(List<List<CodebuildWebhookFilter>> filterGroups) {
        this.filterGroups = filterGroups;
    }

    @Output
    public String getLastModifiedSecret() {
        return lastModifiedSecret;
    }

    public void setLastModifiedSecret(String lastModifiedSecret) {
        this.lastModifiedSecret = lastModifiedSecret;
    }

    @Output
    public String getPayloadUrl() {
        return payloadUrl;
    }

    public void setPayloadUrl(String payloadUrl) {
        this.payloadUrl = payloadUrl;
    }

    @Output
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    @Output
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void copyFrom(BatchGetProjectsResponse model) {
        if (!model.projects().isEmpty()) {
            Webhook webhook = model.projects().get(0).webhook();

            setBranchFilter(webhook.branchFilter());
            setBuildType(webhook.buildTypeAsString());
            setLastModifiedSecret(webhook.lastModifiedSecret().toString());
            setPayloadUrl(webhook.payloadUrl());
            setSecret(webhook.secret());
            setUrl(webhook.url());

            if (webhook.filterGroups() != null) {
                List<List<CodebuildWebhookFilter>> filterGroups = new ArrayList<>();
                CodebuildWebhookFilter webhookFilter = newSubresource(CodebuildWebhookFilter.class);

                for (List<WebhookFilter> filters : webhook.filterGroups()) {
                    List<CodebuildWebhookFilter> filterList = new ArrayList<>();

                    for (WebhookFilter filter : filters) {
                        webhookFilter.copyFrom(filter);
                        filterList.add(webhookFilter);
                    }
                    filterGroups.add(filterList);
                }

                setFilterGroups(filterGroups);
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
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        CodeBuildClient client = createClient(CodeBuildClient.class);

        if (changedFieldNames.size() > 1) {
            client.updateWebhook(r -> r.projectName(getProjectName())
                .branchFilter(getBranchFilter())
                .buildType(getBuildType())
                .filterGroups(convertFilterGroups(getFilterGroups()))
            );
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        CodeBuildClient client = createClient(CodeBuildClient.class);

        client.deleteWebhook(r -> r.projectName(getProjectName()));
    }

    private List<List<WebhookFilter>> convertFilterGroups(List<List<CodebuildWebhookFilter>> filterGroups) {
        List<List<WebhookFilter>> projectFilterGroups = new ArrayList<>();

        for (List<CodebuildWebhookFilter> filters : filterGroups) {
            List<WebhookFilter> filterList = new ArrayList<>();

            for (CodebuildWebhookFilter filter : filters) {
                filterList.add(WebhookFilter.builder()
                    .pattern(filter.getPattern())
                    .type(filter.getType())
                    .excludeMatchedPattern(filter.getExcludeMatchedPattern())
                    .build());
            }

            projectFilterGroups.add(filterList);
        }

        return projectFilterGroups;
    }
}
