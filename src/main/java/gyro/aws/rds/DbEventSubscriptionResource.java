package gyro.aws.rds;

import gyro.aws.Copyable;
import gyro.aws.sns.TopicResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.scope.State;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.CreateEventSubscriptionResponse;
import software.amazon.awssdk.services.rds.model.DescribeEventSubscriptionsResponse;
import software.amazon.awssdk.services.rds.model.EventSubscription;
import software.amazon.awssdk.services.rds.model.SubscriptionNotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Create a db event subscription.
 *
 * .. code-block:: gyro
 *
 *    aws::db-event-subscription db-event-subscription-example
 *        name: "db-event-subscription-example"
 *        sns-topic: $(aws::sns-topic sns-topic-example)
 *        enabled: true
 *        source-type: "db-instance"
 *        event-categories: ["availability", "deletion"]
 *        tags: {
 *            Name: "db-event-subscription-example"
 *        }
 *    end
 */
@Type("db-event-subscription")
public class DbEventSubscriptionResource extends RdsTaggableResource implements Copyable<EventSubscription> {

    private Boolean enabled;
    private List<String> eventCategories;
    private TopicResource snsTopic;
    private List<String> sourceIds;
    private String sourceType;
    private String name;

    /**
     * Enable or disable the subscription. Default to true.
     */
    @Updatable
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * A list of event categories for a SourceType to subscribe to. See `Events <https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/USER_Events.html>`_ topic in the Amazon RDS User Guide or by using the `DescribeEventCategories` action.
     */
    @Updatable
    public List<String> getEventCategories() {
        if (eventCategories == null || eventCategories.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> sorted = new ArrayList<>(eventCategories);
        Collections.sort(sorted);

        return sorted;
    }

    public void setEventCategories(List<String> eventCategories) {
        this.eventCategories = eventCategories;
    }

    /**
     * The subscribed SNS topic. (Required)
     */
    @Updatable
    public TopicResource getSnsTopic() {
        return snsTopic;
    }

    public void setSnsTopic(TopicResource snsTopic) {
        this.snsTopic = snsTopic;
    }

    /**
     * The list of identifiers of the event sources. If omitted, then all sources are included in the response.
     */
    public List<String> getSourceIds() {
        return sourceIds;
    }

    public void setSourceIds(List<String> sourceIds) {
        if (sourceIds == null) {
            sourceIds = new ArrayList<>();
        }

        this.sourceIds = sourceIds;
    }

    /**
     * The type of source that is generating the events. If omitted, all events are returned. Valid values: ``db-instance``, ``db-cluster``, ``db-parameter-group``, ``db-security-group``, ``db-snapshot``, ``db-cluster-snapshot``.
     */
    @Updatable
    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    /**
     * The name of the subscription. (Required)
     */
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void copyFrom(EventSubscription subscription) {
        setEnabled(subscription.enabled());
        setEventCategories(subscription.eventCategoriesList());
        setSnsTopic(findById(TopicResource.class, subscription.snsTopicArn()));
        List<String> sourceIds = subscription.sourceIdsList();
        setSourceIds(sourceIds.isEmpty() ? null : sourceIds);
        setSourceType(subscription.sourceType());
        setArn(subscription.eventSubscriptionArn());
    }

    @Override
    protected boolean doRefresh() {
        RdsClient client = createClient(RdsClient.class);

        if (ObjectUtils.isBlank(getName())) {
            throw new GyroException("name is missing, unable to load db event subscription.");
        }

        try {
            DescribeEventSubscriptionsResponse response = client.describeEventSubscriptions(
                r -> r.subscriptionName(getName())
            );

            response.eventSubscriptionsList().forEach(this::copyFrom);

        } catch (SubscriptionNotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        RdsClient client = createClient(RdsClient.class);
        CreateEventSubscriptionResponse response = client.createEventSubscription(
            r -> r.enabled(getEnabled())
                    .eventCategories(getEventCategories())
                    .sourceIds(getSourceIds())
                    .sourceType(getSourceType())
                    .subscriptionName(getName())
                    .snsTopicArn(getSnsTopic().getArn())
        );

        setArn(response.eventSubscription().eventSubscriptionArn());
    }

    @Override
    protected void doUpdate(Resource config, Set<String> changedProperties) {
        RdsClient client = createClient(RdsClient.class);
        client.modifyEventSubscription(
            r -> r.enabled(getEnabled())
                    .eventCategories(getEventCategories())
                    .snsTopicArn(getSnsTopic().getArn())
                    .sourceType(getSourceType())
                    .subscriptionName(getName())
        );
    }

    @Override
    public void delete(GyroUI ui, State state) {
        RdsClient client = createClient(RdsClient.class);
        client.deleteEventSubscription(
            r -> r.subscriptionName(getName())
        );

    }
}
