package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.core.resource.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.GeoMatchConstraint;
import software.amazon.awssdk.services.waf.model.GeoMatchSetUpdate;

import java.util.Set;

public class GeoMatchConstraintResource extends AwsResource {

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
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        saveGeoMatchConstraint(client, getGeoMatchConstraint(), false);
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        saveGeoMatchConstraint(client, getGeoMatchConstraint(), true);
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

    private void saveGeoMatchConstraint(WafClient client, GeoMatchConstraint geoMatchConstraint, boolean isDelte) {
        GeoMatchSetResource parent = (GeoMatchSetResource) parent();

        GeoMatchSetUpdate geoMatchSetUpdate = GeoMatchSetUpdate.builder()
            .action(!isDelte ? ChangeAction.INSERT : ChangeAction.DELETE)
            .geoMatchConstraint(geoMatchConstraint)
            .build();

        client.updateGeoMatchSet(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .geoMatchSetId(parent.getGeoMatchSetId())
                .updates(geoMatchSetUpdate)
        );
    }
}
