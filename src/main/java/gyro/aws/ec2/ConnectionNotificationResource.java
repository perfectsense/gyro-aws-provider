package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.resource.Id;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.ConnectionNotification;
import software.amazon.awssdk.services.ec2.model.CreateVpcEndpointConnectionNotificationResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpcEndpointConnectionNotificationsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.VpcEndpoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Creates a connection notification for an endpoint or an endpoint service.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::connection-notification connection-notification-example
 *         vpc-endpoint-id: $(aws::endpoint endpoint-example-interface | endpoint-id)
 *         connection-notification-arn: "arn:aws:sns:us-west-2:242040583208:gyro-instance-state"
 *         connection-events: [
 *             "Accept"
 *         ]
 *     end
 *
 */
@Type("connection-notification")
public class ConnectionNotificationResource extends AwsResource implements Copyable<ConnectionNotification> {

    private String serviceId;
    private EndpointResource vpcEndpoint;
    private String connectionNotificationArn;
    private List<String> connectionEvents;
    private String connectionNotificationId;
    private String connectionNotificationState;
    private String connectionNotificationType;

    private final Set<String> masterEventSet = new HashSet<>(Arrays.asList(
        "Accept",
        "Connect",
        "Delete"
    ));

    /**
     * The id of the endpoint service. Either endpoint id or endpoint service id is required.
     */
    @Output
    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * The id of the vpc endpoint. Either endpoint id or endpoint service id is required.
     */
    public EndpointResource getVpcEndpoint() {
        return vpcEndpoint;
    }

    public void setVpcEndpoint(EndpointResource vpcEndpoint) {
        this.vpcEndpoint = vpcEndpoint;
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
     * The id of the connection notification.
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
     * The state of the connection notification.
     */
    @Output
    public String getConnectionNotificationState() {
        return connectionNotificationState;
    }

    public void setConnectionNotificationState(String connectionNotificationState) {
        this.connectionNotificationState = connectionNotificationState;
    }

    /**
     * The notification type of the connection notification.
     */
    @Output
    public String getConnectionNotificationType() {
        return connectionNotificationType;
    }

    public void setConnectionNotificationType(String connectionNotificationType) {
        this.connectionNotificationType = connectionNotificationType;
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
    public void create() {
        Ec2Client client = createClient(Ec2Client.class);

        validate();

        CreateVpcEndpointConnectionNotificationResponse response = null;

        if (getVpcEndpoint() != null) {
            response = client.createVpcEndpointConnectionNotification(
                r -> r.vpcEndpointId(getVpcEndpoint().getEndpointId())
                    .connectionEvents(getConnectionEvents())
                    .connectionNotificationArn(getConnectionNotificationArn())
            );
        } else if (!ObjectUtils.isBlank(getServiceId())) {
            response = client.createVpcEndpointConnectionNotification(
                r -> r.serviceId(getServiceId())
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
    public void update(Resource current, Set<String> changedFieldNames) {
        Ec2Client client = createClient(Ec2Client.class);

        validate();

        client.modifyVpcEndpointConnectionNotification(
            r -> r.connectionNotificationId(getConnectionNotificationId())
                .connectionEvents(getConnectionEvents())
                .connectionNotificationArn(getConnectionNotificationArn())
        );
    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteVpcEndpointConnectionNotifications(
            r -> r.connectionNotificationIds(Collections.singleton(getConnectionNotificationId()))
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("connection notification");

        if (!ObjectUtils.isBlank(getConnectionNotificationId())) {
            sb.append(" - ").append(getConnectionNotificationId());
        }

        return sb.toString();
    }

    @Override
    public void copyFrom(ConnectionNotification connectionNotification) {
        setConnectionNotificationId(connectionNotification.connectionNotificationId());
        setConnectionEvents(connectionNotification.connectionEvents());
        setConnectionNotificationArn(connectionNotification.connectionNotificationArn());
        setConnectionNotificationId(connectionNotification.connectionNotificationId());
        setConnectionNotificationState(connectionNotification.connectionNotificationStateAsString());
        setConnectionNotificationType(connectionNotification.connectionNotificationTypeAsString());
        setVpcEndpoint(connectionNotification.vpcEndpointId() != null ? findById(EndpointResource.class, connectionNotification.vpcEndpointId()) : null);
        setServiceId(connectionNotification.serviceId());
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
        if ((getVpcEndpoint() == null && ObjectUtils.isBlank(getServiceId()))
            || (getVpcEndpoint() != null && !ObjectUtils.isBlank(getServiceId()))) {
            throw new GyroException("Either 'vpc-endpoint' or 'service-id' needs to be set. Not both at a time.");
        }

        if (getConnectionEvents().stream().anyMatch(o -> !masterEventSet.contains(o))) {
            throw new GyroException("The values - (" + String.join(" , ", getConnectionEvents())
                + ") is invalid for parameter 'connection-events'. Valid values [ '" + String.join("', '") + "' ].");
        }
    }
}
