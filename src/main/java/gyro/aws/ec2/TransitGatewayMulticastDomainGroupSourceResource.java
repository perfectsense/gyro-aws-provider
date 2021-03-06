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

import java.util.Set;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.TransitGatewayMulticastGroup;

/**
 * Creates a transit gateway multicast domain group source.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *      group-source
 *         group-ip-address: "224.0.0.0"
 *         network-interface: $(aws::network-interface example-network-interface)
 *      end
 */
public class TransitGatewayMulticastDomainGroupSourceResource extends AwsResource
    implements Copyable<TransitGatewayMulticastGroup> {

    private String groupIpAddress;
    private NetworkInterfaceResource networkInterface;

    /**
     * The IP address to assign to the multicast domain group. The address should be in the ``224.0.0.0/4`` or ``ff00::/8`` CIDR range.
     */
    @Required
    public String getGroupIpAddress() {
        return groupIpAddress;
    }

    public void setGroupIpAddress(String groupIpAddress) {
        this.groupIpAddress = groupIpAddress;
    }

    /**
     * The network interface that sends the multicast traffic.
     */
    @Required
    public NetworkInterfaceResource getNetworkInterface() {
        return networkInterface;
    }

    public void setNetworkInterface(NetworkInterfaceResource networkInterface) {
        this.networkInterface = networkInterface;
    }

    @Override
    public void copyFrom(TransitGatewayMulticastGroup model) {
        setGroupIpAddress(model.groupIpAddress());
        setNetworkInterface(findById(NetworkInterfaceResource.class, model.networkInterfaceId()));
    }

    @Override
    public String primaryKey() {
        return String.format("with address: %s", getGroupIpAddress());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);

        client.registerTransitGatewayMulticastGroupSources(r -> r
            .groupIpAddress(getGroupIpAddress())
            .networkInterfaceIds(getNetworkInterface().getId())
            .transitGatewayMulticastDomainId(((TransitGatewayMulticastDomainResource) parent()).getId()));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);

        client.deregisterTransitGatewayMulticastGroupSources(r -> r
            .groupIpAddress(getGroupIpAddress())
            .networkInterfaceIds(getNetworkInterface().getId())
            .transitGatewayMulticastDomainId(((TransitGatewayMulticastDomainResource) parent()).getId()));
    }
}
