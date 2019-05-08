package gyro.aws.waf.regional;

import software.amazon.awssdk.services.waf.model.GeoMatchConstraint;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

//@ResourceName(parent = "geo-match-set", value = "geo-match-constraint")
public class GeoMatchConstraintResource extends gyro.aws.waf.common.GeoMatchConstraintResource {
    public GeoMatchConstraintResource() {

    }

    public GeoMatchConstraintResource(GeoMatchConstraint geoMatchConstraint) {
        setType(geoMatchConstraint.typeAsString());
        setValue(geoMatchConstraint.valueAsString());
    }

    @Override
    protected void saveGeoMatchConstraint(GeoMatchConstraint geoMatchConstraint, boolean isDelete) {
        WafRegionalClient client = getRegionalClient();

        client.updateGeoMatchSet(getUpdateGeoMatchSetRequest(geoMatchConstraint, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }
}
