package gyro.aws.sns;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.Topic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query topics.
 *
 * .. code-block:: gyro
 *
 *    sns: $(aws::topic EXTERNAL/* | arn = '')
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
        List<Topic> topics = new ArrayList<>();

        if (filters.containsKey("arn") && !ObjectUtils.isBlank(filters.get("arn"))) {
            topics.addAll(client.listTopicsPaginator().topics().stream().filter(o -> o.topicArn().equals(filters.get("arn"))).collect(Collectors.toList()));
        }

        return topics;
    }

    @Override
    protected List<Topic> findAllAws(SnsClient client) {
        return client.listTopicsPaginator().topics().stream().collect(Collectors.toList());
    }
}
