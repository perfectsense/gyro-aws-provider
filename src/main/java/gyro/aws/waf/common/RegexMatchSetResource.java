package gyro.aws.waf.common;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.waf.model.PredicateType;
import software.amazon.awssdk.services.waf.model.RegexMatchSet;

public abstract class RegexMatchSetResource extends ConditionResource implements Copyable<RegexMatchSet> {
    @Override
    String getDisplayName() {
        return "waf regex match set";
    }

    @Override
    protected String getType() {
        return PredicateType.REGEX_MATCH.toString();
    }
}
