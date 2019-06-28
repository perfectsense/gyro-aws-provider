package gyro.aws.waf.common;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.waf.model.PredicateType;
import software.amazon.awssdk.services.waf.model.SizeConstraintSet;

public abstract class SizeConstraintSetResource extends ConditionResource implements Copyable<SizeConstraintSet> {
    @Override
    String getDisplayName() {
        return "size constraint set";
    }

    @Override
    protected String getType() {
        return PredicateType.SIZE_CONSTRAINT.toString();
    }
}
