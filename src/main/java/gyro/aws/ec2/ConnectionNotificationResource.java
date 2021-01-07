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

package gyro.aws.ec2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.sns.TopicResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.ConnectionNotification;
import software.amazon.awssdk.services.ec2.model.CreateVpcEndpointConnectionNotificationResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpcEndpointConnectionNotificationsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;

/**
 * Creates a vpc connection notification for an endpoint or an endpoint service.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::vpc-connection-notification connection-notification-example
 *         vpc-endpoint: $(aws::vpc-endpoint endpoint-example-interface)
 *         topic: "arn:aws:sns:us-east-2:242040583208:beam-instance-state"
 *         connection-events: [
 *             "Accept"
 *         ]
 *     end
 *
 */
@Type("vpc-connection-notification")
public class ConnectionNotificationResource extends AwsResource implements Copyable<ConnectionNotification> {

    private EndpointServiceResource endpointService;
    private EndpointResource endpoint;
    private TopicResource topic;
    private List<String> connectionEvents;

    // Read-only
    private String id;
    private String state;
    private String type;

    private final Set<String> masterEventSet = new HashSet<>(Arrays.asList(
        "Accept",
        "Connect",
        "Delete",
        "Reject"
    ));

    /**
     * The Endpoint Service. Either Endpoint or Endpoint Service is required.
     */
    @ConflictsWith("endpoint")
    public EndpointServiceResource getEndpointService() {
        return endpointService;
    }

    public void setEndpointService(EndpointServiceResource endpointService) {
        this.endpointService = endpointService;
    }

    /**
     * The Endpoint. Either Endpoint or Endpoint Service is required.
     */
    @ConflictsWith("endpoint-service")
    public EndpointResource getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(EndpointResource endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * The SNS topic.
     */
    @Required
    @Updatable
    public TopicResource getTopic() {
        return topic;
    }

    public void setTopic(TopicResource topic) {
        this.topic = topic;
    }

    /**
     * The events this notification is subscribing to. Defaults to all values.
     */
    @Updatable
    @ValidStrings({ "Accept", "Connect", "Delete" })
    public List<String> getConnectionEvents() {
        if (connectionEvents == null) {
            connectionEvents = new ArrayList<>(masterEventSet);
        }

        return connectionEvents;
    }

    public void setConnectionEvents(List<String> connectionEvents) {
        this.connectionEvents = connectionEvents;
    }

    /**
     * The ID of the Connection Notification.
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
     * The state of the Connection Notification.
     */
    @Output
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * The notification type of the Connection Notification.
     */
    @Output
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void copyFrom(ConnectionNotification connectionNotification) {
        setId(connectionNotification.connectionNotificationId());
        setConnectionEvents(connectionNotification.connectionEvents() != null
            ? new ArrayList<>(connectionNotification.connectionEvents()) : null);
        setTopic(findById(TopicResource.class, connectionNotification.connectionNotificationId()));
        setId(connectionNotification.connectionNotificationId());
        setState(connectionNotification.connectionNotificationStateAsString());
        setType(connectionNotification.connectionNotificationTypeAsString());
        setEndpoint(!ObjectUtils.isBlank(connectionNotification.vpcEndpointId()) ? findById(
            EndpointResource.class, connectionNotification.vpcEndpointId()) : null);
        setEndpointService(!ObjectUtils.isBlank(connectionNotification.serviceId())
            ? findById(EndpointServiceResource.class, connectionNotification.serviceId()) : null);
    }

    @Override
    public boolean refresh() {
        Ec2Client client = createClient(Ec2Client.class);

        ConnectionNotification connectionNotification = getConnectionNotification(client);

        if (connectionNotification == null) {
            return false;
        }

        copyFrom(connectionNotification);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        validate();

        CreateVpcEndpointConnectionNotificationResponse response = null;

        if (getEndpoint() != null) {
            response = client.createVpcEndpointConnectionNotification(
                r -> r.vpcEndpointId(getEndpoint().getId())
                    .connectionEvents(getConnectionEvents())
                    .connectionNotificationArn(getTopic().getArn())
            );
        } else if (getEndpointService() != null) {
            response = client.createVpcEndpointConnectionNotification(
                r -> r.serviceId(getEndpointService().getId())
                    .connectionEvents(getConnectionEvents())
                    .connectionNotificationArn(getTopic().getArn())
            );
        } else {
            throw new GyroException("endpoint or endpoint-service required.");
        }

        setId(response.connectionNotification().connectionNotificationId());
        setType(response.connectionNotification().connectionNotificationTypeAsString());
        setState(response.connectionNotification().connectionNotificationStateAsString());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Ec2Client client = createClient(Ec2Client.class);

        validate();

        client.modifyVpcEndpointConnectionNotification(
            r -> r.connectionNotificationId(getId())
                .connectionEvents(getConnectionEvents())
                .connectionNotificationArn(getTopic().getArn())
        );
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteVpcEndpointConnectionNotifications(
            r -> r.connectionNotificationIds(Collections.singleton(getId()))
        );
    }

    private ConnectionNotification getConnectionNotification(Ec2Client client) {
        ConnectionNotification connectionNotification = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load connection notification.");
        }

        try {
            DescribeVpcEndpointConnectionNotificationsResponse response = client
                .describeVpcEndpointConnectionNotifications(r -> r.connectionNotificationId(getId()));

            if (!response.connectionNotificationSet().isEmpty()) {
                connectionNotification = response.connectionNotificationSet().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return connectionNotification;
    }

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if ((getEndpoint() == null && getEndpointService() == null)
            || (getEndpoint() != null && getEndpointService() != null)) {
            errors.add(new ValidationError(this, null,
                "Either 'endpoint' or 'endpoint-service' needs to be set. Not both at a time."));
        }

        if (getConnectionEvents().stream().anyMatch(o -> !masterEventSet.contains(o))) {
            errors.add(new ValidationError(this, null,
                "The values - (" + getConnectionEvents().stream().filter(o -> !masterEventSet.contains(o))
                    .collect(Collectors.joining(" , "))
                    + ") is invalid for parameter 'connection-events'. Valid values [ '" + String.join(
                    "', '", masterEventSet) + "' ]."));
        }

        return errors;
    }
}
