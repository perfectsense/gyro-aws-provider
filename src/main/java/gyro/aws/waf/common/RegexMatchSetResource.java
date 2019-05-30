package gyro.aws.waf.common;

public abstract class RegexMatchSetResource extends ConditionResource {
    @Override
    String getDisplayName() {
        return "regex match set";
    }
}
