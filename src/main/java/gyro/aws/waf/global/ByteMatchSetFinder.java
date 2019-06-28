package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ByteMatchSet;
import software.amazon.awssdk.services.waf.model.ByteMatchSetSummary;
import software.amazon.awssdk.services.waf.model.ListByteMatchSetsRequest;
import software.amazon.awssdk.services.waf.model.ListByteMatchSetsResponse;
import software.amazon.awssdk.services.waf.model.WafNonexistentItemException;

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

        String marker = null;
        ListByteMatchSetsResponse response;
        List<ByteMatchSetSummary> byteMatchSetSummaries = new ArrayList<>();

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listByteMatchSets();
            } else {
                response = client.listByteMatchSets(ListByteMatchSetsRequest.builder().nextMarker(marker).build());
            }

            marker = response.nextMarker();
            byteMatchSetSummaries.addAll(response.byteMatchSets());

        } while (!ObjectUtils.isBlank(marker));

        for (ByteMatchSetSummary byteMatchSetSummary : byteMatchSetSummaries) {
            byteMatchSets.add(client.getByteMatchSet(r -> r.byteMatchSetId(byteMatchSetSummary.byteMatchSetId())).byteMatchSet());
        }

        return byteMatchSets;
    }

    @Override
    protected List<ByteMatchSet> findAws(WafClient client, Map<String, String> filters) {
        List<ByteMatchSet> byteMatchSets = new ArrayList<>();

        try {
            byteMatchSets.add(client.getByteMatchSet(r -> r.byteMatchSetId(filters.get("byte-match-set-id"))).byteMatchSet());
        } catch (WafNonexistentItemException ignore) {
            //ignore
        }

        return byteMatchSets;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }
}