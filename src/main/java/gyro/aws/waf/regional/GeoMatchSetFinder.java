package gyro.aws.waf.regional;

import gyro.core.Type;
import software.amazon.awssdk.services.waf.model.GeoMatchSet;
import software.amazon.awssdk.services.waf.model.GeoMatchSetSummary;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query geo match set regional.
 *
 * .. code-block:: gyro
 *
 *    geo-match-sets: $(aws::geo-match-set-regional EXTERNAL/* | id = '')
 */
@Type("geo-match-set-regional")
public class GeoMatchSetFinder extends gyro.aws.waf.common.GeoMatchSetFinder<WafRegionalClient, GeoMatchSetResource> {
    @Override
    protected List<GeoMatchSet> findAllAws(WafRegionalClient client) {
        List<GeoMatchSet> geoMatchSets = new ArrayList<>();

        List<GeoMatchSetSummary> geoMatchSetSummaries = client.listGeoMatchSets().geoMatchSets();

        for (GeoMatchSetSummary geoMatchSetSummary : geoMatchSetSummaries) {
            geoMatchSets.add(client.getGeoMatchSet(r -> r.geoMatchSetId(geoMatchSetSummary.geoMatchSetId())).geoMatchSet());
        }

        return geoMatchSets;
    }

    @Override
    protected List<GeoMatchSet> findAws(WafRegionalClient client, Map<String, String> filters) {
        List<GeoMatchSet> geoMatchSets = new ArrayList<>();

        if (filters.containsKey("geo-match-set-id")) {
            geoMatchSets.add(client.getGeoMatchSet(r -> r.geoMatchSetId(filters.get("geo-match-set-id"))).geoMatchSet());
        }

        return geoMatchSets;
    }
}