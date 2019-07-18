package gyro.aws.ec2;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.aws.route53.HostedZoneResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;

public class DnsEntry extends Diffable implements Copyable<software.amazon.awssdk.services.ec2.model.DnsEntry> {
    private String name;
    private HostedZoneResource hostedZone;

    /**
     * Name of the dns.
     */
    @Output
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Associated hosted zone.
     */
    @Output
    public HostedZoneResource getHostedZone() {
        return hostedZone;
    }

    public void setHostedZone(HostedZoneResource hostedZone) {
        this.hostedZone = hostedZone;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.ec2.model.DnsEntry dnsEntry) {
        setName(dnsEntry.dnsName());
        setHostedZone(!ObjectUtils.isBlank(dnsEntry.hostedZoneId()) ? findById(HostedZoneResource.class, dnsEntry.hostedZoneId()) : null);
    }

    @Override
    public String toDisplayString() {
        return "dns entry " + getName();
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s",getName(), getHostedZone() != null ? getHostedZone().getId() : "");
    }
}
