package gyro.aws.waf.common;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.waf.model.ByteMatchSet;
import software.amazon.awssdk.services.waf.model.PredicateType;

public abstract class ByteMatchSetResource extends ConditionResource implements Copyable<ByteMatchSet> {
    @Override
    String getDisplayName() {
        return "byte match set";
    }

    @Override
    protected String getType() {
        return PredicateType.BYTE_MATCH.toString();
    }
}
