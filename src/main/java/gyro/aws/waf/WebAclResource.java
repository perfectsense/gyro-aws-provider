package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import gyro.lang.Resource;
import com.psddev.dari.util.ObjectUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.waf.WafClient;
import software.amazon.awssdk.services.waf.model.ActivatedRule;
import software.amazon.awssdk.services.waf.model.CreateWebAclResponse;
import software.amazon.awssdk.services.waf.model.GetWebAclResponse;
import software.amazon.awssdk.services.waf.model.WafAction;
import software.amazon.awssdk.services.waf.model.WebACL;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ResourceName("waf-acl")
public class WebAclResource extends AwsResource {
    private String name;
    private String metricName;
    private String defaultAction;
    private String webAclId;
    private String arn;
    private List<ActivatedRuleResource> activatedRule;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    @ResourceDiffProperty(updatable = true, nullable = true)
    public String getDefaultAction() {
        return defaultAction;
    }

    public void setDefaultAction(String defaultAction) {
        this.defaultAction = defaultAction;
    }

    public String getWebAclId() {
        return webAclId;
    }

    public void setWebAclId(String webAclId) {
        this.webAclId = webAclId;
    }

    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @ResourceDiffProperty(nullable = true, subresource = true)
    public List<ActivatedRuleResource> getActivatedRule() {
        if (activatedRule == null) {
            activatedRule = new ArrayList<>();
        }

        return activatedRule;
    }

    public void setActivatedRule(List<ActivatedRuleResource> activatedRule) {
        this.activatedRule = activatedRule;
    }

    @Override
    public boolean refresh() {
        if (ObjectUtils.isBlank(getWebAclId())) {
            return false;
        }

        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        GetWebAclResponse response = client.getWebACL(
            r -> r.webACLId(getWebAclId())
        );

        WebACL webAcl = response.webACL();
        setArn(webAcl.webACLArn());
        setDefaultAction(webAcl.defaultAction().typeAsString());
        setMetricName(webAcl.metricName());
        setName(webAcl.name());

        getActivatedRule().clear();
        for (ActivatedRule activatedRule : webAcl.rules()) {
            ActivatedRuleResource activatedRuleResource = new ActivatedRuleResource(activatedRule);
            activatedRuleResource.parent(this);
            getActivatedRule().add(activatedRuleResource);
        }

        return true;
    }

    @Override
    public void create() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);
        CreateWebAclResponse response = client.createWebACL(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .name(getName())
                .metricName(getMetricName())
                .defaultAction(WafAction.builder().type(getDefaultAction()).build())
        );

        WebACL webAcl = response.webACL();
        setArn(webAcl.webACLArn());
        setWebAclId(webAcl.webACLId());
    }

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        client.updateWebACL(
            r -> r.webACLId(getWebAclId())
                .changeToken(client.getChangeToken().changeToken())
                .defaultAction(WafAction.builder().type(getDefaultAction()).build())
        );
    }

    @Override
    public void delete() {
        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        client.deleteWebACL(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .webACLId(getWebAclId())
        );
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append("waf acl");

        if (!ObjectUtils.isBlank(getName())) {
            sb.append(" - ").append(getName());
        }

        if (!ObjectUtils.isBlank(getWebAclId())) {
            sb.append(" - ").append(getWebAclId());
        }

        return sb.toString();
    }
}
