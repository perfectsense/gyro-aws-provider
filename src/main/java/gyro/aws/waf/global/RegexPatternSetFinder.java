package gyro.aws.waf.global;

import gyro.core.Type;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.RegexPatternSet;
import software.amazon.awssdk.services.waf.model.RegexPatternSetSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query regex pattern set.
 *
 * .. code-block:: gyro
 *
 *    regex-pattern-sets: $(aws::regex-pattern-set EXTERNAL/* | regex-pattern-set-id = '')
 */
@Type("regex-pattern-set")
public class RegexPatternSetFinder extends gyro.aws.waf.common.RegexPatternSetFinder<WafClient, RegexPatternSetResource> {
    @Override
    protected List<RegexPatternSet> findAllAws(WafClient client) {
        List<RegexPatternSet> regexPatternSets = new ArrayList<>();

        List<RegexPatternSetSummary> regexPatternSetSummaries = client.listRegexPatternSets().regexPatternSets();

        for (RegexPatternSetSummary regexPatternSetSummary : regexPatternSetSummaries) {
            regexPatternSets.add(client.getRegexPatternSet(r -> r.regexPatternSetId(regexPatternSetSummary.regexPatternSetId())).regexPatternSet());
        }

        return regexPatternSets;
    }

    @Override
    protected List<RegexPatternSet> findAws(WafClient client, Map<String, String> filters) {
        List<RegexPatternSet> regexPatternSets = new ArrayList<>();

        if (filters.containsKey("regex-pattern-set-id")) {
            regexPatternSets.add(client.getRegexPatternSet(r -> r.regexPatternSetId(filters.get("regex-pattern-set-id"))).regexPatternSet());
        }

        return regexPatternSets;
    }
}