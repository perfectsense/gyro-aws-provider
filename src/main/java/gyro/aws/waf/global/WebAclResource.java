package gyro.aws.waf.global;

import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ActivatedRule;
import software.amazon.awssdk.services.waf.model.CreateWebAclRequest;
import software.amazon.awssdk.services.waf.model.CreateWebAclResponse;
import software.amazon.awssdk.services.waf.model.GetWebAclResponse;
import software.amazon.awssdk.services.waf.model.UpdateWebAclRequest;
import software.amazon.awssdk.services.waf.model.WebACL;

//@ResourceName("waf-acl")
public class WebAclResource extends gyro.aws.waf.common.WebAclResource {

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
        activatedRuleResource.parent(this);
        getActivatedRule().add(activatedRuleResource);
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
