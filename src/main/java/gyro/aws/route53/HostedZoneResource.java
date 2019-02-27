package gyro.aws.route53;

import gyro.aws.AwsResource;
import gyro.core.BeamException;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import gyro.lang.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.CreateHostedZoneResponse;
import software.amazon.awssdk.services.route53.model.GetHostedZoneResponse;
import software.amazon.awssdk.services.route53.model.HostedZone;
import software.amazon.awssdk.services.route53.model.VPC;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Creates a Hosted Zone.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::hosted-zone hosted-zone-example
 *         hosted-zone-name: "hosted-zone-example.com"
 *         private-zone: true
 *         vpc
 *             vpc-id: $(aws::vpc vpc-hosted-zone-example | vpc-id)
 *             vpc-region: $(aws::credentials default | region)
 *         end
 *         comment: "hosted-zone-example.co comment modified"
 *     end
 *
 */
@ResourceName("hosted-zone")
public class HostedZoneResource extends AwsResource {

    private String delegationSetId;
    private String comment;
    private Boolean privateZone;
    private String hostedZoneId;
    private String hostedZoneName;
    private Long resourceRecordSetCount;
    private String description;
    private String servicePrincipal;
    private List<Route53VpcResource> vpc;

    /**
     * The id of a delegation set.
     */
    public String getDelegationSetId() {
        return delegationSetId;
    }

    public void setDelegationSetId(String delegationSetId) {
        this.delegationSetId = delegationSetId;
    }

    /**
     * Comment for the hosted Zone.
     */
    @ResourceDiffProperty(updatable = true, nullable = true)
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Is the the hosted zone private.
     */
    public Boolean getPrivateZone() {
        return privateZone;
    }

    public void setPrivateZone(Boolean privateZone) {
        this.privateZone = privateZone;
    }

    public String getHostedZoneId() {
        return hostedZoneId;
    }

    public void setHostedZoneId(String hostedZoneId) {
        this.hostedZoneId = hostedZoneId;
    }

    /**
     * The name of the hosted zone.
     */
    public String getHostedZoneName() {
        if (hostedZoneName != null) {
            hostedZoneName += hostedZoneName.endsWith(".") ? "" : ".";
        }

        return hostedZoneName;
    }

    public void setHostedZoneName(String hostedZoneName) {
        this.hostedZoneName = hostedZoneName;
    }

    public Long getResourceRecordSetCount() {
        return resourceRecordSetCount;
    }

    public void setResourceRecordSetCount(Long resourceRecordSetCount) {
        this.resourceRecordSetCount = resourceRecordSetCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getServicePrincipal() {
        return servicePrincipal;
    }

    public void setServicePrincipal(String servicePrincipal) {
        this.servicePrincipal = servicePrincipal;
    }

    /**
     * A list of vpc to be associated with the hosed zone
     *
     * @subresource gyro.aws.route53.Route53VpcResource
     */
    @ResourceDiffProperty(nullable = true, subresource = true)
    public List<Route53VpcResource> getVpc() {
        if (vpc == null) {
            vpc = new ArrayList<>();
        }

        return vpc;
    }

    public void setVpc(List<Route53VpcResource> vpc) {
        this.vpc = vpc;
    }

    @Override
    public boolean refresh() {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        GetHostedZoneResponse response = client.getHostedZone(
            r -> r.id(getHostedZoneId())
        );

        setDelegationSetId(response.delegationSet() != null ? response.delegationSet().id() : null);
        HostedZone hostedZone = response.hostedZone();
        setComment(hostedZone.config().comment());
        setPrivateZone(hostedZone.config().privateZone());
        setHostedZoneName(hostedZone.name());
        setResourceRecordSetCount(hostedZone.resourceRecordSetCount());
        setDescription(hostedZone.linkedService() != null ? hostedZone.linkedService().description() : null);
        setServicePrincipal(hostedZone.linkedService() != null ? hostedZone.linkedService().servicePrincipal() : null);

        loadVpcs(response.vpCs());

        return true;
    }

    @Override
    public void create() {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        //validate();

        CreateHostedZoneResponse response = client.createHostedZone(
            r -> r.name(getHostedZoneName())
                .delegationSetId(getDelegationSetId())
                .hostedZoneConfig(
                    o -> o.comment(getComment())
                        .privateZone(getPrivateZone())
                )
                .vpc(!getVpc().isEmpty()
                        ? VPC.builder()
                        .vpcId(getVpc().get(0).getVpcId())
                        .vpcRegion(getVpc().get(0).getVpcRegion())
                        .build()
                        : null
                )
            .callerReference(UUID.randomUUID().toString())
        );

        HostedZone hostedZone = response.hostedZone();

        setHostedZoneId(hostedZone.id());
        setResourceRecordSetCount(hostedZone.resourceRecordSetCount());
        setDescription(hostedZone.linkedService() != null ? hostedZone.linkedService().description() : null);
        setServicePrincipal(hostedZone.linkedService() != null ? hostedZone.linkedService().servicePrincipal() : null);
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        if (changedProperties.contains("comment")) {
            client.updateHostedZoneComment(
                r -> r.id(getHostedZoneId())
                    .comment(getComment() != null ? getComment() : "")
            );
        }
    }

    @Override
    public void delete() {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        client.deleteHostedZone(
            r -> r.id(getHostedZoneId())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("hosted zone");

        if (!ObjectUtils.isBlank(getHostedZoneName())) {
            sb.append(" [ ").append(getHostedZoneName()).append(" ]");
        }

        if (!ObjectUtils.isBlank(getHostedZoneId())) {
            sb.append(" - ").append(getHostedZoneId());
        }

        return sb.toString();
    }

    private void loadVpcs(List<VPC> vpcs) {
        getVpc().clear();

        for (VPC vpc :vpcs) {
            Route53VpcResource route53VpcResource = new Route53VpcResource(vpc.vpcId(), vpc.vpcRegionAsString());
            route53VpcResource.parent(this);
            getVpc().add(route53VpcResource);
        }
    }

    void saveVpc(String vpcId, String vpcRegion, boolean isAttach) {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        if (isAttach) {
            client.associateVPCWithHostedZone(
                r -> r.hostedZoneId(getHostedZoneId())
                    .vpc(
                        o -> o.vpcId(vpcId)
                            .vpcRegion(vpcRegion)
                    )
            );
        } else {
            client.disassociateVPCFromHostedZone(
                r -> r.hostedZoneId(getHostedZoneId())
                    .vpc(
                        o -> o.vpcId(vpcId)
                            .vpcRegion(vpcRegion)
                    )
            );
        }
    }

    private void validate() {
        if (getPrivateZone() && getVpc().isEmpty()) {
            throw new BeamException("if param 'private-zone' is set to 'true' at least one vpc needs to be provided.");
        }

        if (!getPrivateZone() && !getVpc().isEmpty()) {
            throw new BeamException("if param 'private-zone' is set to 'false' vpc's cannot be set.");
        }
    }
}
