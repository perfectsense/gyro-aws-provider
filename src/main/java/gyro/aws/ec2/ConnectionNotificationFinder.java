package gyro.aws.ec2;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.ConnectionNotification;

import java.util.List;
import java.util.Map;

/**
 * Query vpc connection notification.
 *
 * .. code-block:: gyro
 *
 *    connection-notification: $(aws::vpc-connection-notification EXTERNAL/* | connection-notification-arn = '')
 */
@Type("vpc-connection-notification")
public class ConnectionNotificationFinder extends AwsFinder<Ec2Client, ConnectionNotification, ConnectionNotificationResource> {

    private String connectionNotificationArn;
    private String connectionNotificationId;
    private String connectionNotificationState;
    private String connectionNotificationType;
    private String serviceId;
    private String vpcEndpointId;

    /**
     * The ARN of SNS topic for the notification.
     */
    public String getConnectionNotificationArn() {
        return connectionNotificationArn;
    }

    public void setConnectionNotificationArn(String connectionNotificationArn) {
        this.connectionNotificationArn = connectionNotificationArn;
    }

    /**
     * The ID of the notification.
     */
    public String getConnectionNotificationId() {
        return connectionNotificationId;
    }

    public void setConnectionNotificationId(String connectionNotificationId) {
        this.connectionNotificationId = connectionNotificationId;
    }

    /**
     * The state of the notification . Valid values are ``Enabled`` or ``Disabled``.
     */
    public String getConnectionNotificationState() {
        return connectionNotificationState;
    }

    public void setConnectionNotificationState(String connectionNotificationState) {
        this.connectionNotificationState = connectionNotificationState;
    }

    /**
     * The type of notification (Topic).
     */
    public String getConnectionNotificationType() {
        return connectionNotificationType;
    }

    public void setConnectionNotificationType(String connectionNotificationType) {
        this.connectionNotificationType = connectionNotificationType;
    }

    /**
     * The ID of the endpoint service.
     */
    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * The ID of the VPC endpoint.
     */
    public String getVpcEndpointId() {
        return vpcEndpointId;
    }

    public void setVpcEndpointId(String vpcEndpointId) {
        this.vpcEndpointId = vpcEndpointId;
    }

    @Override
    protected List<ConnectionNotification> findAllAws(Ec2Client client) {
        return client.describeVpcEndpointConnectionNotifications().connectionNotificationSet();
    }

    @Override
    protected List<ConnectionNotification> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeVpcEndpointConnectionNotifications(r -> r.filters(createFilters(filters))).connectionNotificationSet();
    }
}