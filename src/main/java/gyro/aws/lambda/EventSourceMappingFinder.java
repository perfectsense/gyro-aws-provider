package gyro.aws.lambda;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.GetEventSourceMappingResponse;
import software.amazon.awssdk.services.lambda.model.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query lambda event source mapping.
 *
 * .. code-block:: gyro
 *
 *    event-source-mapping: $(aws::lambda-event-source-mapping EXTERNAL/* | id = '')
 */
@Type("lambda-event-source-mapping")
public class EventSourceMappingFinder extends AwsFinder<LambdaClient, GetEventSourceMappingResponse, EventSourceMappingResource> {
    private String id;

    /**
     * The id of the event source mapping.
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected List<GetEventSourceMappingResponse> findAllAws(LambdaClient client) {
        List<GetEventSourceMappingResponse> getEventSourceMappingResponses = new ArrayList<>();
        client.listEventSourceMappingsPaginator().eventSourceMappings()
            .forEach(o -> getEventSourceMappingResponses.add(client.getEventSourceMapping(r -> r.uuid(o.uuid()))));
        return getEventSourceMappingResponses;
    }

    @Override
    protected List<GetEventSourceMappingResponse> findAws(LambdaClient client, Map<String, String> filters) {
        List<GetEventSourceMappingResponse> getEventSourceMappingResponses = new ArrayList<>();

        if (!filters.containsKey("id")) {
            throw new IllegalArgumentException("'id' is required.");
        }

        try {
            getEventSourceMappingResponses.add(client.getEventSourceMapping(r -> r.uuid(filters.get("id"))));
        } catch (ResourceNotFoundException ignore) {
            // ignore
        }

        return getEventSourceMappingResponses;
    }
}
