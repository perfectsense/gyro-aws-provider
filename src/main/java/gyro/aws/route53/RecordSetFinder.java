package gyro.aws.route53;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsRequest;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.NoSuchHostedZoneException;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query record set.
 *
 * .. code-block:: gyro
 *
 *    record-set: $(aws::record-set EXTERNAL/* | hosted-zone-id = '' and start-record-name = '' and start-record-type = '')
 */
@Type("record-set")
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

        if (filters.containsKey("hosted-zone-id") && !ObjectUtils.isBlank(filters.get("hosted-zone-id"))) {

            ListResourceRecordSetsRequest.Builder builder = ListResourceRecordSetsRequest.builder();
            builder = builder.hostedZoneId(filters.get("hosted-zone-id"));

            if (filters.containsKey("start-record-name")) {
                builder = builder.startRecordName(filters.get("start-record-name"));
            }

            if (filters.containsKey("start-record-name") && filters.containsKey("start-record-type")) {
                builder = builder.startRecordType(filters.get("start-record-type"));
            }

            if (!filters.containsKey("start-record-name") && filters.containsKey("start-record-type")) {
                throw new IllegalArgumentException("If 'start-record-type' is provided, then 'start-record-name' also needs to be provided.");
            }

            try {
                String startRecordIdentifier = null;
                ListResourceRecordSetsResponse response;
                do {
                    if (startRecordIdentifier == null) {
                        response = client.listResourceRecordSets(builder.build());
                    } else {
                        response = client.listResourceRecordSets(builder.startRecordIdentifier(startRecordIdentifier).build());
                    }
                    startRecordIdentifier = response.nextRecordIdentifier();
                    resourceRecordSets.addAll(response.resourceRecordSets());
                } while (response.isTruncated());

            } catch (NoSuchHostedZoneException ignore) {
                // ignore
            }
        }

        return resourceRecordSets;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }
}
