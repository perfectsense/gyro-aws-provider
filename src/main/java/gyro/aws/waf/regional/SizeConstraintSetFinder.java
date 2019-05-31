package gyro.aws.waf.regional;

import gyro.core.Type;
import software.amazon.awssdk.services.waf.model.SizeConstraintSet;
import software.amazon.awssdk.services.waf.model.SizeConstraintSetSummary;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query size constraint set regional.
 *
 * .. code-block:: gyro
 *
 *    size-constraint-sets: $(aws::size-constraint-set-regional EXTERNAL/* | id = '')
 */
@Type("size-constraint-set-regional")
public class SizeConstraintSetFinder extends gyro.aws.waf.common.SizeConstraintSetFinder<WafRegionalClient, SizeConstraintSetResource> {
    @Override
    protected List<SizeConstraintSet> findAllAws(WafRegionalClient client) {
        List<SizeConstraintSet> sizeConstraintSets = new ArrayList<>();

        List<SizeConstraintSetSummary> sizeConstraintSetSummaries = client.listSizeConstraintSets().sizeConstraintSets();

        for (SizeConstraintSetSummary sizeConstraintSetSummary : sizeConstraintSetSummaries) {
            sizeConstraintSets.add(client.getSizeConstraintSet(r -> r.sizeConstraintSetId(sizeConstraintSetSummary.sizeConstraintSetId())).sizeConstraintSet());
        }

        return sizeConstraintSets;
    }

    @Override
    protected List<SizeConstraintSet> findAws(WafRegionalClient client, Map<String, String> filters) {
        List<SizeConstraintSet> sizeConstraintSets = new ArrayList<>();

        if (filters.containsKey("size-constraint-set-id")) {
            sizeConstraintSets.add(client.getSizeConstraintSet(r -> r.sizeConstraintSetId(filters.get("size-constraint-set-id"))).sizeConstraintSet());
        }

        return sizeConstraintSets;
    }
}