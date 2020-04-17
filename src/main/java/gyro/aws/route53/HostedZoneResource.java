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

package gyro.aws.route53;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.ec2.VpcResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.Type;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import gyro.core.scope.State;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.CreateHostedZoneResponse;
import software.amazon.awssdk.services.route53.model.GetHostedZoneResponse;
import software.amazon.awssdk.services.route53.model.HostedZone;
import software.amazon.awssdk.services.route53.model.HostedZoneNotFoundException;
import software.amazon.awssdk.services.route53.model.NoSuchHostedZoneException;
import software.amazon.awssdk.services.route53.model.PublicZoneVpcAssociationException;
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
 *     aws::route53-hosted-zone hosted-zone-example
 *         name: "hosted-zone-example.com"
 *         vpcs: [
 *             $(aws::vpc vpc-hosted-zone-example)
 *         ]
 *         comment: "hosted-zone-example.comment modified"
 *     end
 *
 */
@Type("route53-hosted-zone")
public class HostedZoneResource extends AwsResource implements Copyable<HostedZone> {

    private String delegationSetId;
    private String comment;
    private String id;
    private String name;
    private Long resourceRecordSetCount;
    private String description;
    private String servicePrincipal;
    private Set<VpcResource> vpcs;

    private Boolean privateZone;

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
     * The name of the Hosted Zone.
     */
    public String getName() {
        if (name != null) {
            name += name.endsWith(".") ? "" : ".";
        }

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * A set of Vpcs to be associated with the Hosted Zone. If no vpc is associated, the hosted zone becomes a public zone and vpc's cannot be associated later. If one or vpc is provided then hosted zone becomes private zone, and vpc's cannot be emptied.
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
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    /**
     * Is the Hosted Zone private.
     */
    @Output
    public Boolean getPrivateZone() {
        if (privateZone == null) {
            privateZone = !getVpcs().isEmpty();
        }

        return privateZone;
    }

    public void setPrivateZone(Boolean privateZone) {
        this.privateZone = privateZone;
    }

    @Override
    public void copyFrom(HostedZone hostedZone) {
        setId(hostedZone.id());
        setComment(hostedZone.config().comment());
        setName(hostedZone.name());
        setPrivateZone(hostedZone.config().privateZone());
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
    public void create(GyroUI ui, State state) {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        VpcResource firstVpcResource = !getVpcs().isEmpty() ? getVpcs().iterator().next() : null;

        CreateHostedZoneResponse response = client.createHostedZone(
            r -> r.name(getName())
                .delegationSetId(getDelegationSetId())
                .hostedZoneConfig(
                    o -> o.comment(getComment())
                        .privateZone(getPrivateZone())
                )
                .vpc(getVpc(firstVpcResource))
            .callerReference(UUID.randomUUID().toString())
        );

        HostedZone hostedZone = response.hostedZone();

        setId(hostedZone.id());
        setResourceRecordSetCount(hostedZone.resourceRecordSetCount());
        setDescription(hostedZone.linkedService() != null ? hostedZone.linkedService().description() : null);
        setServicePrincipal(hostedZone.linkedService() != null ? hostedZone.linkedService().servicePrincipal() : null);

        if (getVpcs().size() > 1) {
            for (VpcResource vpcResource : getVpcs()) {
                if (!vpcResource.equals(firstVpcResource)) {
                    client.associateVPCWithHostedZone(
                        r -> r.hostedZoneId(getId())
                            .vpc(getVpc(vpcResource)));
                }
            }
        }
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        if (changedFieldNames.contains("comment")) {
            client.updateHostedZoneComment(
                r -> r.id(getId())
                    .comment(getComment() != null ? getComment() : "")
            );
        }

        if (changedFieldNames.contains("vpcs")) {
            HostedZoneResource currentHostedZone = (HostedZoneResource) current;

            if (getPrivateZone() && getVpcs().isEmpty()) {
                throw new GyroException(String.format("Hosted zone %s is a private zone and must have at least one VPC.", getName()));
            }

            Set<String> pendingVpcIds = getVpcs().stream().map(VpcResource::getId).collect(Collectors.toSet());
            List<VPC> deleteVpcs = currentHostedZone.getVpcs().stream().filter(o -> !pendingVpcIds.contains(o.getId())).map(this::getVpc).collect(Collectors.toList());
            for (VPC vpc : deleteVpcs) {
                client.disassociateVPCFromHostedZone(r -> r.hostedZoneId(getId()).vpc(vpc));
            }

            try {
                Set<String> currentVpcIds = currentHostedZone.getVpcs().stream().map(VpcResource::getId).collect(Collectors.toSet());
                List<VPC> addVpcs = getVpcs().stream().filter(o -> !currentVpcIds.contains(o.getId())).map(this::getVpc).collect(Collectors.toList());
                for (VPC vpc : addVpcs) {
                    client.associateVPCWithHostedZone(r -> r.hostedZoneId(getId()).vpc(vpc));
                }
            } catch (PublicZoneVpcAssociationException ex) {
                throw new GyroException(String.format("Hosted zone %s is a public zone and cannot be associated with a VPC.", getName()));
            }
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        Route53Client client = createClient(Route53Client.class, Region.AWS_GLOBAL.toString(), null);

        client.deleteHostedZone(
            r -> r.id(getId())
        );
    }

    private GetHostedZoneResponse getHostedZoneResponse(Route53Client client) {
        GetHostedZoneResponse hostedZoneResponse = null;

        if (ObjectUtils.isBlank(getId())) {
            throw new GyroException("hosted-zone-id is missing, unable to load hosted zone.");
        }

        try {
            hostedZoneResponse = client.getHostedZone(
                r -> r.id(getId())
            );

            if (hostedZoneResponse.hostedZone() == null) {
                hostedZoneResponse = null;
            }

        } catch (HostedZoneNotFoundException | NoSuchHostedZoneException ignore) {
            // ignore
        }

        return hostedZoneResponse;
    }

    private VPC getVpc(VpcResource vpcResource) {
        return vpcResource == null
            ? null
            : VPC.builder()
            .vpcId(vpcResource.getId())
            .vpcRegion(vpcResource.getRegion())
            .build();
    }
}
