package gyro.aws.waf.common;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.waf.model.ByteMatchSet;

public abstract class ByteMatchSetResource extends ConditionResource implements Copyable<ByteMatchSet> {
    @Override
    String getDisplayName() {
        return "byte match set";
    }
}
