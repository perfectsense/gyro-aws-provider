package gyro.aws.lambda;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.core.BeamCore;
import gyro.core.GyroException;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import gyro.core.diff.ResourceOutput;
import gyro.lang.Resource;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.CreateEventSourceMappingRequest;
import software.amazon.awssdk.services.lambda.model.CreateEventSourceMappingResponse;
import software.amazon.awssdk.services.lambda.model.GetEventSourceMappingResponse;
import software.amazon.awssdk.services.lambda.model.ResourceNotFoundException;

import java.time.Instant;
import java.util.Date;
import java.util.Set;

/**
 * Creates an event source mapping.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::event-source-mapping event-source-mapping-example
 *         function-name: $(aws::lambda-function lambda-function-event-source-mapping-example | function-name)
 *         batch-size: 10
 *         event-source-arn: "$(aws::sqs sqs-event-source-mapping-example | queue-arn)"
 *     end
 */
@ResourceName("event-source-mapping")
public class EventSourceMapping extends AwsResource {
    private String functionName;
    private Integer batchSize;
    private Boolean enabled;
    private String eventSourceArn;
    private String startingPosition;
    private String startingPositionTimestamp;

    // -- Readonly

    private String id;
    private Date lastModified;
    private String lastProcessingResult;
    private String state;
    private String stateTransitionReason;
    private String arn;
    private String functionArn;


    /**
     * The name / arn / partial arn of the function to be associated with. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public String getFunctionName() {
        return isFunctionArnSame() ? getFunctionArn() : functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    /**
     * The batch size for the event to invoke the function. (Required)
     */
    @ResourceDiffProperty(updatable = true)
    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * Enable or disable the event mapping. Defaults to ``True``.
     */
    @ResourceDiffProperty(updatable = true)
    public Boolean getEnabled() {
        if (enabled == null) {
            enabled = true;
        }

        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * The event source arn to be connected with the function. (Required)
     */
    public String getEventSourceArn() {
        return eventSourceArn;
    }

    public void setEventSourceArn(String eventSourceArn) {
        this.eventSourceArn = eventSourceArn;
    }

    /**
     * The starting position in terms of the connected resource for the function to be invoked. Required if source event is DynamoDb or Kinesis. Valid values are ``TRIM_HORIZON`` or ``LATEST`` or ``AT_TIMESTAMP``
     */
    public String getStartingPosition() {
        return startingPosition != null ? startingPosition.toUpperCase() : null;
    }

    public void setStartingPosition(String startingPosition) {
        this.startingPosition = startingPosition;
    }

    /**
     * Starting timestamp to invoke the function. Required only if `starting-position` set to ``AT_TIMESTAMP``.
     */
    public String getStartingPositionTimestamp() {
        return startingPositionTimestamp;
    }

    public void setStartingPositionTimestamp(String startingPositionTimestamp) {
        this.startingPositionTimestamp = startingPositionTimestamp;
    }

    /**
     * The id of the event mapping.
     */
    @ResourceOutput
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Last modified date of the event mapping.
     */
    @ResourceOutput
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Last processing of the event mapping.
     */
    @ResourceOutput
    public String getLastProcessingResult() {
        return lastProcessingResult;
    }

    public void setLastProcessingResult(String lastProcessingResult) {
        this.lastProcessingResult = lastProcessingResult;
    }

    /**
     * Current state of the event mapping.
     */
    @ResourceOutput
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * Reason for the current state transition.
     */
    @ResourceOutput
    public String getStateTransitionReason() {
        return stateTransitionReason;
    }

    public void setStateTransitionReason(String stateTransitionReason) {
        this.stateTransitionReason = stateTransitionReason;
    }

    /**
     * The arn for the event mapping.
     */
    @ResourceOutput
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The arn of the function associated the event mapping.
     */
    @ResourceOutput
    public String getFunctionArn() {
        return functionArn;
    }

    public void setFunctionArn(String functionArn) {
        this.functionArn = functionArn;
    }

    @Override
    public boolean refresh() {
        LambdaClient client = createClient(LambdaClient.class);

        try {
            GetEventSourceMappingResponse response = client.getEventSourceMapping(
                r -> r.uuid(getId())
            );

            setBatchSize(response.batchSize());
            setEventSourceArn(response.eventSourceArn());
            setLastModified(Date.from(response.lastModified()));
            setLastProcessingResult(response.lastProcessingResult());
            setState(response.state());
            setStateTransitionReason(response.stateTransitionReason());
            setEnabled(getState().equals("Enabled"));
            setFunctionArn(response.functionArn());
            setFunctionName(getFunctionArn());
        } catch (ResourceNotFoundException ex) {
            return false;
        }

        return true;

    }

    @Override
    public void create() {
        LambdaClient client = createClient(LambdaClient.class);

        CreateEventSourceMappingRequest.Builder builder = CreateEventSourceMappingRequest.builder()
            .functionName(getFunctionName())
            .batchSize(getBatchSize())
            .enabled(getEnabled())
            .eventSourceArn(getEventSourceArn());

        if (!ObjectUtils.isBlank(getStartingPosition())) {
            builder = builder.startingPosition(getStartingPosition());
        }

        if (!ObjectUtils.isBlank(getStartingPositionTimestamp())) {
            builder = builder.startingPositionTimestamp(Instant.now());
        }

        CreateEventSourceMappingResponse response = client.createEventSourceMapping(builder.build());

        setId(response.uuid());

        saveEventSourceMapping(client);
    }

    @Override
    public void update(Resource resource, Set<String> set) {
        if (!getState().equals("Enabled") && !getState().equals("Disabled")) {
            throw new GyroException(String.format("Event source mapping in '%s' state. Please try again.", getState()));
        }

        LambdaClient client = createClient(LambdaClient.class);

        client.updateEventSourceMapping(
            r -> r.uuid(getId())
                .enabled(getEnabled())
                .batchSize(getBatchSize())
                .functionName(getFunctionName())
        );

        saveEventSourceMapping(client);
    }

    @Override
    public void delete() {
        LambdaClient client = createClient(LambdaClient.class);

        client.deleteEventSourceMapping(
            r -> r.uuid(getId())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("event source mapping");

        if (!ObjectUtils.isBlank(getFunctionName())) {
            sb.append(" function - ").append(getFunctionName());
        }

        if (!ObjectUtils.isBlank(getEventSourceArn())) {
            sb.append(" source - ").append(getEventSourceArn());
        }

        return sb.toString();
    }

    private boolean isFunctionArnSame() {
        if (ObjectUtils.isBlank(getFunctionArn())) {
            return false;
        } else if (functionName.equals(getFunctionArn())) {
            return true;
        } else {
            if (!functionName.contains(":")) {
                // Just function name
                return getFunctionArn().endsWith(functionName);
            } else if (functionName.startsWith("arn:aws:lambda")) {
                // Full Arn
                return functionName.equals(getFunctionArn());
            } else if (functionName.contains(":function:")) {
                // Partial
                String partialName = functionName.split(":function:").length == 2 ? functionName.split(":function:")[1] : null;
                String partialArn = getFunctionArn().split(":function:").length == 2 ? getFunctionArn().split(":function:")[1] : null;
                if (partialName == null || !partialName.equals(partialArn)) {
                    return false;
                }
            } else {
                return false;
            }

            return true;
        }
    }

    private void saveEventSourceMapping(LambdaClient client) {
        long time = 20000;
        int count = 0;
        boolean wait = true;
        GetEventSourceMappingResponse response = null;

        while(wait && count < 20) {
            count++;
            response = client.getEventSourceMapping(r -> r.uuid(getId()));
            if (response.state().equals(getEnabled() ? "Enabled" : "Disabled")) {
                break;
            }

            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                break;
            }

            if (count % 5 == 0) {
                wait = BeamCore.ui().readBoolean(Boolean.FALSE, "\nWait for completion?..... ");
            }
        }

        setLastModified(Date.from(response.lastModified()));
        setLastProcessingResult(response.lastProcessingResult());
        setState(response.state());
        setStateTransitionReason(response.stateTransitionReason());
        setFunctionArn(response.functionArn());
        setEventSourceArn(response.eventSourceArn());
        setFunctionName(getFunctionArn());
    }
}
