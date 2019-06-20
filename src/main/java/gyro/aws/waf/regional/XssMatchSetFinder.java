package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import software.amazon.awssdk.services.waf.model.ListXssMatchSetsRequest;
import software.amazon.awssdk.services.waf.model.ListXssMatchSetsResponse;
import software.amazon.awssdk.services.waf.model.WafNonexistentItemException;
import software.amazon.awssdk.services.waf.model.XssMatchSet;
import software.amazon.awssdk.services.waf.model.XssMatchSetSummary;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query xss match set regional.
 *
 * .. code-block:: gyro
 *
 *    xss-match-sets: $(aws::xss-match-set-regional EXTERNAL/* | id = '')
 */
@Type("xss-match-set-regional")
public class XssMatchSetFinder extends gyro.aws.waf.common.XssMatchSetFinder<WafRegionalClient, XssMatchSetResource> {
    @Override
    protected List<XssMatchSet> findAllAws(WafRegionalClient client) {
        List<XssMatchSet> xssMatchSets = new ArrayList<>();

        String marker = null;
        ListXssMatchSetsResponse response;
        List<XssMatchSetSummary> xssMatchSetSummaries = new ArrayList<>();

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listXssMatchSets();
            } else {
                response = client.listXssMatchSets(ListXssMatchSetsRequest.builder().nextMarker(marker).build());
            }

            marker = response.nextMarker();
            xssMatchSetSummaries.addAll(response.xssMatchSets());

        } while (!ObjectUtils.isBlank(marker));

        for (XssMatchSetSummary xssMatchSetSummary : xssMatchSetSummaries) {
            xssMatchSets.add(client.getXssMatchSet(r -> r.xssMatchSetId(xssMatchSetSummary.xssMatchSetId())).xssMatchSet());
        }

        return xssMatchSets;
    }

    @Override
    protected List<XssMatchSet> findAws(WafRegionalClient client, Map<String, String> filters) {
        List<XssMatchSet> xssMatchSets = new ArrayList<>();

        if (filters.containsKey("xss-match-set-id") && !ObjectUtils.isBlank(filters.get("xss-match-set-id"))) {
            try {
                xssMatchSets.add(client.getXssMatchSet(r -> r.xssMatchSetId(filters.get("xss-match-set-id"))).xssMatchSet());
            } catch (WafNonexistentItemException ignore) {
                //ignore
            }
        }

        return xssMatchSets;
    }
}