package gyro.aws.waf.common;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.waf.model.XssMatchSet;

public abstract class XssMatchSetResource extends ConditionResource implements Copyable<XssMatchSet> {
    @Override
    String getDisplayName() {
        return "xss match set";
    }
}
