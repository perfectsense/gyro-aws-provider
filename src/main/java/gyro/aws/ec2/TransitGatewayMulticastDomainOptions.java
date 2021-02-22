package gyro.aws.ec2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.ec2.model.AutoAcceptSharedAssociationsValue;
import software.amazon.awssdk.services.ec2.model.CreateTransitGatewayMulticastDomainRequestOptions;
import software.amazon.awssdk.services.ec2.model.Igmpv2SupportValue;
import software.amazon.awssdk.services.ec2.model.StaticSourcesSupportValue;

public class TransitGatewayMulticastDomainOptions extends Diffable
    implements Copyable<software.amazon.awssdk.services.ec2.model.TransitGatewayMulticastDomainOptions> {

    public AutoAcceptSharedAssociationsValue autoAcceptSharedAssociations;
    public Igmpv2SupportValue igmpv2Support;
    public StaticSourcesSupportValue staticSourcesSupport;

    /**
     * Indicates whether to automatically accept cross-account subnet associations that are associated with the transit gateway multicast domain.
     */
    public AutoAcceptSharedAssociationsValue getAutoAcceptSharedAssociations() {
        return autoAcceptSharedAssociations;
    }

    public void setAutoAcceptSharedAssociations(AutoAcceptSharedAssociationsValue autoAcceptSharedAssociations) {
        this.autoAcceptSharedAssociations = autoAcceptSharedAssociations;
    }

    /**
     * Indicates whether to enable Internet Group Management Protocol (IGMP) version 2 for the transit gateway multicast domain.
     */
    public Igmpv2SupportValue getIgmpv2Support() {
        return igmpv2Support;
    }

    public void setIgmpv2Support(Igmpv2SupportValue igmpv2Support) {
        this.igmpv2Support = igmpv2Support;
    }

    /**
     * Indicates whether to enable support for statically configuring multicast group sources for a domain.
     */
    public StaticSourcesSupportValue getStaticSourcesSupport() {
        return staticSourcesSupport;
    }

    public void setStaticSourcesSupport(StaticSourcesSupportValue staticSourcesSupport) {
        this.staticSourcesSupport = staticSourcesSupport;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.ec2.model.TransitGatewayMulticastDomainOptions model) {
        setAutoAcceptSharedAssociations(model.autoAcceptSharedAssociations());
        setIgmpv2Support(model.igmpv2Support());
        setStaticSourcesSupport(model.staticSourcesSupport());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    CreateTransitGatewayMulticastDomainRequestOptions toCreateTransitGatewayMulticastDomainRequestOptions() {
        return CreateTransitGatewayMulticastDomainRequestOptions.builder()
            .autoAcceptSharedAssociations(getAutoAcceptSharedAssociations())
            .igmpv2Support(getIgmpv2Support())
            .staticSourcesSupport(getStaticSourcesSupport())
            .build();
    }
}
