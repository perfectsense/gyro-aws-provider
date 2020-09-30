/*
 * Copyright 2020, Perfect Sense, Inc.
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
import java.util.List;
import java.util.Set;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.RoleResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.scope.State;
import gyro.core.validation.ConflictsWith;
import gyro.core.validation.Required;
import gyro.core.validation.ValidNumbers;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateFlowLogsRequest;
import software.amazon.awssdk.services.ec2.model.DeleteFlowLogsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeFlowLogsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeFlowLogsResponse;
import software.amazon.awssdk.services.ec2.model.FlowLog;
import software.amazon.awssdk.services.ec2.model.FlowLogsResourceType;
import software.amazon.awssdk.services.ec2.model.LogDestinationType;
import software.amazon.awssdk.services.ec2.model.TrafficType;

/**
 * Creates a flow log for VPCs, Subnets or Network Interfaces.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    aws::flow-log example-flow-log
 *        log-destination: "arn:aws:s3:::example-bucket-flow-logs/"
 *        destination-type: "s3"
 *        log-format: '${version} ${account-id} ${interface-id} ${srcaddr} ${dstaddr} ${srcport} ${dstport} ${protocol} ${packets} ${bytes} ${start} ${end} ${action} ${log-status}'
 *        max-aggregation-interval: 60
 *        vpc: $(aws::vpc vpc-example-flow-log)
 *        traffic-type: "all"
 *
 *        tags: {
 *            "example-tag": "example-value"
 *        }
 *    end
 */
@Type("flow-log")
public class FlowLogResource extends Ec2TaggableResource implements Copyable<FlowLog> {

    private RoleResource role;
    private String logDestination;
    private LogDestinationType destinationType;
    private String logFormat;
    private Integer maxAggregationInterval;
    private VpcResource vpc;
    private SubnetResource subnet;
    private NetworkInterfaceResource networkInterface;
    private TrafficType trafficType;

    // Read-only
    private String id;

    /**
     * The IAM role that permits Amazon EC2 to publish flow logs when ``destination-type`` is set to ``cloud-watch-logs``.
     */
    public RoleResource getRole() {
        return role;
    }

    public void setRole(RoleResource role) {
        this.role = role;
    }

    /**
     * The destination to which flow log data should be published.
     */
    @Required
    public String getLogDestination() {
        return logDestination;
    }

    public void setLogDestination(String logDestination) {
        this.logDestination = logDestination;
    }

    /**
     * The type of destination to which flow log data should be published.
     */
    @Required
    @ValidStrings({"cloud-watch-logs", "s3"})
    public LogDestinationType getDestinationType() {
        return destinationType;
    }

    public void setDestinationType(LogDestinationType destinationType) {
        this.destinationType = destinationType;
    }

    /**
     * The fields to include in the flow log record when ``destination-type`` is set to ``s3``.
     */
    public String getLogFormat() {
        return logFormat;
    }

    public void setLogFormat(String logFormat) {
        this.logFormat = logFormat;
    }

    /**
     * The maximum interval of time during which a flow of packets is captured and aggregated into a flow log record. Valid values are ``60`` or ``600``.
     */
    @ValidNumbers({60, 600})
    public Integer getMaxAggregationInterval() {
        return maxAggregationInterval;
    }

    public void setMaxAggregationInterval(Integer maxAggregationInterval) {
        this.maxAggregationInterval = maxAggregationInterval;
    }

    /**
     * The vpc for which the flow log should be created.
     */
    @ConflictsWith({"subnet", "network-interface"})
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    /**
     * The subnet for which the flow log should be created.
     */
    @ConflictsWith({"vpc", "network-interface"})
    public SubnetResource getSubnet() {
        return subnet;
    }

    public void setSubnet(SubnetResource subnet) {
        this.subnet = subnet;
    }

    /**
     * The network interface for which the flow log should be created.
     */
    @ConflictsWith({"subnet", "vpc"})
    public NetworkInterfaceResource getNetworkInterface() {
        return networkInterface;
    }

    public void setNetworkInterface(NetworkInterfaceResource networkInterface) {
        this.networkInterface = networkInterface;
    }

    /**
     * The type of traffic to log.
     */
    @Required
    @ValidStrings({"ACCEPT", "REJECT", "ALL"})
    public TrafficType getTrafficType() {
        return trafficType;
    }

    public void setTrafficType(TrafficType trafficType) {
        this.trafficType = trafficType;
    }

    /**
     * The ID of the flow log.
     */
    @Output
    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected String getResourceId() {
        return getId();
    }

    @Override
    public void copyFrom(FlowLog model) {
        setRole(findById(RoleResource.class, model.deliverLogsPermissionArn()));
        setLogDestination(model.logDestination());
        setDestinationType(model.logDestinationType());
        setLogFormat(model.logFormat());
        setMaxAggregationInterval(model.maxAggregationInterval());
        setTrafficType(model.trafficType());
        setId(model.flowLogId());

        if (model.resourceId().contains("vpc")) {
            setVpc(findById(VpcResource.class, model.resourceId()));

        } else if (model.resourceId().contains("subnet")) {
            setSubnet(findById(SubnetResource.class, model.resourceId()));

        } else {
            setNetworkInterface(findById(NetworkInterfaceResource.class, model.resourceId()));
        }
    }

    @Override
    protected boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        DescribeFlowLogsResponse describeFlowLogsResponse = client.describeFlowLogs(DescribeFlowLogsRequest.builder()
                .flowLogIds(getId())
                .build());

        if (!describeFlowLogsResponse.hasFlowLogs()) {
            return false;
        }

        copyFrom(describeFlowLogsResponse.flowLogs().get(0));

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        CreateFlowLogsRequest.Builder builder = CreateFlowLogsRequest.builder()
                .maxAggregationInterval(getMaxAggregationInterval())
                .logDestinationType(getDestinationType())
                .logDestination(getLogDestination())
                .trafficType(getTrafficType())
                .resourceType(getVpc() != null ? FlowLogsResourceType.VPC :
                        (getSubnet() != null ? FlowLogsResourceType.SUBNET :
                                FlowLogsResourceType.NETWORK_INTERFACE))
                .logFormat(getLogFormat())
                .resourceIds(getVpc() != null ? getVpc().getResourceId() :
                        (getSubnet() != null ? getSubnet().getResourceId() :
                                getNetworkInterface().getResourceId()));

        if (getDestinationType().equals(LogDestinationType.CLOUD_WATCH_LOGS)) {
            builder = builder.deliverLogsPermissionArn(getRole().getArn());
        }

        setId(client.createFlowLogs(builder.build()).flowLogIds().get(0));
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteFlowLogs(DeleteFlowLogsRequest.builder().flowLogIds(getId()).build());
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (!configuredFields.contains("vpc") && !configuredFields.contains("subnet")
                && !configuredFields.contains("network-interface")) {
            errors.add(new ValidationError(this, null, "At least one of 'vpc', 'subnet' or 'network-interface' is required"));
        }

        if (configuredFields.contains("role") && getDestinationType().equals(LogDestinationType.S3)) {
            errors.add(new ValidationError(this, "role", "'role' can only be set if 'destination-type' is set to 'cloud-watch-logs''"));
        }

        if (configuredFields.contains("log-format") && getDestinationType().equals(LogDestinationType.CLOUD_WATCH_LOGS)) {
            errors.add(new ValidationError(this, "log-format", "'log-format' can only be set if 'destination-type' is set to 's3''"));
        }

        return errors;
    }
}
