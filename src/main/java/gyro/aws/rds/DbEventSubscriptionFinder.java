package gyro.aws.rds;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.EventSubscription;
import software.amazon.awssdk.services.rds.model.SubscriptionNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query db event subscription.
 *
 * .. code-block:: gyro
 *
 *    subscriptions: $(aws::db-event-subscription EXTERNAL/* | subscription-name = 'db-event-subscription-example')
 */
@Type("db-event-subscription")
public class DbEventSubscriptionFinder extends AwsFinder<RdsClient, EventSubscription, DbEventSubscriptionResource> {

    private String subscriptionName;

    /**
     * The name of the event subscription.
     */
    public String getSubscriptionName() {
        return subscriptionName;
    }

    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    @Override
    protected List<EventSubscription> findAws(RdsClient client, Map<String, String> filters) {
        if (!filters.containsKey("subscription-name")) {
            throw new IllegalArgumentException("'subscription-name' is required.");
        }

        try {
            return client.describeEventSubscriptions(r -> r.subscriptionName(filters.get("subscription-name"))).eventSubscriptionsList();
        } catch (SubscriptionNotFoundException ex) {
            return Collections.emptyList();
        }
    }

    @Override
    protected List<EventSubscription> findAllAws(RdsClient client) {
        return client.describeEventSubscriptionsPaginator().eventSubscriptionsList().stream().collect(Collectors.toList());
    }
}
