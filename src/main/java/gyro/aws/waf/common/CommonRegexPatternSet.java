package gyro.aws.waf.common;

import gyro.core.Type;
import software.amazon.awssdk.services.waf.model.RegexMatchSet;
import software.amazon.awssdk.services.waf.model.RegexMatchSetSummary;
import software.amazon.awssdk.services.waf.model.RegexPatternSet;

import java.util.List;

@Type("common-pattern")
public class CommonRegexPatternSet extends RegexPatternSetResource {
    @Override
    protected void doCreate() {

    }

    @Override
    protected void savePatterns(List<String> oldPatterns, List<String> newPatterns) {

    }

    @Override
    protected void deleteRegexPatternSet() {

    }

    @Override
    protected List<RegexMatchSetSummary> getRegexMatchSetSummaries() {
        return null;
    }

    @Override
    protected RegexMatchSet getRegexMatchSet(String regexMatchSetId) {
        return null;
    }

    @Override
    protected RegexPatternSet getRegexPatternSet() {
        return null;
    }
}
