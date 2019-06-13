package gyro.aws.waf.global;

import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.RegexMatchSet;
import software.amazon.awssdk.services.waf.model.RegexMatchSetSummary;

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

        List<RegexMatchSetSummary> regexMatchSetSummaries = client.listRegexMatchSets().regexMatchSets();

        for (RegexMatchSetSummary regexMatchSetSummary : regexMatchSetSummaries) {
            regexMatchSets.add(client.getRegexMatchSet(r -> r.regexMatchSetId(regexMatchSetSummary.regexMatchSetId())).regexMatchSet());
        }

        return regexMatchSets;
    }

    @Override
    protected List<RegexMatchSet> findAws(WafClient client, Map<String, String> filters) {
        List<RegexMatchSet> regexMatchSets = new ArrayList<>();

        if (filters.containsKey("regex-match-set-id")) {
            regexMatchSets.add(client.getRegexMatchSet(r -> r.regexMatchSetId(filters.get("regex-match-set-id"))).regexMatchSet());
        }

        return regexMatchSets;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }
}