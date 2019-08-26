package gyro.aws.route53;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.HostedZone;
import software.amazon.awssdk.services.route53.model.HostedZoneNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query hosted zone.
 *
 * .. code-block:: gyro
 *
 *    hosted-zone: $(external-query aws::route53-hosted-zone { hosted-zone-id: ''})
 */
@Type("route53-hosted-zone")
public class HostedZoneFinder extends AwsFinder<Route53Client, HostedZone, HostedZoneResource> {
    private String hostedZoneId;

    /**
     * The ID of the hosted zone.
     */
    public String getHostedZoneId() {
        return hostedZoneId;
    }

    public void setHostedZoneId(String hostedZoneId) {
        this.hostedZoneId = hostedZoneId;
    }

    @Override
    protected List<HostedZone> findAllAws(Route53Client client) {
        return client.listHostedZonesPaginator().hostedZones().stream().collect(Collectors.toList());
    }

    @Override
    protected List<HostedZone> findAws(Route53Client client, Map<String, String> filters) {
        List<HostedZone> hostedZones = new ArrayList<>();

        try {
            hostedZones.add(client.getHostedZone(r -> r.id(filters.get("hosted-zone-id"))).hostedZone());
        } catch (HostedZoneNotFoundException ignore) {
            // ignore
        }

        return hostedZones;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }
}
