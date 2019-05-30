package gyro.aws.waf.common;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchSet;

public abstract class SqlInjectionMatchSetResource extends ConditionResource implements Copyable<SqlInjectionMatchSet> {
    @Override
    String getDisplayName() {
        return "sql injection match set";
    }
}
