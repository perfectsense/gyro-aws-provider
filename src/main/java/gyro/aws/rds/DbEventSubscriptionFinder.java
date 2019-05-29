package gyro.aws.rds;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.rds.RdsClient;
import software.amazon.awssdk.services.rds.model.EventSubscription;

import java.util.List;
import java.util.Map;

@Type("db-event-subscription")
public class DbEventSubscriptionFinder extends AwsFinder<RdsClient, EventSubscription, DbEventSubscriptionResource> {

    private String subscriptionName;

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    @Override
    protected List<EventSubscription> findAws(RdsClient client, Map<String, String> filters) {
        return client.describeEventSubscriptions(r -> r.subscriptionName(filters.get("subscription-name"))).eventSubscriptionsList();
    }

    @Override
    protected List<EventSubscription> findAllAws(RdsClient client) {
        return client.describeEventSubscriptions().eventSubscriptionsList();
    }

}
