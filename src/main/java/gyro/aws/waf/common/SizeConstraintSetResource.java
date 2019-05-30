package gyro.aws.waf.common;

public abstract class SizeConstraintSetResource extends ConditionResource {
    @Override
    String getDisplayName() {
        return "size constraint set";
    }
}
