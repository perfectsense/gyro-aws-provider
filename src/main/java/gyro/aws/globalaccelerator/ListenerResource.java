/*
 * Copyright 2021, Brightspot.
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

package gyro.aws.globalaccelerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.globalaccelerator.GlobalAcceleratorClient;
import software.amazon.awssdk.services.globalaccelerator.model.ClientAffinity;
import software.amazon.awssdk.services.globalaccelerator.model.CreateListenerResponse;
import software.amazon.awssdk.services.globalaccelerator.model.DescribeListenerResponse;
import software.amazon.awssdk.services.globalaccelerator.model.InvalidPortRangeException;
import software.amazon.awssdk.services.globalaccelerator.model.Listener;
import software.amazon.awssdk.services.globalaccelerator.model.ListenerNotFoundException;
import software.amazon.awssdk.services.globalaccelerator.model.PortRange;
import software.amazon.awssdk.services.globalaccelerator.model.Protocol;

/**
 * Create a global accelerator listener.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::global-accelerator-listener accelerator
 *         accelerator: $(aws::global-accelerator accelerator)
 *         protocol: TCP
 *
 *         port-range
 *             from-port: 80
 *             to-port: 80
 *         end
 *
 *         port-range
 *             from-port: 443
 *             to-port: 443
 *         end
 *     end
 */
@Type("global-accelerator-listener")
public class ListenerResource extends AwsResource implements Copyable<Listener> {

    private AcceleratorResource accelerator;
    private ClientAffinity clientAffinity;
    private List<ListenerPortRange> portRange;
    private Protocol protocol;

    // -- Output fields
    private String arn;

    @Override
    public boolean refresh() {
        GlobalAcceleratorClient client =
            createClient(GlobalAcceleratorClient.class, "us-west-2", "https://globalaccelerator.us-west-2.amazonaws.com");

        try {
            DescribeListenerResponse response = client.describeListener(r -> r.listenerArn(getArn()));
            copyFrom(response.listener());
        } catch (ListenerNotFoundException ex) {
            return false;
        }

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        GlobalAcceleratorClient client =
            createClient(GlobalAcceleratorClient.class, "us-west-2", "https://globalaccelerator.us-west-2.amazonaws.com");

        CreateListenerResponse response = client.createListener(r -> r
            .acceleratorArn(getAccelerator().getArn())
            .clientAffinity(getClientAffinity())
            .protocol(getProtocol())
            .portRanges(portRanges())
        );

        setArn(response.listener().listenerArn());
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

        GlobalAcceleratorClient client =
            createClient(GlobalAcceleratorClient.class, "us-west-2", "https://globalaccelerator.us-west-2.amazonaws.com");

        try {
            client.updateListener(r -> r
                .listenerArn(getArn())
                .clientAffinity(getClientAffinity())
                .protocol(getProtocol())
                .portRanges(portRanges())
            );
        } catch (InvalidPortRangeException ex) {
            throw new GyroException("Invalid port range. Ensure port is not used by an endpoint before removing it from a listener.");
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        GlobalAcceleratorClient client =
            createClient(GlobalAcceleratorClient.class, "us-west-2", "https://globalaccelerator.us-west-2.amazonaws.com");

        client.deleteListener(r -> r.listenerArn(getArn()));
    }

    /**
     * The accelerator to add this listener to.
     */
    public AcceleratorResource getAccelerator() {
        return accelerator;
    }

    public void setAccelerator(AcceleratorResource accelerator) {
        this.accelerator = accelerator;
    }

    /**
     * Whether to stick clients to the same endpoint.
     */
    @Updatable
    public ClientAffinity getClientAffinity() {
        return clientAffinity;
    }

    public void setClientAffinity(ClientAffinity clientAffinity) {
        this.clientAffinity = clientAffinity;
    }

    /**
     * The port ranges that the accelerator should accept connections to.
     */
    @Updatable
    public List<ListenerPortRange> getPortRange() {
        if (portRange == null) {
            portRange = new ArrayList<>();
        }

        return portRange;
    }

    public void setPortRange(List<ListenerPortRange> portRange) {
        this.portRange = portRange;
    }

    /**
     * The protocol for connections to the accelerator.
     */
    @Updatable
    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    /**
     * The arn for this listener.
     */
    @Id
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    private List<PortRange> portRanges() {
        return getPortRange().stream()
            .map(ListenerPortRange::portRange)
            .collect(Collectors.toList());
    }

    @Override
    public void copyFrom(Listener listener) {
        setArn(listener.listenerArn());
        setProtocol(listener.protocol());
        setClientAffinity(listener.clientAffinity());

        getPortRange().clear();
        listener.portRanges().forEach(p -> {
            ListenerPortRange pr = newSubresource(ListenerPortRange.class);
            pr.setToPort(p.toPort());
            pr.setFromPort(p.fromPort());

            getPortRange().add(pr);
        });
    }
}
