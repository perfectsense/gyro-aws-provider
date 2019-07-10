package gyro.aws.waf.common;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.waf.model.IPSet;
import software.amazon.awssdk.services.waf.model.PredicateType;

public abstract class IpSetResource extends ConditionResource implements Copyable<IPSet> {
    @Override
    String getDisplayName() {
        return "waf ip set";
    }

    @Override
    protected String getType() {
        return PredicateType.IP_MATCH.toString();
    }
}
