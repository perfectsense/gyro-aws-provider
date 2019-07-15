package gyro.aws.waf.common;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.waf.model.PredicateType;
import software.amazon.awssdk.services.waf.model.SqlInjectionMatchSet;

public abstract class SqlInjectionMatchSetResource extends ConditionResource implements Copyable<SqlInjectionMatchSet> {
    @Override
    String getDisplayName() {
        return "waf sql injection match set";
    }

    @Override
    protected String getType() {
        return PredicateType.SQL_INJECTION_MATCH.toString();
    }
}
