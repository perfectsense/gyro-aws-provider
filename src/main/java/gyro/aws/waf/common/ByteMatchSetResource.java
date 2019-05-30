package gyro.aws.waf.common;

public abstract class ByteMatchSetResource extends ConditionResource {
    @Override
    String getDisplayName() {
        return "byte match set";
    }
}
