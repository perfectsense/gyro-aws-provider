package gyro.aws.waf.global;

import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ActivatedRule;

public class ActivatedRuleResource extends gyro.aws.waf.common.ActivatedRuleResource {
    @Override
    protected void saveActivatedRule(ActivatedRule activatedRule, boolean isDelete) {
        WebAclResource parent = (WebAclResource) parent();

        if (!isDelete || parent.isActivatedRulePresent(activatedRule)) {

            WafClient client = getGlobalClient();

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
