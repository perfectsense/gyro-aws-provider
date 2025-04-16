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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.TimeoutSettings;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateVpcPeeringConnectionResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpcPeeringConnectionsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.VpcPeeringConnection;

/**
 * Create a Peering Connection between two VPC.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::vpc-peering-connection peering-connection-example
 *         vpc: $(aws::vpc vpc-example-for-peering-connection-1)
 *         peer-vpc: $(aws::vpc vpc-example-for-peering-connection-2)
 *
 *         tags: {
 *             Name: "peering-connection-example"
 *         }
 *     end
 */
@Type("vpc-peering-connection")
public class PeeringConnectionResource extends Ec2TaggableResource<VpcPeeringConnection>
    implements Copyable<VpcPeeringConnection> {

    private VpcResource vpc;
    private VpcResource peerVpc;
    private Boolean allowDnsResolutionFromRemoteVpc;
    private Boolean allowEgressFromLocalClassicLinkToRemoteVpc;
    private Boolean allowEgressFromLocalVpcToRemoteClassicLink;
    private Boolean peerAllowDnsResolutionFromRemoteVpc;
    private Boolean peerAllowEgressFromLocalClassicLinkToRemoteVpc;
    private Boolean peerAllowEgressFromLocalVpcToRemoteClassicLink;

    // Read-only
    private String id;

    /**
     * The requester VPC. See `Creating and Accepting Peering Connection <https://docs.aws.amazon.com/vpc/latest/peering/create-vpc-peering-connection.html/>`_.
     */
    @Required
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    /**
     * The accepter VPC.
     */
    @Required
    public VpcResource getPeerVpc() {
        return peerVpc;
    }

    public void setPeerVpc(VpcResource peerVpc) {
        this.peerVpc = peerVpc;
    }

    /**
     * When set to ``true``, enables a local VPC to resolve public DNS hostnames to private IP addresses when queried from instances in the peer VPC. Defaults to ``false``.
     */
    @Updatable
    public Boolean getAllowDnsResolutionFromRemoteVpc() {
        if (allowDnsResolutionFromRemoteVpc == null) {
            allowDnsResolutionFromRemoteVpc = false;
        }

        return allowDnsResolutionFromRemoteVpc;
    }

    public void setAllowDnsResolutionFromRemoteVpc(Boolean allowDnsResolutionFromRemoteVpc) {
        this.allowDnsResolutionFromRemoteVpc = allowDnsResolutionFromRemoteVpc;
    }

    /**
     * When set to ``true``, enables outbound communication from an EC2-Classic instance that's linked to a local VPC using ClassicLink to instances in a peer VPC. Defaults to ``false``.
     */
    @Updatable
    public Boolean getAllowEgressFromLocalClassicLinkToRemoteVpc() {
        if (allowEgressFromLocalClassicLinkToRemoteVpc == null) {
            allowEgressFromLocalClassicLinkToRemoteVpc = false;
        }

        return allowEgressFromLocalClassicLinkToRemoteVpc;
    }

    public void setAllowEgressFromLocalClassicLinkToRemoteVpc(Boolean allowEgressFromLocalClassicLinkToRemoteVpc) {
        this.allowEgressFromLocalClassicLinkToRemoteVpc = allowEgressFromLocalClassicLinkToRemoteVpc;
    }

    /**
     * When set to ``true``, enables outbound communication from instances in a local VPC to an EC2-Classic instance that's linked to a peer VPC using ClassicLink. Defaults to ``false``.
     */
    @Updatable
    public Boolean getAllowEgressFromLocalVpcToRemoteClassicLink() {
        if (allowEgressFromLocalVpcToRemoteClassicLink == null) {
            allowEgressFromLocalVpcToRemoteClassicLink = false;
        }

        return allowEgressFromLocalVpcToRemoteClassicLink;
    }

    public void setAllowEgressFromLocalVpcToRemoteClassicLink(Boolean allowEgressFromLocalVpcToRemoteClassicLink) {
        this.allowEgressFromLocalVpcToRemoteClassicLink = allowEgressFromLocalVpcToRemoteClassicLink;
    }

    /**
     * When set to ``true``, enables a local VPC to resolve public DNS hostnames to private IP addresses when queried from instances in the peer VPC. Defaults to ``false``.
     */
    @Updatable
    public Boolean getPeerAllowDnsResolutionFromRemoteVpc() {
        if (peerAllowDnsResolutionFromRemoteVpc == null) {
            peerAllowDnsResolutionFromRemoteVpc = false;
        }

        return peerAllowDnsResolutionFromRemoteVpc;
    }

    public void setPeerAllowDnsResolutionFromRemoteVpc(Boolean peerAllowDnsResolutionFromRemoteVpc) {
        this.peerAllowDnsResolutionFromRemoteVpc = peerAllowDnsResolutionFromRemoteVpc;
    }

    /**
     * When set to ``true``, enables outbound communication from an EC2-Classic instance that's linked to a local VPC using ClassicLink to instances in a peer VPC. Defaults to ``false``.
     */
    @Updatable
    public Boolean getPeerAllowEgressFromLocalClassicLinkToRemoteVpc() {
        if (peerAllowEgressFromLocalClassicLinkToRemoteVpc == null) {
            peerAllowEgressFromLocalClassicLinkToRemoteVpc = false;
        }

        return peerAllowEgressFromLocalClassicLinkToRemoteVpc;
    }

    public void setPeerAllowEgressFromLocalClassicLinkToRemoteVpc(Boolean peerAllowEgressFromLocalClassicLinkToRemoteVpc) {
        this.peerAllowEgressFromLocalClassicLinkToRemoteVpc = peerAllowEgressFromLocalClassicLinkToRemoteVpc;
    }

    /**
     * When set to ``true``, enables outbound communication from instances in a local VPC to an EC2-Classic instance that's linked to a peer VPC using ClassicLink. Defaults to ``false``.
     */
    @Updatable
    public Boolean getPeerAllowEgressFromLocalVpcToRemoteClassicLink() {
        if (peerAllowEgressFromLocalVpcToRemoteClassicLink == null) {
            peerAllowEgressFromLocalVpcToRemoteClassicLink = false;
        }

        return peerAllowEgressFromLocalVpcToRemoteClassicLink;
    }

    public void setPeerAllowEgressFromLocalVpcToRemoteClassicLink(Boolean peerAllowEgressFromLocalVpcToRemoteClassicLink) {
        this.peerAllowEgressFromLocalVpcToRemoteClassicLink = peerAllowEgressFromLocalVpcToRemoteClassicLink;
    }

    /**
     * The ID of the Peering Connection.
     */
    @Id
    @Output
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
    public void copyFrom(VpcPeeringConnection vpcPeeringConnection) {
        setId(vpcPeeringConnection.vpcPeeringConnectionId());
        setVpc(findById(VpcResource.class, vpcPeeringConnection.requesterVpcInfo().vpcId()));
        setAllowDnsResolutionFromRemoteVpc(vpcPeeringConnection.requesterVpcInfo().peeringOptions()
            .allowDnsResolutionFromRemoteVpc());
        setAllowEgressFromLocalClassicLinkToRemoteVpc(vpcPeeringConnection.requesterVpcInfo()
            .peeringOptions().allowEgressFromLocalClassicLinkToRemoteVpc());
        setAllowEgressFromLocalVpcToRemoteClassicLink(vpcPeeringConnection.requesterVpcInfo()
            .peeringOptions().allowEgressFromLocalVpcToRemoteClassicLink());
        setPeerVpc(findById(VpcResource.class, vpcPeeringConnection.accepterVpcInfo().vpcId()));
        setPeerAllowDnsResolutionFromRemoteVpc(vpcPeeringConnection.accepterVpcInfo()
            .peeringOptions().allowDnsResolutionFromRemoteVpc());
        setPeerAllowEgressFromLocalClassicLinkToRemoteVpc(vpcPeeringConnection.accepterVpcInfo()
            .peeringOptions().allowEgressFromLocalClassicLinkToRemoteVpc());
        setPeerAllowEgressFromLocalVpcToRemoteClassicLink(vpcPeeringConnection.accepterVpcInfo()
            .peeringOptions().allowEgressFromLocalVpcToRemoteClassicLink());

        refreshTags();
    }

    @Override
    protected boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        VpcPeeringConnection vpcPeeringConnection = getVpcPeeringConnection(client);

        if (vpcPeeringConnection == null) {
            return false;
        }

        copyFrom(vpcPeeringConnection);

        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        CreateVpcPeeringConnectionResponse response = client.createVpcPeeringConnection(
            r -> r.vpcId(getVpc().getId()).peerVpcId(getPeerVpc().getId()).peerOwnerId(getPeerVpc().getAccount())
                .peerRegion(getPeerVpc().getRegion())
        );

        VpcPeeringConnection vpcPeeringConnection = response.vpcPeeringConnection();
        setId(vpcPeeringConnection.vpcPeeringConnectionId());

        waitForStatus(client, "pending-acceptance");

        if (!getVpc().getRegion().equals(getPeerVpc().getRegion())) {
            try (Ec2Client accepterClient = createClient(Ec2Client.class, getPeerVpc().getRegion(), "")) {
                accepterClient.acceptVpcPeeringConnection(
                    r -> r.vpcPeeringConnectionId(getId()).overrideConfiguration()
                );
            }
        } else {
            client.acceptVpcPeeringConnection(
                r -> r.vpcPeeringConnectionId(getId())
            );

            waitForStatus(client, "active");
            modifyPeeringConnectionSettings(client);
        }
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set<String> changedProperties) {
        Ec2Client client = createClient(Ec2Client.class);

        modifyPeeringConnectionSettings(client);
    }

    @Override
    public void delete(GyroUI ui, State state) {
        try (Ec2Client client = createClient(Ec2Client.class)) {
            client.deleteVpcPeeringConnection(r -> r.vpcPeeringConnectionId(getId()));
        }
    }

    private VpcPeeringConnection getVpcPeeringConnection(Ec2Client client) {
        VpcPeeringConnection vpcPeeringConnection = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("id is missing, unable to load peering connection.");
        }

        try {
            DescribeVpcPeeringConnectionsResponse response = client.describeVpcPeeringConnections(
                r -> r.vpcPeeringConnectionIds(Collections.singleton(getId()))
            );

            if (!response.vpcPeeringConnections().isEmpty()) {
                vpcPeeringConnection = response.vpcPeeringConnections().get(0);
            }

        } catch (Ec2Exception ex) {
            if (!ex.getLocalizedMessage().contains("does not exist")) {
                throw ex;
            }
        }

        return vpcPeeringConnection;
    }

    private void modifyPeeringConnectionSettings(Ec2Client client) {
        client.modifyVpcPeeringConnectionOptions(r -> r.vpcPeeringConnectionId(getId())
            .accepterPeeringConnectionOptions(acp -> acp.allowDnsResolutionFromRemoteVpc(
                getPeerAllowDnsResolutionFromRemoteVpc())
                .allowEgressFromLocalClassicLinkToRemoteVpc(getPeerAllowEgressFromLocalClassicLinkToRemoteVpc())
                .allowEgressFromLocalVpcToRemoteClassicLink(getPeerAllowEgressFromLocalVpcToRemoteClassicLink()))
            .requesterPeeringConnectionOptions(req -> req.allowDnsResolutionFromRemoteVpc(
                getAllowDnsResolutionFromRemoteVpc())
                .allowEgressFromLocalClassicLinkToRemoteVpc(getAllowEgressFromLocalClassicLinkToRemoteVpc())
                .allowEgressFromLocalVpcToRemoteClassicLink(getAllowEgressFromLocalVpcToRemoteClassicLink()))
        );
    }

    private void waitForStatus(Ec2Client client, String status) {
        Wait.atMost(2, TimeUnit.MINUTES)
            .checkEvery(5, TimeUnit.SECONDS)
            .prompt(false)
            .resourceOverrides(this, TimeoutSettings.Action.CREATE)
            .until(() -> {
                DescribeVpcPeeringConnectionsResponse vpcPeeringConnectionsResponse = client.describeVpcPeeringConnections(
                    r -> r.vpcPeeringConnectionIds(getId())
                );
                if (!vpcPeeringConnectionsResponse.vpcPeeringConnections().isEmpty()) {
                    return false;
                } else {
                    return vpcPeeringConnectionsResponse.vpcPeeringConnections().get(0).status().code().toString().equals(status);
                }
            });
    }
}
