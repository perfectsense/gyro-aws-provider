package gyro.aws.waf.regional;

import software.amazon.awssdk.services.waf.model.ActivatedRule;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;


public class ActivatedRuleResource extends gyro.aws.waf.common.ActivatedRuleResource {
    @Override
    protected void saveActivatedRule(ActivatedRule activatedRule, boolean isDelete) {
        WebAclResource parent = (WebAclResource) parent();

        if (!isDelete || parent.isActivatedRulePresent(activatedRule)) {

            WafRegionalClient client = getRegionalClient();

            client.updateWebACL(
                toUpdateWebAclRequest(parent, activatedRule, isDelete)
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
