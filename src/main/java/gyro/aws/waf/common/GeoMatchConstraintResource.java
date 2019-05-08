package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.Resource;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.GeoMatchConstraint;
import software.amazon.awssdk.services.waf.model.GeoMatchSetUpdate;
import software.amazon.awssdk.services.waf.model.UpdateGeoMatchSetRequest;

import java.util.Set;

public abstract class GeoMatchConstraintResource extends AbstractWafResource {
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

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        saveGeoMatchConstraint(getGeoMatchConstraint(), false);
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        saveGeoMatchConstraint(getGeoMatchConstraint(), true);
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

    protected abstract void saveGeoMatchConstraint(GeoMatchConstraint geoMatchConstraint, boolean isDelete);

    private GeoMatchConstraint getGeoMatchConstraint() {
        return GeoMatchConstraint.builder()
            .type(getType())
            .value(getValue())
            .build();
    }

    protected UpdateGeoMatchSetRequest.Builder getUpdateGeoMatchSetRequest(GeoMatchConstraint geoMatchConstraint, boolean isDelete) {
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
