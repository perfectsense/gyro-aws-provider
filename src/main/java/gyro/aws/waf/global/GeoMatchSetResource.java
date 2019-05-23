package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateGeoMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GeoMatchConstraint;
import software.amazon.awssdk.services.waf.model.GeoMatchSet;
import software.amazon.awssdk.services.waf.model.GetGeoMatchSetResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates a global geo match set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 * aws::geo-match-set geo-match-set-example
 *     name: "geo-match-set-example"
 *
 *     geo-match-constraint
 *         type: "Country"
 *         value: "TL"
 *     end
 * end
 */
@Type("geo-match-set")
public class GeoMatchSetResource extends gyro.aws.waf.common.GeoMatchSetResource {
    private List<GeoMatchConstraintResource> geoMatchConstraint;

    /**
     * List of geo match constraint data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.global.GeoMatchConstraintResource
     */
    @Updatable
    public List<GeoMatchConstraintResource> getGeoMatchConstraint() {
        if (geoMatchConstraint == null) {
            geoMatchConstraint = new ArrayList<>();
        }

        return geoMatchConstraint;
    }

    public void setGeoMatchConstraint(List<GeoMatchConstraintResource> geoMatchConstraint) {
        this.geoMatchConstraint = geoMatchConstraint;
    }

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
