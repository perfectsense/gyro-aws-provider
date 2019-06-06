package gyro.aws.sns;

import gyro.aws.AwsFinder;
import gyro.core.Type;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query subscriptions.
 *
 * .. code-block:: gyro
 *
 *    subscriber: $(aws::subscriber EXTERNAL/* | arn = '')
 */
@Type("subscriber")
public class SubscriberFinder extends AwsFinder<SnsClient, Subscription, SubscriberResource> {

    private String arn;

    /**
     * The arn of the subscription.
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    protected List<Subscription> findAws(SnsClient client, Map<String, String> filters) {
        List<Subscription> targetSubscription = new ArrayList<>();

        for (Subscription target : client.listSubscriptions().subscriptions()) {
            if (target.subscriptionArn().equals(filters.get("arn"))) {
                targetSubscription.add(target);
                return targetSubscription;
            }
        }

        return null;
    }

    @Override
    protected List<Subscription> findAllAws(SnsClient client) {
        return client.listSubscriptions().subscriptions();
    }
}
