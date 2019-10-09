package gyro.aws.route53;

import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

public class Geolocation extends Diffable {

    private String continentCode;
    private String countryCode;
    private String subdivisionCode;

    /**
     * The continent code. At least one of continent code, country code or subdivision code is required.
     */
    @Updatable
    public String getContinentCode() {
        return continentCode != null ? continentCode.toUpperCase() : null;
    }

    public void setContinentCode(String continentCode) {
        this.continentCode = continentCode;
    }

    /**
     * The country code. At least one of continent code, country code or subdivision code is required.
     */
    @Updatable
    public String getCountryCode() {
        return countryCode != null ? countryCode.toUpperCase() : null;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * The sub division code. At least one of continent code, country code or subdivision code is required.
     */
    @Updatable
    public String getSubdivisionCode() {
        return subdivisionCode != null ? subdivisionCode.toUpperCase() : null;
    }

    public void setSubdivisionCode(String subdivisionCode) {
        this.subdivisionCode = subdivisionCode;
    }

    @Override
    public String primaryKey() {
        return "geolocation configuration";
    }

}
