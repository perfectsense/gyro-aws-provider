package gyro.aws.route53;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.ec2.VpcResource;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private Set<VpcResource> vpcs;

    /**
     * The ID of a delegation set.
     */
    public String getDelegationSetId() {
        return delegationSetId;
    }

    public void setDelegationSetId(String delegationSetId) {
        this.delegationSetId = delegationSetId;
    }

    /**
     * Comment for the Hosted Zone.
     */
    @Updatable
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Is the the Hosted Zone private.
     */
    public Boolean getPrivateZone() {
        return privateZone;
    }

    public void setPrivateZone(Boolean privateZone) {
        this.privateZone = privateZone;
    }

    /**
     * The name of the Hosted Zone.
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
     * A set of Vpcs to be associated with the Hosted Zone.
     *
     * @subresource gyro.aws.route53.Route53VpcResource
     */
    @Updatable
    public Set<VpcResource> getVpcs() {
        if (vpcs == null) {
            vpcs = new HashSet<>();
        }

        return vpcs;
    }

    public void setVpcs(Set<VpcResource> vpcs) {
        this.vpcs = vpcs;
    }

    /**
     * The ID of the Hosted Zone.
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
     * The record count of the Hosted Zone.
     */
    @Output
    public Long getResourceRecordSetCount() {
        return resourceRecordSetCount;
    }

    public void setResourceRecordSetCount(Long resourceRecordSetCount) {
        this.resourceRecordSetCount = resourceRecordSetCount;
    }

    /**
     * The description of a linked service to the Hosted Zone.
     */
    @Output
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The service principal of the Hosted Zone.
     */
    @Output
    public String getServicePrincipal() {
        return servicePrincipal;
    }

    public void setServicePrincipal(String servicePrincipal) {
        this.servicePrincipal = servicePrincipal;
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
            setVpcs(response.vpCs().stream().map(o -> findById(VpcResource.class, o.vpcId())).collect(Collectors.toSet()));
        }
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

        validate();
        VpcResource firstVpcResource = !getVpcs().isEmpty() ? getVpcs().iterator().next() : null;

        CreateHostedZoneResponse response = client.createHostedZone(
            r -> r.name(getHostedZoneName())
                .delegationSetId(getDelegationSetId())
                .hostedZoneConfig(
                    o -> o.comment(getComment())
                        .privateZone(getPrivateZone())
                )
                .vpc(getVpc(firstVpcResource))
            .callerReference(UUID.randomUUID().toString())
        );

        HostedZone hostedZone = response.hostedZone();

        setHostedZoneId(hostedZone.id());
        setResourceRecordSetCount(hostedZone.resourceRecordSetCount());
        setDescription(hostedZone.linkedService() != null ? hostedZone.linkedService().description() : null);
        setServicePrincipal(hostedZone.linkedService() != null ? hostedZone.linkedService().servicePrincipal() : null);

        if (getVpcs().size() > 1) {
            for (VpcResource vpcResource : getVpcs()) {
                if (!vpcResource.equals(firstVpcResource)) {
                    client.associateVPCWithHostedZone(
                        r -> r.hostedZoneId(getHostedZoneId())
                            .vpc(getVpc(vpcResource)));
                }
            }
        }
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
        validate();

        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        if (changedFieldNames.contains("comment")) {
            client.updateHostedZoneComment(
                r -> r.id(getHostedZoneId())
                    .comment(getComment() != null ? getComment() : "")
            );
        }

        if (changedFieldNames.contains("vpcs")) {
            HostedZoneResource currentHostedZone = (HostedZoneResource) current;

            Set<String> pendingVpcIds = getVpcs().stream().map(VpcResource::getVpcId).collect(Collectors.toSet());
            List<VPC> deleteVpcs = currentHostedZone.getVpcs().stream().filter(o -> !pendingVpcIds.contains(o.getVpcId())).map(this::getVpc).collect(Collectors.toList());
            for (VPC vpc : deleteVpcs) {
                client.disassociateVPCFromHostedZone(r -> r.hostedZoneId(getHostedZoneId()).vpc(vpc));
            }

            Set<String> currentVpcIds = currentHostedZone.getVpcs().stream().map(VpcResource::getVpcId).collect(Collectors.toSet());
            List<VPC> addVpcs = getVpcs().stream().filter(o -> !currentVpcIds.contains(o.getVpcId())).map(this::getVpc).collect(Collectors.toList());
            for (VPC vpc : addVpcs) {
                client.associateVPCWithHostedZone(r -> r.hostedZoneId(getHostedZoneId()).vpc(vpc));
            }
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

    private void validate() {
        if (getPrivateZone() && getVpcs().isEmpty()) {
            throw new GyroException("if param 'private-zone' is set to 'true' 'vpcs' needs to be provided.");
        }

        if (!getPrivateZone() && !getVpcs().isEmpty()) {
            throw new GyroException("if param 'private-zone' is set to 'false' 'vpcs' cannot be set.");
        }
    }

    private VPC getVpc(VpcResource vpcResource) {
        return vpcResource == null
            ? null
            : VPC.builder()
            .vpcId(vpcResource.getVpcId())
            .vpcRegion(vpcResource.getRegion())
            .build();
    }
}
