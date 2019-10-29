/*
 * Copyright 2019, Perfect Sense, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gyro.aws.waf.common;

import gyro.aws.Copyable;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.resource.Id;
import gyro.core.resource.Resource;
import gyro.core.resource.Output;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.waf.model.ActivatedRule;
import software.amazon.awssdk.services.waf.model.CreateWebAclRequest;
import software.amazon.awssdk.services.waf.model.CreateWebAclResponse;
import software.amazon.awssdk.services.waf.model.UpdateWebAclRequest;
import software.amazon.awssdk.services.waf.model.WebACL;

import java.util.List;
import java.util.Set;

public abstract class WebAclResource extends AbstractWafResource implements Copyable<WebACL> {
    private String name;
    private String metricName;
    private WafAction defaultAction;
    private String webAclId;
    private String arn;

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
     * The default action for the waf acl. (Required)
     */
    @Updatable
    public WafAction getDefaultAction() {
        return defaultAction;
    }

    public void setDefaultAction(WafAction defaultAction) {
        this.defaultAction = defaultAction;
    }

    @Id
    @Output
    public String getWebAclId() {
        return webAclId;
    }

    public void setWebAclId(String webAclId) {
        this.webAclId = webAclId;
    }

    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    protected abstract WebACL getWebAcl();

    protected abstract void setActivatedRules(ActivatedRule activatedRule);

    protected abstract void clearActivatedRules();

    protected abstract List<Integer> getActivatedRulesPriority();

    protected abstract CreateWebAclResponse doCreate(CreateWebAclRequest.Builder builder);

    protected abstract void doUpdate(UpdateWebAclRequest.Builder builder);

    @Override
    public void copyFrom(WebACL webAcl) {
        setWebAclId(webAcl.webACLId());
        setArn(webAcl.webACLArn());
        setMetricName(webAcl.metricName());
        setName(webAcl.name());

        WafAction action = newSubresource(WafAction.class);
        action.copyFrom(webAcl.defaultAction());
        setDefaultAction(action);

        clearActivatedRules();
        for (ActivatedRule activatedRule : webAcl.rules()) {
            setActivatedRules(activatedRule);
        }
    }

    @Override
    public boolean refresh() {
        WebACL webAcl = getWebAcl();

        if (webAcl == null) {
            return false;
        }

        copyFrom(webAcl);

        return true;
    }

    @Override
    public void create(GyroUI ui, State state) {
        CreateWebAclResponse response;

        CreateWebAclRequest.Builder builder = CreateWebAclRequest.builder()
            .name(getName())
            .metricName(getMetricName())
            .defaultAction(getDefaultAction().toWafAction());

        response = doCreate(builder);

        WebACL webAcl = response.webACL();
        setArn(webAcl.webACLArn());
        setWebAclId(webAcl.webACLId());
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedProperties) {
        UpdateWebAclRequest.Builder builder = UpdateWebAclRequest.builder()
            .webACLId(getWebAclId())
            .defaultAction(getDefaultAction().toWafAction());

        doUpdate(builder);
    }

    protected void validateActivatedRule() {
        boolean invalidPriority = false;
        int start = 1;

        for (int priority: getActivatedRulesPriority()) {
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
