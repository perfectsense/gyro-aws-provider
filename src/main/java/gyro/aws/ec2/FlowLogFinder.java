package gyro.aws.ec2;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import gyro.core.Type;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.FlowLog;

/**
 * Query vpc.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    flow-log: $(external-query aws::flow-log {traffic-type: "ALL"})
 */
@Type("flow-log")
public class FlowLogFinder extends Ec2TaggableAwsFinder<Ec2Client, FlowLog, FlowLogResource> {

    private String deliverLogStatus;
    private String logDestinationType;
    private String flowLogId;
    private String logGroupName;
    private String resourceId;
    private String trafficType;

    /**
     * The status of the logs delivery.
     */
    public String getDeliverLogStatus() {
        return deliverLogStatus;
    }

    public void setDeliverLogStatus(String deliverLogStatus) {
        this.deliverLogStatus = deliverLogStatus;
    }

    /**
     * The type of destination to which the flow log publishes data.
     */
    public String getLogDestinationType() {
        return logDestinationType;
    }

    public void setLogDestinationType(String logDestinationType) {
        this.logDestinationType = logDestinationType;
    }

    /**
     * The ID of the flow log.
     */
    public String getFlowLogId() {
        return flowLogId;
    }

    public void setFlowLogId(String flowLogId) {
        this.flowLogId = flowLogId;
    }

    /**
     * The name of the log group.
     */
    public String getLogGroupName() {
        return logGroupName;
    }

    public void setLogGroupName(String logGroupName) {
        this.logGroupName = logGroupName;
    }

    /**
     * The ID of the VPC, subnet, or network interface.
     */
    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    /**
     * The type of traffic.
     */
    public String getTrafficType() {
        return trafficType;
    }

    public void setTrafficType(String trafficType) {
        this.trafficType = trafficType;
    }

    @Override
    protected List<FlowLog> findAllAws(Ec2Client client) {
        return client.describeFlowLogsPaginator().flowLogs().stream().collect(Collectors.toList());
    }

    @Override
    protected List<FlowLog> findAws(Ec2Client client, Map<String, String> filters) {
        return client.describeFlowLogsPaginator(r -> r.filter(createFilters(filters))).flowLogs().stream().collect(Collectors.toList());
    }
}
