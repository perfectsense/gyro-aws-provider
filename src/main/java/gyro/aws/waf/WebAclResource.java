package gyro.aws.waf;

import com.psddev.dari.util.ObjectUtils;
import gyro.core.GyroException;
import gyro.core.resource.Resource;
import gyro.core.resource.ResourceDiffProperty;
import gyro.core.resource.ResourceOutput;
import software.amazon.awssdk.services.waf.model.ActivatedRule;
import software.amazon.awssdk.services.waf.model.CreateWebAclRequest;
import software.amazon.awssdk.services.waf.model.CreateWebAclResponse;
import software.amazon.awssdk.services.waf.model.UpdateWebAclRequest;
import software.amazon.awssdk.services.waf.model.WafAction;
import software.amazon.awssdk.services.waf.model.WebACL;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class WebAclResource extends AbstractWafResource {
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
     * @subresource gyro.aws.waf.ActivatedRuleResource
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

    protected abstract WebACL getWebAcl();

    protected abstract void setActivatedRules(ActivatedRule activatedRule);

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
            setActivatedRules(activatedRule);
        }

        return true;
    }

    protected abstract CreateWebAclResponse doCreate(CreateWebAclRequest.Builder builder);

    @Override
    public void create() {
        CreateWebAclResponse response;

        CreateWebAclRequest.Builder builder = CreateWebAclRequest.builder()
            .name(getName())
            .metricName(getMetricName())
            .defaultAction(
                WafAction.builder()
                    .type(getDefaultAction())
                    .build()
            );

        response = doCreate(builder);

        WebACL webAcl = response.webACL();
        setArn(webAcl.webACLArn());
        setWebAclId(webAcl.webACLId());
    }

    protected abstract void doUpdate(UpdateWebAclRequest.Builder builder);

    @Override
    public void update(Resource current, Set<String> changedProperties) {
        UpdateWebAclRequest.Builder builder = UpdateWebAclRequest.builder()
            .webACLId(getWebAclId())
            .defaultAction(
                WafAction.builder()
                    .type(getDefaultAction())
                    .build()
            );

        doUpdate(builder);
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

    public ActivatedRule getActivatedRuleWithPriority(int priority) {
        WebACL webAcl = getWebAcl();

        if (webAcl != null) {
            return webAcl.rules().stream().filter(o -> o.priority() == priority).findFirst().orElse(null);
        }

        return null;
    }

    public boolean isActivatedRulePresent(ActivatedRule activatedRule) {
        WebACL webAcl = getWebAcl();

        if (webAcl != null) {
            return webAcl.rules().stream().anyMatch(o -> o.ruleId().equals(activatedRule.ruleId()) && o.typeAsString().equals(activatedRule.typeAsString()));
        }

        return false;
    }
}
