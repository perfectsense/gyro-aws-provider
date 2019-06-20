package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ListRegexMatchSetsRequest;
import software.amazon.awssdk.services.waf.model.ListRegexMatchSetsResponse;
import software.amazon.awssdk.services.waf.model.RegexMatchSet;
import software.amazon.awssdk.services.waf.model.RegexMatchSetSummary;
import software.amazon.awssdk.services.waf.model.WafNonexistentItemException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query regex match set.
 *
 * .. code-block:: gyro
 *
 *    regex-match-sets: $(aws::regex-match-set EXTERNAL/* | id = '')
 */
@Type("regex-match-set")
public class RegexMatchSetFinder extends gyro.aws.waf.common.RegexMatchSetFinder<WafClient, RegexMatchSetResource> {
    @Override
    protected List<RegexMatchSet> findAllAws(WafClient client) {
        List<RegexMatchSet> regexMatchSets = new ArrayList<>();

        String marker = null;
        ListRegexMatchSetsResponse response;
        List<RegexMatchSetSummary> regexMatchSetSummaries = new ArrayList<>();

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listRegexMatchSets();
            } else {
                response = client.listRegexMatchSets(ListRegexMatchSetsRequest.builder().nextMarker(marker).build());
            }

            marker = response.nextMarker();
            regexMatchSetSummaries.addAll(response.regexMatchSets());

        } while (!ObjectUtils.isBlank(marker));

        for (RegexMatchSetSummary regexMatchSetSummary : regexMatchSetSummaries) {
            regexMatchSets.add(client.getRegexMatchSet(r -> r.regexMatchSetId(regexMatchSetSummary.regexMatchSetId())).regexMatchSet());
        }

        return regexMatchSets;
    }

    @Override
    protected List<RegexMatchSet> findAws(WafClient client, Map<String, String> filters) {
        List<RegexMatchSet> regexMatchSets = new ArrayList<>();

        if (filters.containsKey("regex-match-set-id") && !ObjectUtils.isBlank(filters.get("regex-match-set-id"))) {
            try {
                regexMatchSets.add(client.getRegexMatchSet(r -> r.regexMatchSetId(filters.get("regex-match-set-id"))).regexMatchSet());
            } catch (WafNonexistentItemException ignore) {
                //ignore
            }
        }

        return regexMatchSets;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }
}