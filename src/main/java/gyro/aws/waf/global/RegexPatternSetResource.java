package gyro.aws.waf.global;

import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateRegexPatternSetResponse;
import software.amazon.awssdk.services.waf.model.RegexMatchSet;
import software.amazon.awssdk.services.waf.model.RegexMatchSetSummary;
import software.amazon.awssdk.services.waf.model.RegexPatternSet;
import software.amazon.awssdk.services.waf.model.UpdateRegexPatternSetRequest;

import java.util.List;

//@ResourceName("regex-pattern-set")
public class RegexPatternSetResource extends gyro.aws.waf.common.RegexPatternSetResource {
    @Override
    protected void doCreate() {
        WafClient client = getGlobalClient();

        CreateRegexPatternSetResponse response = client.createRegexPatternSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setRegexPatternSetId(response.regexPatternSet().regexPatternSetId());
    }

    @Override
    protected void savePatterns(List<String> oldPatterns, List<String> newPatterns) {
        WafClient client = getGlobalClient();

        UpdateRegexPatternSetRequest regexPatternSetRequest = getUpdateRegexPatternSetRequest(oldPatterns, newPatterns)
            .changeToken(client.getChangeToken().changeToken())
            .build();


        if (!regexPatternSetRequest.updates().isEmpty()) {
            client.updateRegexPatternSet(regexPatternSetRequest);
        }
    }

    @Override
    protected void deleteRegexPatternSet() {
        WafClient client = getGlobalClient();

        client.deleteRegexPatternSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .regexPatternSetId(getRegexPatternSetId())
        );
    }

    @Override
    protected List<RegexMatchSetSummary> getRegexMatchSetSummaries() {
        return getGlobalClient().listRegexMatchSets().regexMatchSets();
    }

    @Override
    protected RegexMatchSet getRegexMatchSet(String regexMatchSetId) {
        return getGlobalClient().getRegexMatchSet(
            r -> r.regexMatchSetId(regexMatchSetId)
        ).regexMatchSet();
    }

    @Override
    protected RegexPatternSet getRegexPatternSet() {
        return getGlobalClient().getRegexPatternSet(
            r -> r.regexPatternSetId(getRegexPatternSetId())
        ).regexPatternSet();
    }
}
