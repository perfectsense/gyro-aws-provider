package gyro.aws.waf.common;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.waf.model.RegexMatchSet;

public abstract class RegexMatchSetResource extends ConditionResource implements Copyable<RegexMatchSet> {
    @Override
    String getDisplayName() {
        return "regex match set";
    }
}
