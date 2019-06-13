package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
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
 *    regex-pattern-sets: $(aws::regex-pattern-set-regional EXTERNAL/* | regex-pattern-set-id = '')
 */
@Type("regex-pattern-set-regional")
public class RegexPatternSetFinder extends gyro.aws.waf.common.RegexPatternSetFinder<WafRegionalClient, RegexPatternSetResource> {
    @Override
    protected List<RegexPatternSet> findAllAws(WafRegionalClient client) {
        List<RegexPatternSet> regexPatternSets = new ArrayList<>();

        List<RegexPatternSetSummary> regexPatternSetSummaries = client.listRegexPatternSets().regexPatternSets();

        for (RegexPatternSetSummary regexPatternSetSummary : regexPatternSetSummaries) {
            regexPatternSets.add(client.getRegexPatternSet(r -> r.regexPatternSetId(regexPatternSetSummary.regexPatternSetId())).regexPatternSet());
        }

        return regexPatternSets;
    }

    @Override
    protected List<RegexPatternSet> findAws(WafRegionalClient client, Map<String, String> filters) {
        List<RegexPatternSet> regexPatternSets = new ArrayList<>();

        if (filters.containsKey("regex-pattern-set-id") && !ObjectUtils.isBlank(filters.get("regex-pattern-set-id"))) {
            try {
                regexPatternSets.add(client.getRegexPatternSet(r -> r.regexPatternSetId(filters.get("regex-pattern-set-id"))).regexPatternSet());
            } catch (WafNonexistentItemException ignore) {
                //ignore
            }
        }

        return regexPatternSets;
    }
}