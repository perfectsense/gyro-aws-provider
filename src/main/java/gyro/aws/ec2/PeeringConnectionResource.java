package gyro.aws.ec2;

import gyro.aws.AwsResource;
import gyro.core.Type;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.CreateVpcPeeringConnectionResponse;
import software.amazon.awssdk.services.ec2.model.DescribeVpcPeeringConnectionsResponse;
import software.amazon.awssdk.services.ec2.model.VpcPeeringConnection;

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
 *         owner-id: '572681481110'
 *         region: 'us-east-1'
 *
 *         tags: {
 *             Name: "peering-connection-example"
 *         }
 *     end
 */
@Type("peering-connection")
public class PeeringConnectionResource extends Ec2TaggableResource<VpcPeeringConnection> {

    private VpcResource vpc;
    private VpcResource peerVpc;
    private String ownerId;
    private String region;
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
     * Accepter Account id. (Required)
     */
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * Accepter Region. (Required)
     */
    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * The id of the peering connection.
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
    protected boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);

        DescribeVpcPeeringConnectionsResponse response = client.describeVpcPeeringConnections(r -> r.vpcPeeringConnectionIds(getId()));

        if (!response.vpcPeeringConnections().isEmpty()) {
            VpcPeeringConnection vpcPeeringConnection = response.vpcPeeringConnections().get(0);
            setPeeringConnectionId(vpcPeeringConnection.vpcPeeringConnectionId());
            setOwnerId(vpcPeeringConnection.accepterVpcInfo().ownerId());
            setVpc(findById(VpcResource.class, vpcPeeringConnection.accepterVpcInfo().vpcId()));

            return true;
        }

        return false;
    }

    @Override
    protected void doCreate() {
        Ec2Client client = createClient(Ec2Client.class);

        CreateVpcPeeringConnectionResponse response = client.createVpcPeeringConnection(
            r -> r.vpcId(getVpc().getVpcId())
                .peerVpcId(getPeerVpc().getVpcId())
                .peerOwnerId(getOwnerId())
                .peerRegion(getRegion())
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

        if (getPeeringConnectionId() != null) {
            sb.append(getPeeringConnectionId());

        } else {
            sb.append("peering connection");
        }

        return sb.toString();
    }
}
