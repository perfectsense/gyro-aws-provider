package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.IPSet;
import software.amazon.awssdk.services.waf.model.IPSetSummary;
import software.amazon.awssdk.services.waf.model.WafNonexistentItemException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query ip set.
 *
 * .. code-block:: gyro
 *
 *    ip-sets: $(aws::ip-set EXTERNAL/* | id = '')
 */
@Type("ip-set")
public class IpSetFinder extends gyro.aws.waf.common.IpSetFinder<WafClient, IpSetResource> {
    @Override
    protected List<IPSet> findAllAws(WafClient client) {
        List<IPSet> ipSets = new ArrayList<>();

        List<IPSetSummary> ipSetSummaries = client.listIPSets().ipSets();

        for (IPSetSummary ipSetSummary : ipSetSummaries) {
            ipSets.add(client.getIPSet(r -> r.ipSetId(ipSetSummary.ipSetId())).ipSet());
        }

        return ipSets;
    }

    @Override
    protected List<IPSet> findAws(WafClient client, Map<String, String> filters) {
        List<IPSet> ipSets = new ArrayList<>();

        if (filters.containsKey("ip-set-id") && !ObjectUtils.isBlank(filters.get("ip-set-id"))) {
            try {
                ipSets.add(client.getIPSet(r -> r.ipSetId(filters.get("ip-set-id"))).ipSet());
            } catch (WafNonexistentItemException ignore) {
                //ignore
            }
        }

        return ipSets;
    }

    @Override
    protected String getRegion() {
        return Region.AWS_GLOBAL.toString();
    }
}