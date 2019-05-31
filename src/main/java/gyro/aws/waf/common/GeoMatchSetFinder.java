package gyro.aws.waf.common;

import gyro.aws.AwsFinder;
import gyro.aws.AwsResource;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.waf.model.GeoMatchSet;

abstract public class GeoMatchSetFinder<T extends SdkClient, U extends AwsResource> extends AwsFinder<T, GeoMatchSet, U> {
    private String GeoMatchSetId;

    /**
     * The id of geo match set.
     */
    public String getGeoMatchSetId() {
        return GeoMatchSetId;
    }

    public void setGeoMatchSetId(String geoMatchSetId) {
        GeoMatchSetId = geoMatchSetId;
    }
}