package gyro.aws.route53;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.HostedZone;
import software.amazon.awssdk.services.route53.model.HostedZoneNotFoundException;
import software.amazon.awssdk.services.route53.model.ListHostedZonesRequest;
import software.amazon.awssdk.services.route53.model.ListHostedZonesResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query hosted zone.
 *
 * .. code-block:: gyro
 *
 *    hosted-zone: $(aws::hosted-zone EXTERNAL/* | hosted-zone-id = '')
 */
@Type("hosted-zone")
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
        List<HostedZone> hostedZones = new ArrayList<>();

        String marker = null;
        ListHostedZonesResponse response;

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listHostedZones();
            } else {
                response  = client.listHostedZones(ListHostedZonesRequest.builder().marker(marker).build());
            }

            marker = response.marker();
            hostedZones.addAll(response.hostedZones());

        } while (response.isTruncated());

        return hostedZones;
    }

    @Override
    protected List<HostedZone> findAws(Route53Client client, Map<String, String> filters) {
        List<HostedZone> hostedZones = new ArrayList<>();

        if (filters.containsKey("hosted-zone-id") && !ObjectUtils.isBlank(filters.get("hosted-zone-id"))) {
            try {
                hostedZones.add(client.getHostedZone(r -> r.id(filters.get("hosted-zone-id"))).hostedZone());
            } catch (HostedZoneNotFoundException ignore) {
                // ignore
            }
        }

        return hostedZones;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }
}
