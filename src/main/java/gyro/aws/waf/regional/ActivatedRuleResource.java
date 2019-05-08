package gyro.aws.waf.regional;

import software.amazon.awssdk.services.waf.model.ActivatedRule;
import software.amazon.awssdk.services.waf.model.ExcludedRule;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.stream.Collectors;

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
    protected void saveActivatedRule(ActivatedRule activatedRule, boolean isDelete) {
        WebAclResource parent = (WebAclResource) parent();

        if (!isDelete || parent.isActivatedRulePresent(activatedRule)) {

            WafRegionalClient client = getRegionalClient();

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
            saveActivatedRule(activatedRule, true);
        }
    }
}
