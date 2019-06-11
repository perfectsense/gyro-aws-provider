package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateVpcPeeringConnectionResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpcPeeringConnectionsResponse;
import software.amazon.awssdk.services.ec2.model.Ec2Exception;
import software.amazon.awssdk.services.ec2.model.VpcPeeringConnection;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

import java.util.Collections;
import java.util.Set;

/**
 * Create a Peering Connection between two VPC.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::peering-connection peering-connection-example
 *         vpc: $(aws::vpc vpc-example-for-peering-connection-1)
 *         peer-vpc: $(aws::vpc vpc-example-for-peering-connection-2)
 *         region: 'us-east-1'
 *
 *         tags: {
 *             Name: "peering-connection-example"
 *         }
 *     end
 */
@Type("peering-connection")
public class PeeringConnectionResource extends Ec2TaggableResource<VpcPeeringConnection> implements Copyable<VpcPeeringConnection> {

    private VpcResource vpc;
    private VpcResource peerVpc;
    private String peeringConnectionId;

    /**
     * Requester VPC. See `Creating and Accepting Peering Connection <https://docs.aws.amazon.com/vpc/latest/peering/create-vpc-peering-connection.html/>`_. (Required)
     */
    public VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(VpcResource vpc) {
        this.vpc = vpc;
    }

    /**
     * Accepter VPC. (Required)
     */
    public VpcResource getPeerVpc() {
        return peerVpc;
    }

    public void setPeerVpc(VpcResource peerVpc) {
        this.peerVpc = peerVpc;
    }

    /**
     * The ID of the Peering Connection.
     */
    @Id
    @Output
    public String getPeeringConnectionId() {
        return peeringConnectionId;
    }

    public void setPeeringConnectionId(String peeringConnectionId) {
        this.peeringConnectionId = peeringConnectionId;
    }

    @Override
    protected String getId() {
        return getPeeringConnectionId();
    }

    @Override
    public void copyFrom(VpcPeeringConnection vpcPeeringConnection) {
        setPeeringConnectionId(vpcPeeringConnection.vpcPeeringConnectionId());
        setVpc(findById(VpcResource.class, vpcPeeringConnection.requesterVpcInfo().vpcId()));
        setPeerVpc(findById(VpcResource.class, vpcPeeringConnection.accepterVpcInfo().vpcId()));
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
    protected void doCreate() {
        Ec2Client client = createClient(Ec2Client.class);

        CreateVpcPeeringConnectionResponse response = client.createVpcPeeringConnection(
            r -> r.vpcId(getVpc().getVpcId())
                .peerVpcId(getPeerVpc().getVpcId())
                .peerOwnerId(getAccountNumber())
                .peerRegion(getPeerVpc().getRegion())
        );

        VpcPeeringConnection vpcPeeringConnection = response.vpcPeeringConnection();
        setPeeringConnectionId(vpcPeeringConnection.vpcPeeringConnectionId());

        client.acceptVpcPeeringConnection(
            r -> r.vpcPeeringConnectionId(getPeeringConnectionId())
        );
    }

    @Override
    protected void doUpdate(AwsResource config, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        Ec2Client client = createClient(Ec2Client.class);

        client.deleteVpcPeeringConnection(r -> r.vpcPeeringConnectionId(getPeeringConnectionId()));
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("peering connection");

        if (!ObjectUtils.isBlank(getPeeringConnectionId())) {
            sb.append(" - ").append(getPeeringConnectionId());
        }

        return sb.toString();
    }

    private String getAccountNumber() {
        StsClient client = createClient(StsClient.class);
        GetCallerIdentityResponse response = client.getCallerIdentity();
        return response.account();
    }

    private VpcPeeringConnection getVpcPeeringConnection(Ec2Client client) {
        VpcPeeringConnection vpcPeeringConnection = null;

        if (ObjectUtils.isBlank(getPeeringConnectionId())) {
            throw new GyroException("vpc-peering-connection-id is missing, unable to load peering connection.");
        }

        try {
            DescribeVpcPeeringConnectionsResponse response = client.describeVpcPeeringConnections(
                r -> r.vpcPeeringConnectionIds(Collections.singleton(getPeeringConnectionId()))
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
}
