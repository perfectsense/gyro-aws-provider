package gyro.aws.waf;

import gyro.core.resource.ResourceName;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.GeoMatchConstraint;
import software.amazon.awssdk.services.waf.model.GeoMatchSetUpdate;
import software.amazon.awssdk.services.waf.model.UpdateGeoMatchSetRequest;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.Set;

@ResourceName(parent = "geo-match-set", value = "geo-match-constraint")
public class GeoMatchConstraintResource extends AbstractWafResource {

    private String value;
    private String type;

    /**
     * The value filter. Uses two letter country codes (i.e. US) when type selected as ```COUNTRY```.
     */
    public String getValue() {
        return value != null ? value.toUpperCase() : null;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * The type of geo match filter. Allowed values ```Country```
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public GeoMatchConstraintResource() {

    }

    public GeoMatchConstraintResource(GeoMatchConstraint geoMatchConstraint) {
        setType(geoMatchConstraint.typeAsString());
        setValue(geoMatchConstraint.valueAsString());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        if (getRegionalWaf()) {
            saveGeoMatchConstraint(getRegionalClient(), getGeoMatchConstraint(), false);
        } else {
            saveGeoMatchConstraint(getGlobalClient(), getGeoMatchConstraint(), false);
        }
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        if (getRegionalWaf()) {
            saveGeoMatchConstraint(getRegionalClient(), getGeoMatchConstraint(), true);
        } else {
            saveGeoMatchConstraint(getGlobalClient(), getGeoMatchConstraint(), true);
        }
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("geo match constraint");

        if (!ObjectUtils.isBlank(getValue())) {
            sb.append(" - ").append(getValue());
        }

        if (!ObjectUtils.isBlank(getType())) {
            sb.append(" - ").append(getType());
        }

        return sb.toString();
    }

    @Override
    public String primaryKey() {
        return String.format("%s %s", getValue(), getType());
    }

    @Override
    public String resourceIdentifier() {
        return null;
    }

    private GeoMatchConstraint getGeoMatchConstraint() {
        return GeoMatchConstraint.builder()
            .type(getType())
            .value(getValue())
            .build();
    }

    private void saveGeoMatchConstraint(WafClient client, GeoMatchConstraint geoMatchConstraint, boolean isDelete) {
        client.updateGeoMatchSet(getUpdateGeoMatchSetRequest(geoMatchConstraint, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }

    private void saveGeoMatchConstraint(WafRegionalClient client, GeoMatchConstraint geoMatchConstraint, boolean isDelete) {
        client.updateGeoMatchSet(getUpdateGeoMatchSetRequest(geoMatchConstraint, isDelete)
            .changeToken(client.getChangeToken().changeToken())
            .build()
        );
    }

    private UpdateGeoMatchSetRequest.Builder getUpdateGeoMatchSetRequest(GeoMatchConstraint geoMatchConstraint, boolean isDelete) {
        GeoMatchSetResource parent = (GeoMatchSetResource) parent();

        GeoMatchSetUpdate geoMatchSetUpdate = GeoMatchSetUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .geoMatchConstraint(geoMatchConstraint)
            .build();

        return UpdateGeoMatchSetRequest.builder()
            .geoMatchSetId(parent.getGeoMatchSetId())
            .updates(geoMatchSetUpdate);
    }
}
