package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import software.amazon.awssdk.services.waf.model.ListRegexPatternSetsRequest;
import software.amazon.awssdk.services.waf.model.ListRegexPatternSetsResponse;
import software.amazon.awssdk.services.waf.model.RegexPatternSet;
import software.amazon.awssdk.services.waf.model.RegexPatternSetSummary;
import software.amazon.awssdk.services.waf.model.WafNonexistentItemException;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query regex pattern set regional.
 *
 * .. code-block:: gyro
 *
 *    regex-pattern-sets: $(aws::waf-regex-pattern-set-regional EXTERNAL/* | regex-pattern-set-id = '')
 */
@Type("waf-regex-pattern-set-regional")
public class RegexPatternSetFinder extends gyro.aws.waf.common.RegexPatternSetFinder<WafRegionalClient, RegexPatternSetResource> {
    @Override
    protected List<RegexPatternSet> findAllAws(WafRegionalClient client) {
        List<RegexPatternSet> regexPatternSets = new ArrayList<>();

        String marker = null;
        ListRegexPatternSetsResponse response;
        List<RegexPatternSetSummary> regexPatternSetSummaries = new ArrayList<>();

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listRegexPatternSets();
            } else {
                response = client.listRegexPatternSets(ListRegexPatternSetsRequest.builder().nextMarker(marker).build());
            }

            marker = response.nextMarker();
            regexPatternSetSummaries.addAll(response.regexPatternSets());

        } while (!ObjectUtils.isBlank(marker));

        for (RegexPatternSetSummary regexPatternSetSummary : regexPatternSetSummaries) {
            regexPatternSets.add(client.getRegexPatternSet(r -> r.regexPatternSetId(regexPatternSetSummary.regexPatternSetId())).regexPatternSet());
        }

        return regexPatternSets;
    }

    @Override
    protected List<RegexPatternSet> findAws(WafRegionalClient client, Map<String, String> filters) {
        List<RegexPatternSet> regexPatternSets = new ArrayList<>();

        try {
            regexPatternSets.add(client.getRegexPatternSet(r -> r.regexPatternSetId(filters.get("regex-pattern-set-id"))).regexPatternSet());
        } catch (WafNonexistentItemException ignore) {
            //ignore
        }

        return regexPatternSets;
    }
}