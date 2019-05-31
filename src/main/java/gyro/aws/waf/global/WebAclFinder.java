package gyro.aws.waf.global;

import gyro.core.Type;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.WebACL;
import software.amazon.awssdk.services.waf.model.WebACLSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query waf acl.
 *
 * .. code-block:: gyro
 *
 *    waf-acl: $(aws::waf-acl EXTERNAL/* | web-acl-id = '')
 */
@Type("waf-acl")
public class WebAclFinder extends gyro.aws.waf.common.WebAclFinder<WafClient, WebAclResource> {
    @Override
    protected List<WebACL> findAllAws(WafClient client) {
        List<WebACL> webACLS = new ArrayList<>();
        List<WebACLSummary> webACLSummaries = client.listWebACLs().webACLs();

        if (!webACLSummaries.isEmpty()) {
            for (WebACLSummary webACLSummary : webACLSummaries) {
                webACLS.add(client.getWebACL(r -> r.webACLId(webACLSummary.webACLId())).webACL());
            }
        }

        return webACLS;
    }

    @Override
    protected List<WebACL> findAws(WafClient client, Map<String, String> filters) {
        List<WebACL> webACLS = new ArrayList<>();

        if (filters.containsKey("web-acl-id")) {
            webACLS.add(client.getWebACL(r -> r.webACLId(filters.get("web-acl-id"))).webACL());
        }

        return webACLS;
    }
}
