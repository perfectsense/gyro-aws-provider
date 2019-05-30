package gyro.aws.waf.regional;

import gyro.core.Type;
import software.amazon.awssdk.services.waf.model.RegexMatchSet;
import software.amazon.awssdk.services.waf.model.RegexMatchSetSummary;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Type("regex-match-set-regional")
public class RegexMatchSetFinder extends gyro.aws.waf.common.RegexMatchSetFinder<WafRegionalClient, RegexMatchSetResource> {
    @Override
    protected List<RegexMatchSet> findAllAws(WafRegionalClient client) {
        List<RegexMatchSet> regexMatchSets = new ArrayList<>();

        List<RegexMatchSetSummary> regexMatchSetSummaries = client.listRegexMatchSets().regexMatchSets();

        for (RegexMatchSetSummary regexMatchSetSummary : regexMatchSetSummaries) {
            regexMatchSets.add(client.getRegexMatchSet(r -> r.regexMatchSetId(regexMatchSetSummary.regexMatchSetId())).regexMatchSet());
        }

        return regexMatchSets;
    }

    @Override
    protected List<RegexMatchSet> findAws(WafRegionalClient client, Map<String, String> filters) {
        List<RegexMatchSet> regexMatchSets = new ArrayList<>();

        if (filters.containsKey("regex-match-set-id")) {
            regexMatchSets.add(client.getRegexMatchSet(r -> r.regexMatchSetId(filters.get("regex-match-set-id"))).regexMatchSet());
        }

        return regexMatchSets;
    }
}