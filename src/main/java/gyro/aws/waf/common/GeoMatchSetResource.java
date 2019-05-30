package gyro.aws.waf.common;

public abstract class GeoMatchSetResource extends ConditionResource {
    @Override
    String getDisplayName() {
        return "geo match set";
    }
}
