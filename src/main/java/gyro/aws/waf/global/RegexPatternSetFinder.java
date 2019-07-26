package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ListRegexPatternSetsRequest;
import software.amazon.awssdk.services.waf.model.ListRegexPatternSetsResponse;
import software.amazon.awssdk.services.waf.model.RegexPatternSet;
import software.amazon.awssdk.services.waf.model.RegexPatternSetSummary;
import software.amazon.awssdk.services.waf.model.WafNonexistentItemException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query regex pattern set.
 *
 * .. code-block:: gyro
 *
 *    regex-pattern-sets: $(external-query aws::waf-regex-pattern-set)
 */
@Type("waf-regex-pattern-set")
public class RegexPatternSetFinder extends gyro.aws.waf.common.RegexPatternSetFinder<WafClient, RegexPatternSetResource> {
    @Override
    protected List<RegexPatternSet> findAllAws(WafClient client) {
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
    protected List<RegexPatternSet> findAws(WafClient client, Map<String, String> filters) {
        List<RegexPatternSet> regexPatternSets = new ArrayList<>();

        try {
            regexPatternSets.add(client.getRegexPatternSet(r -> r.regexPatternSetId(filters.get("id"))).regexPatternSet());
        } catch (WafNonexistentItemException ignore) {
            //ignore
        }

        return regexPatternSets;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }
}