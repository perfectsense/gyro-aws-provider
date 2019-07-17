package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.scope.State;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.ConnectionNotification;
import software.amazon.awssdk.services.ec2.model.CreateVpcEndpointConnectionNotificationResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpcEndpointConnectionNotificationsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
 *         connection-notification-arn: "arn:aws:sns:us-west-2:242040583208:gyro-instance-state"
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
    private String connectionNotificationArn;
    private Set<String> connectionEvents;
    private String connectionNotificationId;
    private String connectionNotificationState;
    private String connectionNotificationType;

    private final Set<String> masterEventSet = new HashSet<>(Arrays.asList(
        "Accept",
        "Connect",
        "Delete",
        "Reject"
    ));

    /**
     * Endpoint Service. Either Endpoint or Endpoint Service is required.
     */
    public EndpointServiceResource getEndpointService() {
        return endpointService;
    }

    public void setEndpointService(EndpointServiceResource endpointService) {
        this.endpointService = endpointService;
    }

    /**
     * Endpoint. Either Endpoint or Endpoint Service is required.
     */
    public EndpointResource getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(EndpointResource endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * The ARN of the SNS topic. (Required)
     */
    @Updatable
    public String getConnectionNotificationArn() {
        return connectionNotificationArn;
    }

    public void setConnectionNotificationArn(String connectionNotificationArn) {
        this.connectionNotificationArn = connectionNotificationArn;
    }

    /**
     * The events this notification is subscribing to. Defaults to all values. Valid values [ 'Accept', 'Connect', 'Delete' ] (Required)
     */
    @Updatable
    public Set<String> getConnectionEvents() {
        if (connectionEvents == null) {
            connectionEvents = new HashSet<>(masterEventSet);
        }

        return connectionEvents;
    }

    public void setConnectionEvents(Set<String> connectionEvents) {
        this.connectionEvents = connectionEvents;
    }

    /**
     * The ID of the Connection Notification.
     */
    @Id
    @Output
    public String getConnectionNotificationId() {
        return connectionNotificationId;
    }

    public void setConnectionNotificationId(String connectionNotificationId) {
        this.connectionNotificationId = connectionNotificationId;
    }

    /**
     * The state of the Connection Notification.
     */
    @Output
    public String getConnectionNotificationState() {
        return connectionNotificationState;
    }

    public void setConnectionNotificationState(String connectionNotificationState) {
        this.connectionNotificationState = connectionNotificationState;
    }

    /**
     * The notification type of the Connection Notification.
     */
    @Output
    public String getConnectionNotificationType() {
        return connectionNotificationType;
    }

    public void setConnectionNotificationType(String connectionNotificationType) {
        this.connectionNotificationType = connectionNotificationType;
    }

    @Override
    public void copyFrom(ConnectionNotification connectionNotification) {
        setConnectionNotificationId(connectionNotification.connectionNotificationId());
        setConnectionEvents(connectionNotification.connectionEvents() != null ? new HashSet<>(connectionNotification.connectionEvents()) : null);
        setConnectionNotificationArn(connectionNotification.connectionNotificationArn());
        setConnectionNotificationId(connectionNotification.connectionNotificationId());
        setConnectionNotificationState(connectionNotification.connectionNotificationStateAsString());
        setConnectionNotificationType(connectionNotification.connectionNotificationTypeAsString());
        setEndpoint(!ObjectUtils.isBlank(connectionNotification.vpcEndpointId()) ? findById(EndpointResource.class, connectionNotification.vpcEndpointId()) : null);
        setEndpointService(!ObjectUtils.isBlank(connectionNotification.serviceId()) ? findById(EndpointServiceResource.class, connectionNotification.serviceId()) : null);
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
                r -> r.vpcEndpointId(getEndpoint().getEndpointId())
                    .connectionEvents(getConnectionEvents())
                    .connectionNotificationArn(getConnectionNotificationArn())
            );
        } else if (getEndpointService() != null) {
            response = client.createVpcEndpointConnectionNotification(
                r -> r.serviceId(getEndpointService().getServiceId())
                    .connectionEvents(getConnectionEvents())
                    .connectionNotificationArn(getConnectionNotificationArn())
            );
        } else {
            throw new GyroException("vpc-endpoint or service-id required.");
        }

        setConnectionNotificationId(response.connectionNotification().connectionNotificationId());
        setConnectionNotificationType(response.connectionNotification().connectionNotificationTypeAsString());
        setConnectionNotificationState(response.connectionNotification().connectionNotificationStateAsString());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Ec2Client client = createClient(Ec2Client.class);

        validate();

        client.modifyVpcEndpointConnectionNotification(
            r -> r.connectionNotificationId(getConnectionNotificationId())
                .connectionEvents(getConnectionEvents())
                .connectionNotificationArn(getConnectionNotificationArn())
        );
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteVpcEndpointConnectionNotifications(
            r -> r.connectionNotificationIds(Collections.singleton(getConnectionNotificationId()))
        );
    }

    private ConnectionNotification getConnectionNotification(Ec2Client client) {
        ConnectionNotification connectionNotification = null;

        if (ObjectUtils.isBlank(getConnectionNotificationId())) {
            throw new GyroException("connection-notification-id is missing, unable to load connection notification.");
        }

        try {
            DescribeVpcEndpointConnectionNotificationsResponse response = client.describeVpcEndpointConnectionNotifications(
                r -> r.connectionNotificationId(getConnectionNotificationId())
            );

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

    private void validate() {
        if ((getEndpoint() == null && getEndpointService() == null)
            || (getEndpoint() != null && getEndpointService() != null)) {
            throw new GyroException("Either 'endpoint' or 'endpoint-service' needs to be set. Not both at a time.");
        }

        if (getConnectionEvents().stream().anyMatch(o -> !masterEventSet.contains(o))) {
            throw new GyroException("The values - (" + getConnectionEvents().stream().filter(o -> !masterEventSet.contains(o)).collect(Collectors.joining(" , "))
                + ") is invalid for parameter 'connection-events'. Valid values [ '" + String.join("', '", masterEventSet) + "' ].");
        }
    }
}
