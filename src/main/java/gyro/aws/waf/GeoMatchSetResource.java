package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceOutput;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.CreateGeoMatchSetResponse;
import software.amazon.awssdk.services.waf.model.GeoMatchConstraint;
import software.amazon.awssdk.services.waf.model.GeoMatchSet;
import software.amazon.awssdk.services.waf.model.GetGeoMatchSetResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Creates a geo match set.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::geo-match-set geo-match-set-example
 *         name: "geo-match-set-example"
 *
 *         geo-match-constraint
 *             type: "Country"
 *             value: "TL"
 *         end
 *     end
 */
@ResourceType("geo-match-set")
public class GeoMatchSetResource extends AwsResource {
    private String name;
    private String geoMatchSetId;
    private List<GeoMatchConstraintResource> geoMatchConstraint;

    /**
     * The name of the geo match condition. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ResourceOutput
    public String getGeoMatchSetId() {
        return geoMatchSetId;
    }

    public void setGeoMatchSetId(String geoMatchSetId) {
        this.geoMatchSetId = geoMatchSetId;
    }

    /**
     * List of geo match constraint data defining the condition. (Required)
     *
     * @subresource gyro.aws.waf.GeoMatchConstraintResource
     */
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

        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        GetGeoMatchSetResponse response = client.getGeoMatchSet(
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
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        CreateGeoMatchSetResponse response = client.createGeoMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
        );

        GeoMatchSet geoMatchSet = response.geoMatchSet();
        setGeoMatchSetId(geoMatchSet.geoMatchSetId());
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {

    }

    @Override
    public void delete() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        client.deleteGeoMatchSet(
            r -> r.geoMatchSetId(getGeoMatchSetId())
                .changeToken(client.getChangeToken().changeToken())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("geo match set");

        if (!ObjectUtils.isBlank(getName())) {
            sb.append(" - ").append(getName());
        }

        if (!ObjectUtils.isBlank(getGeoMatchSetId())) {
            sb.append(" - ").append(getGeoMatchSetId());
        }

        return sb.toString();
    }
}
