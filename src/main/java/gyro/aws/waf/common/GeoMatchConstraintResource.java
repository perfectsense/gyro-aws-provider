package gyro.aws.waf.common;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.core.resource.Resource;
import software.amazon.awssdk.services.waf.model.ChangeAction;
import software.amazon.awssdk.services.waf.model.GeoMatchConstraint;
import software.amazon.awssdk.services.waf.model.GeoMatchSetUpdate;
import software.amazon.awssdk.services.waf.model.UpdateGeoMatchSetRequest;

import java.util.Set;

public abstract class GeoMatchConstraintResource extends AbstractWafResource implements Copyable<GeoMatchConstraint> {
    private String value;
    private String type;

    /**
     * The value filter. Uses two letter country codes (i.e. US) when type selected as ``COUNTRY``. (Required)
     */
    public String getValue() {
        return value != null ? value.toUpperCase() : null;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * The type of geo match filter. Allowed values are ``Country``. (Required)
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void copyFrom(GeoMatchConstraint geoMatchConstraint) {
        setType(geoMatchConstraint.typeAsString());
        setValue(geoMatchConstraint.valueAsString());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public void create() {
        saveGeoMatchConstraint(toGeoMatchConstraint(), false);
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {

    }

    @Override
    public void delete() {
        saveGeoMatchConstraint(toGeoMatchConstraint(), true);
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

    private GeoMatchConstraint toGeoMatchConstraint() {
        return GeoMatchConstraint.builder()
            .type(getType())
            .value(getValue())
            .build();
    }

    protected UpdateGeoMatchSetRequest.Builder toUpdateGeoMatchSetRequest(GeoMatchConstraint geoMatchConstraint, boolean isDelete) {
        GeoMatchSetResource parent = (GeoMatchSetResource) parent();

        GeoMatchSetUpdate geoMatchSetUpdate = GeoMatchSetUpdate.builder()
            .action(!isDelete ? ChangeAction.INSERT : ChangeAction.DELETE)
            .geoMatchConstraint(geoMatchConstraint)
            .build();

        return UpdateGeoMatchSetRequest.builder()
            .geoMatchSetId(parent.getId())
            .updates(geoMatchSetUpdate);
    }
}
