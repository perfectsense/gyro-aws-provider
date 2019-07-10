package gyro.aws.waf.common;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.waf.model.GeoMatchSet;
import software.amazon.awssdk.services.waf.model.PredicateType;

public abstract class GeoMatchSetResource extends ConditionResource implements Copyable<GeoMatchSet> {
    @Override
    String getDisplayName() {
        return "waf geo match set";
    }

    @Override
    protected String getType() {
        return PredicateType.GEO_MATCH.toString();
    }
}
