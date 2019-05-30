package gyro.aws.waf.common;

public abstract class SqlInjectionMatchSetResource extends ConditionResource {
    @Override
    String getDisplayName() {
        return "sql injection match set";
    }
}
