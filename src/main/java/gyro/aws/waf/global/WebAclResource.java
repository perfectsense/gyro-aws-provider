package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ActivatedRule;
import software.amazon.awssdk.services.waf.model.CreateWebAclRequest;
import software.amazon.awssdk.services.waf.model.CreateWebAclResponse;
import software.amazon.awssdk.services.waf.model.GetWebAclResponse;
import software.amazon.awssdk.services.waf.model.UpdateWebAclRequest;
import software.amazon.awssdk.services.waf.model.WebACL;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Creates a global waf acl.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 * aws::waf-acl waf-acl-example
 *     name: "waf-acl-example"
 *     metric-name: "wafAclExample"
 *     default-action: "ALLOW"
 *
 *     activated-rule
 *         action: "ALLOW"
 *         type: "REGULAR"
 *         priority: 1
 *         rule-id: $(aws::rule rule-example-waf | rule-id)
 *     end
 *
 *     activated-rule
 *         action: "ALLOW"
 *         type: "RATE_BASED"
 *         priority: 2
 *         rule-id: $(aws::rate-rule rate-rule-example-waf | rule-id)
 *     end
 * end
 */
@Type("waf-acl")
public class WebAclResource extends gyro.aws.waf.common.WebAclResource {
    private List<ActivatedRuleResource> activatedRule;

    /**
     * A list of activated rules specifying the connection between waf acl and rule.
     *
     * @subresource gyro.aws.waf.global.ActivatedRuleResource
     */
    @Updatable
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

        GetWebAclResponse response = getGlobalClient().getWebACL(
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
        WafClient client = getGlobalClient();

        return client.createWebACL(builder.changeToken(client.getChangeToken().changeToken()).build());
    }

    @Override
    protected void doUpdate(UpdateWebAclRequest.Builder builder) {
        WafClient client = getGlobalClient();

        client.updateWebACL(builder.changeToken(client.getChangeToken().changeToken()).build());
    }

    @Override
    public void delete() {
        WafClient client = getGlobalClient();

        client.deleteWebACL(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .webACLId(getWebAclId())
        );
    }
}
