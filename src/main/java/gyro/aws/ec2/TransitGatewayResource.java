package gyro.aws.ec2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.scope.State;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

/**
 * Creates a transit gateway.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::transit-gateway example-transit-gateway
 *         amazonSideAsn: "64513"
 *         enable-dns-support: true
 *         description "new example transit gateway"
 *     end
 */
@Type("transit-gateway")
public class TransitGatewayResource extends Ec2TaggableResource<TransitGateway> implements Copyable<TransitGateway> {

    private Long amazonSideAsn;
    private Boolean enableDnsSupport;
    private Boolean enableVpnEcmpSupport;
    private Boolean enableDefaultRouteTableAssoc;
    private Boolean enableDefaultRouteTableProp;
    private Boolean enableAutoAcceptSharedAttach;
    private String description;

    // Read only
    private String id;
    private String arn;
    private String ownerId;
    private String associationDefaultRouteTableId;
    private String propagationDefaultRouteTableId;


    /**
     * A private Autonomous System Number (ASN) for the Amazon side of a BGP session.
     * The range is 64512 to 65534 for 16-bit ASNs and 4200000000 to 4294967294 for 32-bit ASNs.
     */
    public Long getAmazonSideAsn() {
        return amazonSideAsn;
    }

    public void setAmazonSideAsn(Long amazonSideAsn) {
        this.amazonSideAsn = amazonSideAsn;
    }

    /**
     * Enable the VPC to resolve public IPv4 DNS host names to private IPv4 addresses when queried from instances in another VPC attached to the transit gateway.
     */
    public Boolean getEnableDnsSupport() {
        if (enableDnsSupport == null) {
            enableDnsSupport = true;
        }

        return enableDnsSupport;
    }

    public void setEnableDnsSupport(Boolean enableDnsSupport) {
        this.enableDnsSupport = enableDnsSupport;
    }

    /**
     * Enable Equal Cost Multipath (ECMP) routing support between VPN connections.
     */
    public Boolean getEnableVpnEcmpSupport() {
        if (enableVpnEcmpSupport == null) {
            enableVpnEcmpSupport = true;
        }

        return enableVpnEcmpSupport;
    }

    public void setEnableVpnEcmpSupport(Boolean enableVpnEcmpSupport) {
        this.enableVpnEcmpSupport = enableVpnEcmpSupport;
    }

    /**
     * Enable to automatically associate transit gateway attachments with the default route table for the transit gateway.
     */
    public Boolean getEnableDefaultRouteTableAssoc() {
        if (enableDefaultRouteTableAssoc == null) {
            enableDefaultRouteTableAssoc = true;
        }

        return enableDefaultRouteTableAssoc;
    }

    public void setEnableDefaultRouteTableAssoc(Boolean enableDefaultRouteTableAssoc) {
        this.enableDefaultRouteTableAssoc = enableDefaultRouteTableAssoc;
    }

    /**
     * Enable to automatically propagate transit gateway attachments to the default route table for the transit gateway.
     */
    public Boolean getEnableDefaultRouteTableProp() {
        if (enableDefaultRouteTableProp == null) {
            enableDefaultRouteTableProp = true;
        }

        return enableDefaultRouteTableProp;
    }

    public void setEnableDefaultRouteTableProp(Boolean enableDefaultRouteTableProp) {
        this.enableDefaultRouteTableProp = enableDefaultRouteTableProp;
    }

    /**
     * Enable to automatically accept cross-account attachments.
     */
    public Boolean getEnableAutoAcceptSharedAttach() {
        if (enableAutoAcceptSharedAttach == null) {
            enableAutoAcceptSharedAttach = false;
        }

        return enableAutoAcceptSharedAttach;
    }

    public void setEnableAutoAcceptSharedAttach(Boolean enableAutoAcceptSharedAttach) {
        this.enableAutoAcceptSharedAttach = enableAutoAcceptSharedAttach;
    }

    /**
     * The description of the transit gateway.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The ID of the transit gateway.
     */
    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The Amazon Resource Name (ARN) of the transit gateway.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * The ID of the AWS account ID that owns the transit gateway.
     */
    @Output
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * The ID of the default association route table.
     */
    @Output
    public String getAssociationDefaultRouteTableId() {
        return associationDefaultRouteTableId;
    }

    public void setAssociationDefaultRouteTableId(String associationDefaultRouteTableId) {
        this.associationDefaultRouteTableId = associationDefaultRouteTableId;
    }

    /**
     * The ID of the default propagation route table.
     */
    @Output
    public String getPropagationDefaultRouteTableId() {
        return propagationDefaultRouteTableId;
    }

    public void setPropagationDefaultRouteTableId(String propagationDefaultRouteTableId) {
        this.propagationDefaultRouteTableId = propagationDefaultRouteTableId;
    }

    private boolean checkState(TransitGatewayState state, Ec2Client client) {
        TransitGateway gateway = client.describeTransitGateways(r -> r.transitGatewayIds(Collections.singleton(getId()))).transitGateways().get(0);
        return gateway.state().equals(state);
    }

    @Override
    protected String getResourceId() {
        return getId();
    }

    @Override
    protected boolean doRefresh() {
        Ec2Client client = createClient(Ec2Client.class);
        DescribeTransitGatewaysResponse response = client.describeTransitGateways(r -> r.transitGatewayIds(Collections.singleton(getId())));
        if (response.transitGateways().isEmpty()) {
            return false;
        }
        copyFrom(response.transitGateways().get(0));
        return true;
    }

    @Override
    protected void doCreate(GyroUI ui, State state) {
        Ec2Client client = createClient(Ec2Client.class);

        TransitGatewayRequestOptions options = TransitGatewayRequestOptions.builder()
                .amazonSideAsn(getAmazonSideAsn())
                .autoAcceptSharedAttachments(getEnableAutoAcceptSharedAttach() == true ? AutoAcceptSharedAttachmentsValue.ENABLE : AutoAcceptSharedAttachmentsValue.DISABLE)
                .defaultRouteTableAssociation(getEnableDefaultRouteTableAssoc() == true ? DefaultRouteTableAssociationValue.ENABLE : DefaultRouteTableAssociationValue.DISABLE)
                .defaultRouteTablePropagation(getEnableDefaultRouteTableProp() == true ? DefaultRouteTablePropagationValue.ENABLE : DefaultRouteTablePropagationValue.DISABLE)
                .dnsSupport(getEnableDnsSupport() == true ? DnsSupportValue.ENABLE : DnsSupportValue.DISABLE)
                .vpnEcmpSupport(getEnableVpnEcmpSupport() == true ? VpnEcmpSupportValue.ENABLE : VpnEcmpSupportValue.DISABLE)
                .build();

        CreateTransitGatewayResponse response = client.createTransitGateway(CreateTransitGatewayRequest.builder()
                .description(getDescription())
                .options(options)
                .build()
        );

        setId(response.transitGateway().transitGatewayId());
        setArn(response.transitGateway().transitGatewayArn());
        setOwnerId(response.transitGateway().ownerId());
        setAssociationDefaultRouteTableId(response.transitGateway().options().associationDefaultRouteTableId());
        setPropagationDefaultRouteTableId(response.transitGateway().options().propagationDefaultRouteTableId());

        boolean waitResult = Wait.atMost(3, TimeUnit.MINUTES)
                .checkEvery(10, TimeUnit.SECONDS)
                .prompt(false)
                .until(() -> checkState(TransitGatewayState.AVAILABLE, client));

        if (!waitResult) {
            throw new GyroException("Unable to reach 'available' state for transit gateway - " + getId());
        }
    }

    @Override
    protected void doUpdate(GyroUI ui, State state, AwsResource config, Set changedProperties) {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        Ec2Client client = createClient(Ec2Client.class);
        client.deleteTransitGateway(DeleteTransitGatewayRequest.builder().transitGatewayId(getId()).build());

        boolean waitResult = Wait.atMost(3, TimeUnit.MINUTES)
                .checkEvery(10, TimeUnit.SECONDS)
                .prompt(false)
                .until(() -> checkState(TransitGatewayState.DELETED, client));

        if (!waitResult) {
            throw new GyroException("Unable to reach 'deleted' state for transit gateway - " + getId());
        }
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();
        Long asn = getAmazonSideAsn();
        if (asn != null && ((asn < 64512L || asn > 4294967294L) || (asn < 4200000000L && asn > 65534L)) ) {
            errors.add(new ValidationError(this, "Amazon side ASN", "The private ASN should be within the 64512-65534 or 4200000000-4294967294 range."));
        }
        return errors;
    }

    @Override
    public void copyFrom(TransitGateway model) {
        setId(model.transitGatewayId());
        setOwnerId(model.ownerId());
        setArn(model.transitGatewayArn());
        setAmazonSideAsn(model.options().amazonSideAsn());
        setDescription(model.description());
        setEnableAutoAcceptSharedAttach(model.options().autoAcceptSharedAttachments().equals(AutoAcceptSharedAttachmentsValue.ENABLE));
        setEnableDefaultRouteTableAssoc(model.options().defaultRouteTableAssociation().equals(DefaultRouteTableAssociationValue.ENABLE));
        setEnableDefaultRouteTableProp(model.options().defaultRouteTablePropagation().equals(DefaultRouteTablePropagationValue.ENABLE));
        setEnableDnsSupport(model.options().dnsSupport().equals(DnsSupportValue.ENABLE));
        setEnableVpnEcmpSupport(model.options().vpnEcmpSupport().equals(VpnEcmpSupportValue.ENABLE));
        setAssociationDefaultRouteTableId(model.options().associationDefaultRouteTableId());
        setPropagationDefaultRouteTableId(model.options().propagationDefaultRouteTableId());
        refreshTags();
    }
}
