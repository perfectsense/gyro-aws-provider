package gyro.aws.wafv2;

import java.util.HashSet;
import java.util.Set;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.wafv2.model.GeoMatchStatement;

public class GeoMatchStatementResource extends WafDiffable implements Copyable<GeoMatchStatement> {

    private Set<String> countryCodes;

    public Set<String> getCountryCodes() {
        return countryCodes;
    }

    public void setCountryCodes(Set<String> countryCodes) {
        this.countryCodes = countryCodes;
    }

    @Override
    public void copyFrom(GeoMatchStatement geoMatchStatement) {
        setCountryCodes(new HashSet<>(geoMatchStatement.countryCodesAsStrings()));
        setHashCode(geoMatchStatement.hashCode());
    }

    GeoMatchStatement toGeoMatchStatement() {
        return  GeoMatchStatement.builder()
            .countryCodesWithStrings(getCountryCodes())
            .build();
    }
}
