/*
 * Copyright 2022, Brightspot.
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

package gyro.aws.eventbridge;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Set;

import gyro.aws.Copyable;
import gyro.aws.iam.PolicyResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.CreateEventBusRequest;
import software.amazon.awssdk.services.eventbridge.model.CreateEventBusResponse;
import software.amazon.awssdk.services.eventbridge.model.DescribeEventBusResponse;
import software.amazon.awssdk.services.eventbridge.model.ResourceNotFoundException;
import software.amazon.awssdk.utils.IoUtils;

/**
 * Create an event bus.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::event-bus event-bus-example
 *         name: "event-bus-example"
 *         policy: "policy.json"
 *         tags: {
 *             Name: event-bus-example
 *         }
 *     end
 *
 */
@Type("event-bus")
public class EventBusResource extends EventBridgeTaggableResource implements Copyable<DescribeEventBusResponse> {

    private String name;
    private String eventSource;
    private String policy;

    // Read-only
    private String arn;

    /**
     * The name of the event bus.
     */
    @Required
    @Id
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Event source for the event bus.
     */
    public String getEventSource() {
        return eventSource;
    }

    public void setEventSource(String eventSource) {
        this.eventSource = eventSource;
    }

    /**
     * The policy document. A policy path or policy string is allowed.
     */
    @Updatable
    public String getPolicy() {
        if (this.policy != null && this.policy.contains(".json")) {
            try (InputStream input = openInput(this.policy)) {
                this.policy = PolicyResource.formatPolicy(IoUtils.toUtf8String(input));
                return this.policy;
            } catch (IOException err) {
                throw new GyroException(MessageFormat
                    .format("Event Bus - {0} policy error. Unable to read policy from path [{1}]", getName(), policy));
            }
        } else {
            return PolicyResource.formatPolicy(this.policy);
        }
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    /**
     * The arn for the event bus.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public void copyFrom(DescribeEventBusResponse model) {
        setPolicy(model.policy());
        setArn(model.arn());
        setName(model.name());

        refreshTags();
    }

    @Override
    public boolean refresh() {
        EventBridgeClient client = createClient(EventBridgeClient.class);

        try {
            DescribeEventBusResponse response = client.describeEventBus(r -> r.name(getName()));

            copyFrom(response);

            return true;
        } catch (ResourceNotFoundException ex) {
            // ignore
        }

        return false;
    }

    @Override
    public String resourceArn() {
        return getArn();
    }

    @Override
    public void doCreate(GyroUI ui, State state) throws Exception {

        EventBridgeClient client = createClient(EventBridgeClient.class);

        CreateEventBusRequest.Builder builder = CreateEventBusRequest.builder()
            .name(getName());

        if (!StringUtils.isBlank(getEventSource())) {
            builder = builder.eventSourceName(getEventSource());
        }

        CreateEventBusResponse response = client.createEventBus(builder.build());

        setArn(response.eventBusArn());

        state.save();

        String policy = getPolicy();
        if (!StringUtils.isBlank(policy)) {
            client.putPermission(r -> r.eventBusName(getName()).policy(policy));
        }
    }

    @Override
    public void doUpdate(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

        EventBridgeClient client = createClient(EventBridgeClient.class);

        if (changedFieldNames.contains("policy")) {
            client.putPermission(r -> r.eventBusName(getName()).policy(getPolicy()));
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {

        EventBridgeClient client = createClient(EventBridgeClient.class);

        client.deleteEventBus(r -> r.name(getName()));
    }
}
