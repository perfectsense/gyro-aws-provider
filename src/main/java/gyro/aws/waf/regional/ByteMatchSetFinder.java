package gyro.aws.waf.regional;

import gyro.core.Type;
import software.amazon.awssdk.services.waf.model.ByteMatchSet;
import software.amazon.awssdk.services.waf.model.ByteMatchSetSummary;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Type("byte-match-set-regional")
public class ByteMatchSetFinder extends gyro.aws.waf.common.ByteMatchSetFinder<WafRegionalClient, ByteMatchSetResource> {
    @Override
    protected List<ByteMatchSet> findAllAws(WafRegionalClient client) {
        List<ByteMatchSet> byteMatchSets = new ArrayList<>();

        List<ByteMatchSetSummary> byteMatchSetSummaries = client.listByteMatchSets().byteMatchSets();

        for (ByteMatchSetSummary byteMatchSetSummary : byteMatchSetSummaries) {
            byteMatchSets.add(client.getByteMatchSet(r -> r.byteMatchSetId(byteMatchSetSummary.byteMatchSetId())).byteMatchSet());
        }

        return byteMatchSets;
    }

    @Override
    protected List<ByteMatchSet> findAws(WafRegionalClient client, Map<String, String> filters) {
        List<ByteMatchSet> byteMatchSets = new ArrayList<>();

        if (filters.containsKey("byte-match-set-id")) {
            byteMatchSets.add(client.getByteMatchSet(r -> r.byteMatchSetId(filters.get("byte-match-set-id"))).byteMatchSet());
        }

        return byteMatchSets;
    }
}