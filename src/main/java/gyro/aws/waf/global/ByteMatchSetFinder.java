package gyro.aws.waf.global;

import gyro.core.Type;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ByteMatchSet;
import software.amazon.awssdk.services.waf.model.ByteMatchSetSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query byte match set.
 *
 * .. code-block:: gyro
 *
 *    byte-match-sets: $(aws::byte-match-set EXTERNAL/* | id = '')
 */
@Type("byte-match-set")
public class ByteMatchSetFinder extends gyro.aws.waf.common.ByteMatchSetFinder<WafClient, ByteMatchSetResource> {
    @Override
    protected List<ByteMatchSet> findAllAws(WafClient client) {
        List<ByteMatchSet> byteMatchSets = new ArrayList<>();

        List<ByteMatchSetSummary> byteMatchSetSummaries = client.listByteMatchSets().byteMatchSets();

        for (ByteMatchSetSummary byteMatchSetSummary : byteMatchSetSummaries) {
            byteMatchSets.add(client.getByteMatchSet(r -> r.byteMatchSetId(byteMatchSetSummary.byteMatchSetId())).byteMatchSet());
        }

        return byteMatchSets;
    }

    @Override
    protected List<ByteMatchSet> findAws(WafClient client, Map<String, String> filters) {
        List<ByteMatchSet> byteMatchSets = new ArrayList<>();

        if (filters.containsKey("byte-match-set-id")) {
            byteMatchSets.add(client.getByteMatchSet(r -> r.byteMatchSetId(filters.get("byte-match-set-id"))).byteMatchSet());
        }

        return byteMatchSets;
    }
}