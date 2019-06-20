package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import software.amazon.awssdk.services.waf.model.IPSet;
import software.amazon.awssdk.services.waf.model.IPSetSummary;
import software.amazon.awssdk.services.waf.model.ListIpSetsRequest;
import software.amazon.awssdk.services.waf.model.ListIpSetsResponse;
import software.amazon.awssdk.services.waf.model.WafNonexistentItemException;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query ip set regional.
 *
 * .. code-block:: gyro
 *
 *    ip-sets: $(aws::ip-set-regional EXTERNAL/* | id = '')
 */
@Type("ip-set-regional")
public class IpSetFinder extends gyro.aws.waf.common.IpSetFinder<WafRegionalClient, IpSetResource> {
    @Override
    protected List<IPSet> findAllAws(WafRegionalClient client) {
        List<IPSet> ipSets = new ArrayList<>();

        String marker = null;
        ListIpSetsResponse response;
        List<IPSetSummary> ipSetSummaries = new ArrayList<>();

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listIPSets();
            } else {
                response = client.listIPSets(ListIpSetsRequest.builder().nextMarker(marker).build());
            }

            marker = response.nextMarker();
            ipSetSummaries.addAll(response.ipSets());

        } while (!ObjectUtils.isBlank(marker));

        for (IPSetSummary ipSetSummary : ipSetSummaries) {
            ipSets.add(client.getIPSet(r -> r.ipSetId(ipSetSummary.ipSetId())).ipSet());
        }

        return ipSets;
    }

    @Override
    protected List<IPSet> findAws(WafRegionalClient client, Map<String, String> filters) {
        List<IPSet> ipSets = new ArrayList<>();

        if (filters.containsKey("ip-set-id") && !ObjectUtils.isBlank(filters.get("ip-set-id"))) {
            try {
                ipSets.add(client.getIPSet(r -> r.ipSetId(filters.get("ip-set-id"))).ipSet());
            } catch (WafNonexistentItemException ignore) {
                //ignore
            }
        }

        return ipSets;
    }
}