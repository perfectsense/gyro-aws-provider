package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateGeoMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GeoMatchConstraint;
import software.amazon.awssdk.services.waf.model.GeoMatchSet;
import software.amazon.awssdk.services.waf.model.GetGeoMatchSetResponse;

//@ResourceName("geo-match-set")
public class GeoMatchSetResource extends gyro.aws.waf.common.GeoMatchSetResource {
    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getGeoMatchSetId())) {
            return false;
        }

        GetGeoMatchSetResponse response = getGlobalClient().getGeoMatchSet(
            r -> r.geoMatchSetId(getGeoMatchSetId())
        );

        GeoMatchSet geoMatchSet = response.geoMatchSet();
        setName(geoMatchSet.name());

        getGeoMatchConstraint().clear();
        for (GeoMatchConstraint geoMatchConstraint : geoMatchSet.geoMatchConstraints()) {
            GeoMatchConstraintResource geoMatchConstraintResource = new GeoMatchConstraintResource(geoMatchConstraint);
            geoMatchConstraintResource.parent(this);
            getGeoMatchConstraint().add(geoMatchConstraintResource);
        }

        return true;
    }

    @Override
    public void create() {
        WafClient client = getGlobalClient();

        CreateGeoMatchSetResponse response = client.createGeoMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        setGeoMatchSetId(response.geoMatchSet().geoMatchSetId());
    }

    @Override
    public void delete() {
        WafClient client = getGlobalClient();

        client.deleteGeoMatchSet(
            r -> r.geoMatchSetId(getGeoMatchSetId())
                .changeToken(client.getChangeToken().changeToken())
        );
    }
}
