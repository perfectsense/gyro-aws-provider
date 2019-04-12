package gyro.aws.waf;

import gyro.aws.AwsResource;
import gyro.core.GyroException;
import gyro.core.diff.ResourceDiffProperty;
import gyro.core.diff.ResourceName;
import gyro.core.diff.ResourceOutput;
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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a waf acl.
 *
 * Example
 * -------
 *
 * .. code-block:: beam
 *
 *     aws::waf-acl waf-acl-example
 *         name: "waf-acl-example"
 *         metric-name: "wafAclExample"
 *         default-action: "ALLOW"
 *
 *         activated-rule
 *             action: "ALLOW"
 *             type: "REGULAR"
 *             priority: 1
 *             rule-id: $(aws::rule rule-example | rule-id)
 *         end
 *     end
 */
@ResourceName("waf-acl")
public class WebAclResource extends AwsResource {
    private String name;
    private String metricName;
    private String defaultAction;
    private String webAclId;
    private String arn;
    private List<ActivatedRuleResource> activatedRule;

    /**
     * The name of the waf acl. (Required)
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The metric name of the waf acl. Can only contain letters and numbers. (Required)
     */
    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    /**
     * The default action for the waf acl. valid values ```ALLOW``` or ```BLOCK```. (Required)
     */
    @ResourceDiffProperty(updatable = true, nullable = true)
    public String getDefaultAction() {
        return defaultAction != null ? defaultAction.toUpperCase() : null;
    }

    public void setDefaultAction(String defaultAction) {
        this.defaultAction = defaultAction;
    }

    @ResourceOutput
    public String getWebAclId() {
        return webAclId;
    }

    public void setWebAclId(String webAclId) {
        this.webAclId = webAclId;
    }

    @ResourceOutput
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    /**
     * A list of activated rules specifying the connection between waf acl and rule.
     *
     * @subresources beam.aws.waf.ActivatedRuleResource
     */
    @ResourceDiffProperty(nullable = true, subresource = true)
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
    public boolean refresh() {
        WebACL webAcl = getWebAcl();

        if (webAcl == null) {
            return false;
        }

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
    
    private void validateActivatedRule() {
        List<Integer> priorityList = getActivatedRule().stream()
            .sorted(Comparator.comparing(ActivatedRuleResource::getPriority))
            .map(ActivatedRuleResource::getPriority).collect(Collectors.toList());

        boolean invalidPriority = false;
        int start = 1;

        for (int priority: priorityList) {
            if (priority != start || start > 10) {
                invalidPriority = true;
            }
            start++;
        }

        if (invalidPriority) {
            throw new GyroException("Activated Rule priority exception. Priority value starts from 1 to 10 without skipping any number.");
        }
    }

    private WebACL getWebAcl() {
        if (ObjectUtils.isBlank(getWebAclId())) {
            return null;
        }

        WafClient client = createClient(WafClient.class, Region.AWS_GLOBAL.toString(), null);

        GetWebAclResponse response = client.getWebACL(
            r -> r.webACLId(getWebAclId())
        );

        return response.webACL();
    }

    ActivatedRule getActivatedRuleWithPriority(int priority) {
        WebACL webAcl = getWebAcl();

        if (webAcl != null) {
            return webAcl.rules().stream().filter(o -> o.priority() == priority).findFirst().orElse(null);
        }

        return null;
    }

    boolean isActivatedRulePresent(ActivatedRule activatedRule) {
        WebACL webAcl = getWebAcl();

        if (webAcl != null) {
            return webAcl.rules().stream().anyMatch(o -> o.ruleId().equals(activatedRule.ruleId()) && o.typeAsString().equals(activatedRule.typeAsString()));
        }

        return false;
    }
}
