package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import software.amazon.awssdk.services.waf.model.GeoMatchSet;
import software.amazon.awssdk.services.waf.model.GeoMatchSetSummary;
import software.amazon.awssdk.services.waf.model.ListGeoMatchSetsRequest;
import software.amazon.awssdk.services.waf.model.ListGeoMatchSetsResponse;
import software.amazon.awssdk.services.waf.model.WafNonexistentItemException;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query geo match set regional.
 *
 * .. code-block:: gyro
 *
 *    geo-match-sets: $(external-query aws::waf-geo-match-set-regional)
 */
@Type("waf-geo-match-set-regional")
public class GeoMatchSetFinder extends gyro.aws.waf.common.GeoMatchSetFinder<WafRegionalClient, GeoMatchSetResource> {
    @Override
    protected List<GeoMatchSet> findAllAws(WafRegionalClient client) {
        List<GeoMatchSet> geoMatchSets = new ArrayList<>();

        String marker = null;
        ListGeoMatchSetsResponse response;
        List<GeoMatchSetSummary> geoMatchSetSummaries = new ArrayList<>();

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listGeoMatchSets();
            } else {
                response = client.listGeoMatchSets(ListGeoMatchSetsRequest.builder().nextMarker(marker).build());
            }

            marker = response.nextMarker();
            geoMatchSetSummaries.addAll(response.geoMatchSets());

        } while (!ObjectUtils.isBlank(marker));

        for (GeoMatchSetSummary geoMatchSetSummary : geoMatchSetSummaries) {
            geoMatchSets.add(client.getGeoMatchSet(r -> r.geoMatchSetId(geoMatchSetSummary.geoMatchSetId())).geoMatchSet());
        }

        return geoMatchSets;
    }

    @Override
    protected List<GeoMatchSet> findAws(WafRegionalClient client, Map<String, String> filters) {
        List<GeoMatchSet> geoMatchSets = new ArrayList<>();

        try {
            geoMatchSets.add(client.getGeoMatchSet(r -> r.geoMatchSetId(filters.get("geo-match-set-id"))).geoMatchSet());
        } catch (WafNonexistentItemException ignore) {
            //ignore
        }

        return geoMatchSets;
    }
}