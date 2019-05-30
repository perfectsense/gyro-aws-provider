package gyro.aws.waf.common;

public abstract class IpSetResource extends ConditionResource {
    @Override
    String getDisplayName() {
        return "ip set";
    }
}
