package gyro.aws.waf.regional;

import software.amazon.awssdk.services.waf.model.GeoMatchConstraint;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

public class GeoMatchConstraintResource extends gyro.aws.waf.common.GeoMatchConstraintResource {
    @Override
    protected void saveGeoMatchConstraint(GeoMatchConstraint geoMatchConstraint, boolean isDelete) {
        WafRegionalClient client = getRegionalClient();

        client.updateGeoMatchSet(toUpdateGeoMatchSetRequest(geoMatchConstraint, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }
}
