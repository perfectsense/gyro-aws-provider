package gyro.aws.ec2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.ec2.model.ApplianceModeSupportValue;
import software.amazon.awssdk.services.ec2.model.CreateTransitGatewayVpcAttachmentRequestOptions;
import software.amazon.awssdk.services.ec2.model.DnsSupportValue;
import software.amazon.awssdk.services.ec2.model.Ipv6SupportValue;
import software.amazon.awssdk.services.ec2.model.ModifyTransitGatewayVpcAttachmentRequestOptions;

public class TransitGatewayVpcAttachmentOptions extends Diffable
    implements Copyable<software.amazon.awssdk.services.ec2.model.TransitGatewayVpcAttachmentOptions> {

    private ApplianceModeSupportValue applianceModeSupport;
    private DnsSupportValue dnsSupport;
    private Ipv6SupportValue ipv6Support;

    /**
     * Enable appliance mode support. Defaults to ``enable``.
     */
    @Updatable
    @ValidStrings({ "enable", "disable" })
    public ApplianceModeSupportValue getApplianceModeSupport() {
        return applianceModeSupport;
    }

    public void setApplianceModeSupport(ApplianceModeSupportValue applianceModeSupport) {
        this.applianceModeSupport = applianceModeSupport;
    }

    /**
     * Enable DNS resolution for the attachment. Defaults to ``enable``.
     */
    @Updatable
    @ValidStrings({ "enable", "disable" })
    public DnsSupportValue getDnsSupport() {
        return dnsSupport;
    }

    public void setDnsSupport(DnsSupportValue dnsSupport) {
        this.dnsSupport = dnsSupport;
    }

    /**
     * Enable support for ipv6 block support for the attachment. Defaults to ``disable``.
     */
    @Updatable
    @ValidStrings({ "enable", "disable" })
    public Ipv6SupportValue getIpv6Support() {
        return ipv6Support;
    }

    public void setIpv6Support(Ipv6SupportValue ipv6Support) {
        this.ipv6Support = ipv6Support;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.ec2.model.TransitGatewayVpcAttachmentOptions model) {
        setApplianceModeSupport(model.applianceModeSupport());
        setDnsSupport(model.dnsSupport());
        setIpv6Support(model.ipv6Support());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    CreateTransitGatewayVpcAttachmentRequestOptions toCreateTransitGatewayVpcAttachmentOptions() {
        return CreateTransitGatewayVpcAttachmentRequestOptions.builder()
            .applianceModeSupport(getApplianceModeSupport())
            .dnsSupport(getDnsSupport())
            .ipv6Support(getIpv6Support())
            .build();
    }

    ModifyTransitGatewayVpcAttachmentRequestOptions toModifyTransitGatewayVpcAttachmentRequestOptions() {
        return ModifyTransitGatewayVpcAttachmentRequestOptions.builder()
            .ipv6Support(getIpv6Support())
            .dnsSupport(getDnsSupport())
            .applianceModeSupport(getApplianceModeSupport())
            .build();
    }
}
