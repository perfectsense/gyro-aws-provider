package gyro.aws.waf.global;

import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.XssMatchSet;
import software.amazon.awssdk.services.waf.model.XssMatchSetSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query xss match set.
 *
 * .. code-block:: gyro
 *
 *    xss-match-sets: $(aws::xss-match-set EXTERNAL/* | id = '')
 */
@Type("xss-match-set")
public class XssMatchSetFinder extends gyro.aws.waf.common.XssMatchSetFinder<WafClient, XssMatchSetResource> {
    @Override
    protected List<XssMatchSet> findAllAws(WafClient client) {
        List<XssMatchSet> xssMatchSets = new ArrayList<>();

        List<XssMatchSetSummary> xssMatchSetSummaries = client.listXssMatchSets().xssMatchSets();

        for (XssMatchSetSummary xssMatchSetSummary : xssMatchSetSummaries) {
            xssMatchSets.add(client.getXssMatchSet(r -> r.xssMatchSetId(xssMatchSetSummary.xssMatchSetId())).xssMatchSet());
        }

        return xssMatchSets;
    }

    @Override
    protected List<XssMatchSet> findAws(WafClient client, Map<String, String> filters) {
        List<XssMatchSet> xssMatchSets = new ArrayList<>();

        if (filters.containsKey("xss-match-set-id")) {
            xssMatchSets.add(client.getXssMatchSet(r -> r.xssMatchSetId(filters.get("xss-match-set-id"))).xssMatchSet());
        }

        return xssMatchSets;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }
}
