/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.lambda;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.CreateEventSourceMappingRequest;
import software.amazon.awssdk.services.lambda.model.CreateEventSourceMappingResponse;
import software.amazon.awssdk.services.lambda.model.GetEventSourceMappingResponse;
import software.amazon.awssdk.services.lambda.model.ResourceNotFoundException;
import software.amazon.awssdk.services.lambda.model.UpdateEventSourceMappingRequest;

import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Creates a lambda event source mapping.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::lambda-event-source-mapping event-source-mapping-example
 *         function: $(aws::lambda-function lambda-function-event-source-mapping-example)
 *         batch-size: 10
 *         event-source-arn: "$(aws::sqs-queue sqs-event-source-mapping-example | queue-arn)"
 *     end
 */
@Type("lambda-event-source-mapping")
public class EventSourceMappingResource extends AwsResource implements Copyable<GetEventSourceMappingResponse> {
    private FunctionResource function;
    private Integer functionVersion;
    private FunctionAlias alias;
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
     * The Lambda Function to be associated with. Required if Alias is not provided.
     */
    @Updatable
    public FunctionResource getFunction() {
        return function;
    }

    public void setFunction(FunctionResource function) {
        this.function = function;
    }

    /**
     * The Lambda Function version to be associated with.
     */
    @Updatable
    public Integer getFunctionVersion() {
        return functionVersion;
    }

    public void setFunctionVersion(Integer functionVersion) {
        this.functionVersion = functionVersion;
    }

    /**
     * The Lambda Function to be associated with. Required if function is not provided.
     */
    @Updatable
    public FunctionAlias getAlias() {
        return alias;
    }

    public void setAlias(FunctionAlias alias) {
        this.alias = alias;
    }

    /**
     * The batch size for the event to invoke the Lambda Function.
     */
    @Required
    @Updatable
    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * Enable or disable the Lambda Event Source Mapping. Defaults to ``True``.
     */
    @Updatable
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
     * The event source arn to be connected with the function.
     */
    @Required
    public String getEventSourceArn() {
        return eventSourceArn;
    }

    public void setEventSourceArn(String eventSourceArn) {
        this.eventSourceArn = eventSourceArn;
    }

    /**
     * The starting position in terms of the connected resource for the Lambda Function to be invoked. Required if source event is DynamoDb or Kinesis. Valid values are ``TRIM_HORIZON`` or ``LATEST`` or ``AT_TIMESTAMP``.
     */
    @ValidStrings({"TRIM_HORIZON", "LATEST", "AT_TIMESTAMP"})
    public String getStartingPosition() {
        return startingPosition != null ? startingPosition.toUpperCase() : null;
    }

    public void setStartingPosition(String startingPosition) {
        this.startingPosition = startingPosition;
    }

    /**
     * Starting timestamp to invoke the Lambda Function. Required only if `starting-position` set to ``AT_TIMESTAMP``.
     */
    public String getStartingPositionTimestamp() {
        return startingPositionTimestamp;
    }

    public void setStartingPositionTimestamp(String startingPositionTimestamp) {
        this.startingPositionTimestamp = startingPositionTimestamp;
    }

    /**
     * The ID of the Lambda Event Source Mapping.
     */
    @Id
    @Output
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * Last modified date of the Lambda Event Source Mapping.
     */
    @Output
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Last processing of the Lambda Event Source Mapping.
     */
    @Output
    public String getLastProcessingResult() {
        return lastProcessingResult;
    }

    public void setLastProcessingResult(String lastProcessingResult) {
        this.lastProcessingResult = lastProcessingResult;
    }

    /**
     * Current state of the Lambda Event Source Mapping.
     */
    @Output
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * Reason for the current state transition.
     */
    @Output
    public String getStateTransitionReason() {
        return stateTransitionReason;
    }

    public void setStateTransitionReason(String stateTransitionReason) {
        this.stateTransitionReason = stateTransitionReason;
    }

    /**
     * The arn for the Lambda Event Source Mapping.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The arn of the Lambda Function associated the Lambda Event Source Mapping.
     */
    @Output
    public String getFunctionArn() {
        return functionArn;
    }

    public void setFunctionArn(String functionArn) {
        this.functionArn = functionArn;
    }

    @Override
    public void copyFrom(GetEventSourceMappingResponse response) {
        setId(response.uuid());
        setBatchSize(response.batchSize());
        setEventSourceArn(response.eventSourceArn());
        setLastModified(Date.from(response.lastModified()));
        setLastProcessingResult(response.lastProcessingResult());
        setState(response.state());
        setStateTransitionReason(response.stateTransitionReason());
        setEnabled(getState().equals("Enabled"));
        setFunctionArn(response.functionArn());
        String versionOrAlias = getFunctionArn().split(":function:")[1].split(":").length > 1 ? getFunctionArn().split(":function:")[1].split(":")[1] : "";
        Integer version = getIntVal(versionOrAlias);
        if (!ObjectUtils.isBlank(versionOrAlias) && version == null ) {
            setAlias(findById(FunctionAlias.class, response.functionArn()));
        } else {
            setFunction(findById(FunctionResource.class, getFunctionArn().split(":function:")[1].split(":")[0]));
            setFunctionVersion(version);
        }
    }

    private Integer getIntVal(String val) {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Override
    public boolean refresh() {
        LambdaClient client = createClient(LambdaClient.class);

        try {
            GetEventSourceMappingResponse response = client.getEventSourceMapping(
                r -> r.uuid(getId())
            );

            copyFrom(response);
        } catch (ResourceNotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        LambdaClient client = createClient(LambdaClient.class);

        CreateEventSourceMappingRequest.Builder builder = CreateEventSourceMappingRequest.builder()
            .batchSize(getBatchSize())
            .enabled(getEnabled())
            .eventSourceArn(getEventSourceArn());

        if (getAlias() != null) {
            builder = builder.functionName(getAlias().getArn());
        } else {
            if (getFunctionVersion() == null) {
                builder = builder.functionName(getFunction().getName());
            } else {
                builder = builder.functionName(getFunction().getArnNoVersion() + ":" + getFunctionVersion());
            }
        }

        if (!ObjectUtils.isBlank(getStartingPosition())) {
            builder = builder.startingPosition(getStartingPosition());
        }

        if (!ObjectUtils.isBlank(getStartingPositionTimestamp())) {
            builder = builder.startingPositionTimestamp(Instant.now());
        }

        CreateEventSourceMappingResponse response = client.createEventSourceMapping(builder.build());

        setId(response.uuid());

        state.save();

        waitToSave(client);
    }

    @Override
    public void update(GyroUI ui, State state, Resource resource, Set<String> changedFieldNames) {
        if (!getState().equals("Enabled") && !getState().equals("Disabled")) {
            throw new GyroException(String.format("Event source mapping in '%s' state. Please try again.", getState()));
        }

        LambdaClient client = createClient(LambdaClient.class);

        UpdateEventSourceMappingRequest.Builder builder = UpdateEventSourceMappingRequest.builder()
            .uuid(getId())
            .enabled(getEnabled())
            .batchSize(getBatchSize());

        if (getAlias() != null) {
            builder = builder.functionName(getAlias().getArn());
        } else {
            if (getFunctionVersion() == null) {
                builder = builder.functionName(getFunction().getName());
            } else {
                builder = builder.functionName(getFunction().getArnNoVersion() + ":" + getFunctionVersion());
            }
        }

        client.updateEventSourceMapping(builder.build());

        waitToSave(client);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        LambdaClient client = createClient(LambdaClient.class);

        client.deleteEventSourceMapping(
            r -> r.uuid(getId())
        );
    }

    private void waitToSave(LambdaClient client) {
        boolean waitResult = Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(10, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> client.getEventSourceMapping(
                r -> r.uuid(getId()))
                .state().equals(getEnabled() ? "Enabled" : "Disabled")
            );

        if (!waitResult) {
            throw new GyroException(String.format("Unable to reach '%s' state for lambda event source mapping - %s", getEnabled() ? "Enabled" : "Disabled", getId()));
        }

        copyFrom(client.getEventSourceMapping(r -> r.uuid(getId())));
    }
}
