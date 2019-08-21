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
import gyro.core.validation.ValidationError;
import org.apache.commons.lang.NotImplementedException;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.ConnectionNotification;
import software.amazon.awssdk.services.ec2.model.CreateVpcEndpointConnectionNotificationResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpcEndpointConnectionNotificationsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
    private String arn;
    private Set<String> connectionEvents;
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
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
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
        setConnectionEvents(connectionNotification.connectionEvents() != null ? new HashSet<>(connectionNotification.connectionEvents()) : null);
        setArn(connectionNotification.connectionNotificationArn());
        setId(connectionNotification.connectionNotificationId());
        setState(connectionNotification.connectionNotificationStateAsString());
        setType(connectionNotification.connectionNotificationTypeAsString());
        setEndpoint(!ObjectUtils.isBlank(connectionNotification.vpcEndpointId()) ? findById(EndpointResource.class, connectionNotification.vpcEndpointId()) : null);
        setEndpointService(!ObjectUtils.isBlank(connectionNotification.serviceId()) ? findById(EndpointServiceResource.class, connectionNotification.serviceId()) : null);
    }

    @Override
    public boolean refresh() {
        throw new NotImplementedException();
    }

    @Override
    public void create(GyroUI ui, State state) {
        throw new NotImplementedException();
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        throw new NotImplementedException();
    }

    @Override
    public void delete(GyroUI ui, State state) {
        throw new NotImplementedException();
    }

    private ConnectionNotification getConnectionNotification(Ec2Client client) {
        ConnectionNotification connectionNotification = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load connection notification.");
        }

        try {
            DescribeVpcEndpointConnectionNotificationsResponse response = client.describeVpcEndpointConnectionNotifications(
                r -> r.connectionNotificationId(getId())
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

    @Override
    public List<ValidationError> validate() {
        List<ValidationError> errors = new ArrayList<>();

        if ((getEndpoint() == null && getEndpointService() == null)
            || (getEndpoint() != null && getEndpointService() != null)) {
            errors.add(new ValidationError(this, null, "Either 'endpoint' or 'endpoint-service' needs to be set. Not both at a time."));
        }

        if (getConnectionEvents().stream().anyMatch(o -> !masterEventSet.contains(o))) {
            errors.add(new ValidationError(this, null, "The values - (" + getConnectionEvents().stream().filter(o -> !masterEventSet.contains(o)).collect(Collectors.joining(" , "))
                + ") is invalid for parameter 'connection-events'. Valid values [ '" + String.join("', '", masterEventSet) + "' ]."));
        }

        return errors;
    }
}
