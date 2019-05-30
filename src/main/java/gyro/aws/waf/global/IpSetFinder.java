package gyro.aws.waf.global;

import gyro.core.Type;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.IPSet;
import software.amazon.awssdk.services.waf.model.IPSetSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Type("ip-set")
public class IpSetFinder extends gyro.aws.waf.common.IpSetFinder<WafClient, IpSetResource> {
    @Override
    protected List<IPSet> findAllAws(WafClient client) {
        List<IPSet> ipSets = new ArrayList<>();

        List<IPSetSummary> ipSetSummaries = client.listIPSets().ipSets();

        for (IPSetSummary ipSetSummary : ipSetSummaries) {
            ipSets.add(client.getIPSet(r -> r.ipSetId(ipSetSummary.ipSetId())).ipSet());
        }

        return ipSets;
    }

    @Override
    protected List<IPSet> findAws(WafClient client, Map<String, String> filters) {
        List<IPSet> ipSets = new ArrayList<>();

        if (filters.containsKey("ip-set-id")) {
            ipSets.add(client.getIPSet(r -> r.ipSetId(filters.get("ip-set-id"))).ipSet());
        }

        return ipSets;
    }
}