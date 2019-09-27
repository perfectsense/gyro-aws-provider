package gyro.aws.route53;

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.NoSuchHostedZoneException;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query record set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    record-set: $(external-query aws::route53-record-set { hosted-zone-id: '', start-record-name: '', start-record-type: ''})
 */
@Type("route53-record-set")
public class RecordSetFinder extends AwsFinder<Route53Client, ResourceRecordSet, RecordSetResource> {
    private String hostedZoneId;
    private String startRecordName;
    private String startRecordType;

    /**
     * The ID of the hosted zone that contains the resource record sets that you want to list. (Required)
     */
    public String getHostedZoneId() {
        return hostedZoneId;
    }

    public void setHostedZoneId(String hostedZoneId) {
        this.hostedZoneId = hostedZoneId;
    }

    /**
     * The first name in the lexicographic ordering of resource record sets that you want to list. (Required)
     */
    public String getStartRecordName() {
        return startRecordName;
    }

    public void setStartRecordName(String startRecordName) {
        this.startRecordName = startRecordName;
    }

    /**
     * The type of resource record set to begin the record listing from. Valid values for basic resource record sets are ``A`` or ``AAAA`` or ``CAA`` or ``CNAME`` or ``MX`` or ``NAPTR`` or ``NS`` or ``PTR`` or ``SOA`` or ``SPF`` or ``SRV`` or ``TXT``. Values for weighted, latency, geolocation, and failover resource record sets are ``A`` or ``AAAA`` or ``CAA`` or ``CNAME`` or ``MX`` or ``NAPTR`` or ``PTR`` or ``SPF`` or ``SRV`` or ``TXT``. (Required)
     */
    public String getStartRecordType() {
        return startRecordType;
    }

    public void setStartRecordType(String startRecordType) {
        this.startRecordType = startRecordType;
    }

    @Override
    protected List<ResourceRecordSet> findAllAws(Route53Client client) {
        throw new IllegalArgumentException("Cannot query Recordsets without 'hosted-zone-id'.");
    }

    @Override
    protected List<ResourceRecordSet> findAws(Route53Client client, Map<String, String> filters) {
        List<ResourceRecordSet> resourceRecordSets = new ArrayList<>();

        if (!filters.containsKey("hosted-zone-id")) {
            throw new IllegalArgumentException("Cannot query Recordsets without 'hosted-zone-id'.");
        }

        if (!filters.containsKey("start-record-name") && filters.containsKey("start-record-type")) {
            throw new IllegalArgumentException("If 'start-record-type' is provided, then 'start-record-name' also needs to be provided.");
        }

        try {
            resourceRecordSets.addAll(client.listResourceRecordSetsPaginator(
                r -> r.hostedZoneId(filters.get("hosted-zone-id"))
                    .startRecordType(filters.get("start-record-type"))
                    .startRecordName(filters.get("start-record-name"))
            ).resourceRecordSets().stream().collect(Collectors.toList()));
        } catch (NoSuchHostedZoneException ignore) {
            //ignore
        }

        return resourceRecordSets;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }
}
