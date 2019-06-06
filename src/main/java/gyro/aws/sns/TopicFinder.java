package gyro.aws.sns;

import gyro.aws.AwsFinder;
import gyro.core.Type;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.Topic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query topics.
 *
 * .. code-block:: gyro
 *
 *    $(aws::topic EXTERNAL/* | arn = '')
 */
@Type("topic")
public class TopicFinder extends AwsFinder<SnsClient, Topic, TopicResource> {

    private String arn;

    /**
     * The arn of the topic.
     */
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    protected List<Topic> findAws(SnsClient client, Map<String, String> filters) {
        List<Topic> targetSubscription = new ArrayList<>();

        for (Topic target : client.listTopics().topics()) {
            if (target.topicArn().equals(filters.get("arn"))) {
                targetSubscription.add(target);
                return targetSubscription;
            }
        }

        return null;
    }

    @Override
    protected List<Topic> findAllAws(SnsClient client) {
        return client.listTopics().topics();
    }
}
