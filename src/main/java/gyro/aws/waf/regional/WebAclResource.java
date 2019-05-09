package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.resource.ResourceType;
import gyro.core.resource.ResourceUpdatable;
import software.amazon.awssdk.services.waf.model.ActivatedRule;
import software.amazon.awssdk.services.waf.model.CreateWebAclRequest;
import software.amazon.awssdk.services.waf.model.CreateWebAclResponse;
import software.amazon.awssdk.services.waf.model.GetWebAclResponse;
import software.amazon.awssdk.services.waf.model.UpdateWebAclRequest;
import software.amazon.awssdk.services.waf.model.WebACL;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Creates a regional waf acl.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 * aws::waf-acl-regional waf-acl-example
 *     name: "waf-acl-example"
 *     metric-name: "wafAclExample"
 *     default-action: "ALLOW"
 *
 *     activated-rule
 *         action: "ALLOW"
 *         type: "REGULAR"
 *         priority: 1
 *         rule-id: $(aws::rule-regional rule-example-waf | rule-id)
 *     end
 *
 *     activated-rule
 *         action: "ALLOW"
 *         type: "RATE_BASED"
 *         priority: 2
 *         rule-id: $(aws::rate-rule-regional rate-rule-example-waf | rule-id)
 *     end
 * end
 */
@ResourceType("waf-acl-regional")
public class WebAclResource extends gyro.aws.waf.common.WebAclResource {
    private List<ActivatedRuleResource> activatedRule;

    /**
     * A list of activated rules specifying the connection between waf acl and rule.
     *
     * @subresource gyro.aws.waf.regional.ActivatedRuleResource
     */
    @ResourceUpdatable
    public List<ActivatedRuleResource> getActivatedRule() {
        if (activatedRule == null) {
            activatedRule = new ArrayList<>();
        }

        return activatedRule;
    }

    public void setActivatedRule(List<ActivatedRuleResource> activatedRule) {
        this.activatedRule = activatedRule;

        validateActivatedRule();
    }

    @Override
    protected WebACL getWebAcl() {
        if (ObjectUtils.isBlank(getWebAclId())) {
            return null;
        }

        GetWebAclResponse response = getRegionalClient().getWebACL(
            r -> r.webACLId(getWebAclId())
        );

        return response.webACL();
    }

    @Override
    protected void setActivatedRules(ActivatedRule activatedRule) {
        ActivatedRuleResource activatedRuleResource = new ActivatedRuleResource(activatedRule);
        getActivatedRule().add(activatedRuleResource);
    }

    @Override
    protected void clearActivatedRules() {
        getActivatedRule().clear();
    }

    @Override
    protected List<Integer> getActivatedRulesPriority() {
        return getActivatedRule().stream()
            .sorted(Comparator.comparing(ActivatedRuleResource::getPriority))
            .map(ActivatedRuleResource::getPriority).collect(Collectors.toList());
    }

    @Override
    protected CreateWebAclResponse doCreate(CreateWebAclRequest.Builder builder) {
        WafRegionalClient client = getRegionalClient();

        return client.createWebACL(builder.changeToken(client.getChangeToken().changeToken()).build());
    }

    @Override
    protected void doUpdate(UpdateWebAclRequest.Builder builder) {
        WafRegionalClient client = getRegionalClient();

        client.updateWebACL(builder.changeToken(client.getChangeToken().changeToken()).build());
    }

    @Override
    public void delete() {
        WafRegionalClient client = getRegionalClient();

        client.deleteWebACL(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .webACLId(getWebAclId())
        );
    }
}
