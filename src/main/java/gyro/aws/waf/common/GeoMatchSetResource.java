package gyro.aws.waf.common;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.waf.model.GeoMatchSet;

public abstract class GeoMatchSetResource extends ConditionResource implements Copyable<GeoMatchSet> {
    @Override
    String getDisplayName() {
        return "geo match set";
    }
}
