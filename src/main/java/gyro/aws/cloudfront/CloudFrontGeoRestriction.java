package gyro.aws.cloudfront;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.cloudfront.model.GeoRestriction;
import software.amazon.awssdk.services.cloudfront.model.Restrictions;

import java.util.ArrayList;
import java.util.List;

public class CloudFrontGeoRestriction extends Diffable implements Copyable<GeoRestriction> {

    private String type;
    private List<String> restrictions;

    /**
     * Type of restriction. Valid values are ``Whitelist`` or ``Blacklist``.
     */
    @Updatable
    public String getType() {
        if (type == null) {
            type = "none";
        }

        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * List of countries to whitelist or blacklist. Uses two letter country codes (i.e. US).
     */
    @Updatable
    public List<String> getRestrictions() {
        if (restrictions == null) {
            restrictions = new ArrayList<>();
        }

        return restrictions;
    }

    public void setRestrictions(List<String> restrictions) {
        this.restrictions = restrictions;
    }

    public Restrictions toRestrictions() {
        return Restrictions.builder()
            .geoRestriction(r -> r.restrictionType(getType())
                .items(getRestrictions())
                .quantity(getRestrictions().size())).build();
    }

    @Override
    public String primaryKey() {
        return "geo-restriction";
    }

    @Override
    public String toDisplayString() {
        return "geo restriction";
    }

    @Override
    public void copyFrom(GeoRestriction geoRestriction) {
        setType(geoRestriction.restrictionTypeAsString());
        setRestrictions(geoRestriction.items());
    }
}
