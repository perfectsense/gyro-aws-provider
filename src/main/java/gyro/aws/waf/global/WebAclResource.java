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

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a global waf acl.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 * aws::waf-web-acl waf-web-acl-example
 *     name: "waf-web-acl-example"
 *     metric-name: "wafAclExample"
 *     default-action
 *         type: "ALLOW"
 *     end
 *
 *     rule
 *         action
 *             type: "ALLOW"
 *         end
 *         priority: 1
 *         rule: $(aws::rule rule-example-waf)
 *     end
 *
 *     rule
 *         action
 *             type: "ALLOW"
 *         end
 *         priority: 2
 *         rule: $(aws::rate-rule rate-rule-example-waf)
 *     end
 * end
 */
@Type("waf-web-acl")
public class WebAclResource extends gyro.aws.waf.common.WebAclResource {
    private Set<ActivatedRuleResource> rule;

    /**
     * A set of activated rules specifying the connection between waf acl and rule.
     *
     * @subresource gyro.aws.waf.global.ActivatedRuleResource
     */
    @Updatable
    public Set<ActivatedRuleResource> getRule() {
        if (rule == null) {
            rule = new HashSet<>();
        }

        return rule;
    }

    public void setRule(Set<ActivatedRuleResource> rule) {
        this.rule = rule;

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
        ActivatedRuleResource activatedRuleResource = newSubresource(ActivatedRuleResource.class);
        activatedRuleResource.copyFrom(activatedRule);
        getRule().add(activatedRuleResource);
    }

    @Override
    protected void clearActivatedRules() {
        getRule().clear();
    }

    @Override
    protected List<Integer> getActivatedRulesPriority() {
        return getRule().stream()
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
