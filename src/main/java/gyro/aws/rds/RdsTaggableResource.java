package gyro.aws.rds;

import gyro.aws.AwsResource;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceOutput;
import gyro.lang.Resource;
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
    @ResourceOutput
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * A list of tags.
     */
    @ResourceDiffProperty(updatable = true)
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

    protected abstract void doCreate();

    @Override
    public final void create() {
        doCreate();
        addTags();
    }

    protected abstract void doUpdate(Resource config, Set<String> changedProperties);

    @Override
    public final void update(Resource current, Set<String> changedProperties) {
        doUpdate(current, changedProperties);
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
