package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.GeoMatchSet;
import software.amazon.awssdk.services.waf.model.GeoMatchSetSummary;
import software.amazon.awssdk.services.waf.model.ListGeoMatchSetsRequest;
import software.amazon.awssdk.services.waf.model.ListGeoMatchSetsResponse;
import software.amazon.awssdk.services.waf.model.WafNonexistentItemException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query geo match set.
 *
 * .. code-block:: gyro
 *
 *    geo-match-sets: $(aws::waf-geo-match-set EXTERNAL/* | id = '')
 */
@Type("waf-geo-match-set")
public class GeoMatchSetFinder extends gyro.aws.waf.common.GeoMatchSetFinder<WafClient, GeoMatchSetResource> {
    @Override
    protected List<GeoMatchSet> findAllAws(WafClient client) {
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
    protected List<GeoMatchSet> findAws(WafClient client, Map<String, String> filters) {
        List<GeoMatchSet> geoMatchSets = new ArrayList<>();

        try {
            geoMatchSets.add(client.getGeoMatchSet(r -> r.geoMatchSetId(filters.get("geo-match-set-id"))).geoMatchSet());
        } catch (WafNonexistentItemException ignore) {
            //ignore
        }

        return geoMatchSets;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }
}