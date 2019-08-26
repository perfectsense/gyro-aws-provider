package gyro.aws.sqs;

import gyro.aws.AwsFinder;
import gyro.core.Type;

import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.List;
import java.util.Map;

/**
 * Query SQS queues.
 *
 * .. code-block:: gyro
 *
 *    sqs: $(external-query aws::sqs-queue { name: ''})
 */
@Type("sqs-queue")
public class SqsFinder extends AwsFinder<SqsClient, String, SqsResource> {

    private String name;

    /**
     * The name of the queue.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<String> findAws(SqsClient client, Map<String, String> filters) {
        return client.listQueues(r -> r.queueNamePrefix(filters.get("name"))).queueUrls();
    }

    @Override
    protected List<String> findAllAws(SqsClient client) {
        return client.listQueues().queueUrls();
    }
}
