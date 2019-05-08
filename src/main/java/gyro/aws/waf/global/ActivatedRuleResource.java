package gyro.aws.waf.global;

import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ActivatedRule;
import software.amazon.awssdk.services.waf.model.ExcludedRule;

import java.util.stream.Collectors;

//@ResourceName(parent = "waf-acl", value = "activated-rule")
public class ActivatedRuleResource extends gyro.aws.waf.common.ActivatedRuleResource {
    public ActivatedRuleResource() {

    }

    public ActivatedRuleResource(ActivatedRule activatedRule) {
        setAction(activatedRule.action().typeAsString());
        setPriority(activatedRule.priority());
        setRuleId(activatedRule.ruleId());
        setType(activatedRule.typeAsString());
        setExcludedRules(activatedRule.excludedRules().stream().map(ExcludedRule::ruleId).collect(Collectors.toList()));
    }

    @Override
    protected void saveActivatedRule(boolean isDelete) {
        WebAclResource parent = (WebAclResource) parent();

        ActivatedRule activatedRule = parent.getActivatedRuleWithPriority(getPriority());

        if (!isDelete || parent.isActivatedRulePresent(activatedRule)) {

            WafClient client = getGlobalClient();

            client.updateWebACL(
                getUpdateWebAclRequest(parent, activatedRule, isDelete)
                    .changeToken(client.getChangeToken().changeToken())
                    .build()
            );
        }
    }

    @Override
    protected void handlePriority() {
        WebAclResource parent = (WebAclResource) parent();

        ActivatedRule activatedRule = parent.getActivatedRuleWithPriority(getPriority());
        if (activatedRule != null) {
            //delete conflicting activated rule with same priority
            saveActivatedRule(true);
        }
    }
}
