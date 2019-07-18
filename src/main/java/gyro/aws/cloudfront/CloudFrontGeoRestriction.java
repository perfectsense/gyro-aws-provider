package gyro.aws.cloudfront;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.cloudfront.model.GeoRestriction;
import software.amazon.awssdk.services.cloudfront.model.Restrictions;

import java.util.HashSet;
import java.util.Set;

public class CloudFrontGeoRestriction extends Diffable implements Copyable<GeoRestriction> {

    private String type;
    private Set<String> restrictions;

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
    public Set<String> getRestrictions() {
        if (restrictions == null) {
            restrictions = new HashSet<>();
        }

        return restrictions;
    }

    public void setRestrictions(Set<String> restrictions) {
        this.restrictions = restrictions;
    }

    @Override
    public void copyFrom(GeoRestriction geoRestriction) {
        setType(geoRestriction.restrictionTypeAsString());
        setRestrictions(new HashSet<>(geoRestriction.items()));
    }

    @Override
    public String primaryKey() {
        return "geo-restriction";
    }

    Restrictions toRestrictions() {
        return Restrictions.builder()
            .geoRestriction(r -> r.restrictionType(getType())
                .items(getRestrictions())
                .quantity(getRestrictions().size())).build();
    }
}
