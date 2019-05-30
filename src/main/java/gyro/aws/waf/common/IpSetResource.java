package gyro.aws.waf.common;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.waf.model.IPSet;

public abstract class IpSetResource extends ConditionResource implements Copyable<IPSet> {
    @Override
    String getDisplayName() {
        return "ip set";
    }
}
