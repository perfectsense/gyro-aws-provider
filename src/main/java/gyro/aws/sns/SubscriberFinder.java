package gyro.aws.sns;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query SNS subscriptions.
 *
 * .. code-block:: gyro
 *
 *    subscriber: $(aws::sns-subscriber EXTERNAL/* | arn = '')
 */
@Type("sns-subscriber")
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

        if (filters.containsKey("arn") && !ObjectUtils.isBlank(filters.get("arn"))) {
            targetSubscription.addAll(client.listSubscriptionsPaginator()
                .subscriptions().stream()
                .filter(o -> o.subscriptionArn().equals(filters.get("arn")))
                .collect(Collectors.toList()));
        }

        return targetSubscription;
    }

    @Override
    protected List<Subscription> findAllAws(SnsClient client) {
        return client.listSubscriptionsPaginator().subscriptions().stream().collect(Collectors.toList());
    }
}
