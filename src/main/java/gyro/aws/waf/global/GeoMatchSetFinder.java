package gyro.aws.waf.global;

import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.GeoMatchSet;
import software.amazon.awssdk.services.waf.model.GeoMatchSetSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query geo match set.
 *
 * .. code-block:: gyro
 *
 *    geo-match-sets: $(aws::geo-match-set EXTERNAL/* | id = '')
 */
@Type("geo-match-set")
public class GeoMatchSetFinder extends gyro.aws.waf.common.GeoMatchSetFinder<WafClient, GeoMatchSetResource> {
    @Override
    protected List<GeoMatchSet> findAllAws(WafClient client) {
        List<GeoMatchSet> geoMatchSets = new ArrayList<>();

        List<GeoMatchSetSummary> geoMatchSetSummaries = client.listGeoMatchSets().geoMatchSets();

        for (GeoMatchSetSummary geoMatchSetSummary : geoMatchSetSummaries) {
            geoMatchSets.add(client.getGeoMatchSet(r -> r.geoMatchSetId(geoMatchSetSummary.geoMatchSetId())).geoMatchSet());
        }

        return geoMatchSets;
    }

    @Override
    protected List<GeoMatchSet> findAws(WafClient client, Map<String, String> filters) {
        List<GeoMatchSet> geoMatchSets = new ArrayList<>();

        if (filters.containsKey("geo-match-set-id")) {
            geoMatchSets.add(client.getGeoMatchSet(r -> r.geoMatchSetId(filters.get("geo-match-set-id"))).geoMatchSet());
        }

        return geoMatchSets;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }
}