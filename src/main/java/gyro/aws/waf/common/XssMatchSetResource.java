package gyro.aws.waf.common;

public abstract class XssMatchSetResource extends ConditionResource {
    @Override
    String getDisplayName() {
        return "xss match set";
    }
}
