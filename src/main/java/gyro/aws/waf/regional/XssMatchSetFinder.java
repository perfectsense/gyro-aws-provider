package gyro.aws.waf.regional;

import gyro.core.Type;
import software.amazon.awssdk.services.waf.model.XssMatchSet;
import software.amazon.awssdk.services.waf.model.XssMatchSetSummary;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Type("xss-match-set-regional")
public class XssMatchSetFinder extends gyro.aws.waf.common.XssMatchSetFinder<WafRegionalClient, XssMatchSetResource> {
    @Override
    protected List<XssMatchSet> findAllAws(WafRegionalClient client) {
        List<XssMatchSet> xssMatchSets = new ArrayList<>();

        List<XssMatchSetSummary> xssMatchSetSummaries = client.listXssMatchSets().xssMatchSets();

        for (XssMatchSetSummary xssMatchSetSummary : xssMatchSetSummaries) {
            xssMatchSets.add(client.getXssMatchSet(r -> r.xssMatchSetId(xssMatchSetSummary.xssMatchSetId())).xssMatchSet());
        }

        return xssMatchSets;
    }

    @Override
    protected List<XssMatchSet> findAws(WafRegionalClient client, Map<String, String> filters) {
        List<XssMatchSet> xssMatchSets = new ArrayList<>();

        if (filters.containsKey("xss-match-set-id")) {
            xssMatchSets.add(client.getXssMatchSet(r -> r.xssMatchSetId(filters.get("xss-match-set-id"))).xssMatchSet());
        }

        return xssMatchSets;
    }
}