package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import software.amazon.awssdk.services.waf.model.ListWebAcLsRequest;
import software.amazon.awssdk.services.waf.model.ListWebAcLsResponse;
import software.amazon.awssdk.services.waf.model.WafNonexistentItemException;
import software.amazon.awssdk.services.waf.model.WebACL;
import software.amazon.awssdk.services.waf.model.WebACLSummary;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query waf acl regional.
 *
 * .. code-block:: gyro
 *
 *    waf-acl: $(aws::waf-acl-regional EXTERNAL/* | web-acl-id = '')
 */
@Type("waf-acl-regional")
public class WebAclFinder extends gyro.aws.waf.common.WebAclFinder<WafRegionalClient, WebAclResource> {
    @Override
    protected List<WebACL> findAllAws(WafRegionalClient client) {
        List<WebACL> webACLS = new ArrayList<>();

        String marker = null;
        ListWebAcLsResponse response;
        List<WebACLSummary> webACLSummaries = new ArrayList<>();

        do {
            if (ObjectUtils.isBlank(marker)) {
                response = client.listWebACLs();
            } else {
                response = client.listWebACLs(ListWebAcLsRequest.builder().nextMarker(marker).build());
            }

            marker = response.nextMarker();
            webACLSummaries.addAll(response.webACLs());

        } while (!ObjectUtils.isBlank(marker));

        if (!webACLSummaries.isEmpty()) {
            for (WebACLSummary webACLSummary : webACLSummaries) {
                webACLS.add(client.getWebACL(r -> r.webACLId(webACLSummary.webACLId())).webACL());
            }
        }

        return webACLS;
    }

    @Override
    protected List<WebACL> findAws(WafRegionalClient client, Map<String, String> filters) {
        List<WebACL> webACLS = new ArrayList<>();

        try {
            webACLS.add(client.getWebACL(r -> r.webACLId(filters.get("web-acl-id"))).webACL());
        } catch (WafNonexistentItemException ignore) {
            //ignore
        }

        return webACLS;
    }
}
