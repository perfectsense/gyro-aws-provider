package gyro.aws.waf.global;

import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.SizeConstraintSet;
import software.amazon.awssdk.services.waf.model.SizeConstraintSetSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query size constraint set.
 *
 * .. code-block:: gyro
 *
 *    size-constraint-sets: $(aws::size-constraint-set EXTERNAL/* | id = '')
 */
@Type("size-constraint-set")
public class SizeConstraintSetFinder extends gyro.aws.waf.common.SizeConstraintSetFinder<WafClient, SizeConstraintSetResource> {
    @Override
    protected List<SizeConstraintSet> findAllAws(WafClient client) {
        List<SizeConstraintSet> sizeConstraintSets = new ArrayList<>();

        List<SizeConstraintSetSummary> sizeConstraintSetSummaries = client.listSizeConstraintSets().sizeConstraintSets();

        for (SizeConstraintSetSummary sizeConstraintSetSummary : sizeConstraintSetSummaries) {
            sizeConstraintSets.add(client.getSizeConstraintSet(r -> r.sizeConstraintSetId(sizeConstraintSetSummary.sizeConstraintSetId())).sizeConstraintSet());
        }

        return sizeConstraintSets;
    }

    @Override
    protected List<SizeConstraintSet> findAws(WafClient client, Map<String, String> filters) {
        List<SizeConstraintSet> sizeConstraintSets = new ArrayList<>();

        if (filters.containsKey("size-constraint-set-id")) {
            sizeConstraintSets.add(client.getSizeConstraintSet(r -> r.sizeConstraintSetId(filters.get("size-constraint-set-id"))).sizeConstraintSet());
        }

        return sizeConstraintSets;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }
}