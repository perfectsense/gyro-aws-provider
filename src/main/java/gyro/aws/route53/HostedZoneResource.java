package gyro.aws.route53;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.CreateHostedZoneResponse;
import software.amazon.awssdk.services.route53.model.GetHostedZoneResponse;
import software.amazon.awssdk.services.route53.model.HostedZone;
import software.amazon.awssdk.services.route53.model.HostedZoneNotFoundException;
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
 *             vpc: $(aws::vpc vpc-hosted-zone-example)
 *             vpc-region: "us-east-2"
 *         end
 *         comment: "hosted-zone-example.comment modified"
 *     end
 *
 */
@Type("hosted-zone")
public class HostedZoneResource extends AwsResource implements Copyable<HostedZone> {

    private String delegationSetId;
    private String comment;
    private Boolean privateZone;
    private String hostedZoneId;
    private String hostedZoneName;
    private Long resourceRecordSetCount;
    private String description;
    private String servicePrincipal;
    private Route53VpcResource vpc;

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
    @Updatable
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

    /**
     * The id of the hosted zone.
     */
    @Id
    @Output
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

    /**
     * The record count of the hosted zone.
     */
    @Output
    public Long getResourceRecordSetCount() {
        return resourceRecordSetCount;
    }

    public void setResourceRecordSetCount(Long resourceRecordSetCount) {
        this.resourceRecordSetCount = resourceRecordSetCount;
    }

    /**
     * The description of a linked service to the hosted zone.
     */
    @Output
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The service principal of the hosted zone.
     */
    @Output
    public String getServicePrincipal() {
        return servicePrincipal;
    }

    public void setServicePrincipal(String servicePrincipal) {
        this.servicePrincipal = servicePrincipal;
    }

    /**
     * The Vpc to be associated with the hosed zone
     *
     * @subresource gyro.aws.route53.Route53VpcResource
     */
    public Route53VpcResource getVpc() {
        return vpc;
    }

    public void setVpc(Route53VpcResource vpc) {
        this.vpc = vpc;
    }

    @Override
    public boolean refresh() {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        GetHostedZoneResponse response = getHostedZoneResponse(client);

        if (response == null) {
            return false;
        }

        copyFrom(response.hostedZone());

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
                .vpc(getVpc() != null
                        ? VPC.builder()
                        .vpcId(getVpc().getVpc().getVpcId())
                        .vpcRegion(getVpc().getVpcRegion())
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
    public void update(Resource current, Set<String> changedFieldNames) {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        if (changedFieldNames.contains("comment")) {
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

    @Override
    public void copyFrom(HostedZone hostedZone) {
        setHostedZoneId(hostedZone.id());
        setComment(hostedZone.config().comment());
        setPrivateZone(hostedZone.config().privateZone());
        setHostedZoneName(hostedZone.name());
        setResourceRecordSetCount(hostedZone.resourceRecordSetCount());
        setDescription(hostedZone.linkedService() != null ? hostedZone.linkedService().description() : null);
        setServicePrincipal(hostedZone.linkedService() != null ? hostedZone.linkedService().servicePrincipal() : null);

        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        GetHostedZoneResponse response = getHostedZoneResponse(client);
        setDelegationSetId(response.delegationSet() != null ? response.delegationSet().id() : null);

        if (response.vpCs() != null && !response.vpCs().isEmpty()) {
            Route53VpcResource route53VpcResource = newSubresource(Route53VpcResource.class);
            route53VpcResource.copyFrom(response.vpCs().get(0));
            setVpc(route53VpcResource);
        }
    }

    private GetHostedZoneResponse getHostedZoneResponse(Route53Client client) {
        GetHostedZoneResponse hostedZoneResponse = null;

        if (ObjectUtils.isBlank(getHostedZoneId())) {
            throw new GyroException("hosted-zone-id is missing, unable to load hosted zone.");
        }

        try {
            hostedZoneResponse = client.getHostedZone(
                r -> r.id(getHostedZoneId())
            );

            if (hostedZoneResponse.hostedZone() == null) {
                hostedZoneResponse = null;
            }

        } catch (HostedZoneNotFoundException ignore) {
            // ignore
        }

        return hostedZoneResponse;
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
        if (getPrivateZone() && getVpc() == null) {
            throw new GyroException("if param 'private-zone' is set to 'true' vpc needs to be provided.");
        }

        if (!getPrivateZone() && getVpc() != null) {
            throw new GyroException("if param 'private-zone' is set to 'false' vpc cannot be set.");
        }
    }
}
