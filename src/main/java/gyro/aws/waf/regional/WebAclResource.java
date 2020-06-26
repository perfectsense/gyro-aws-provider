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

package gyro.aws.waf.regional;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.elbv2.ApplicationLoadBalancerResource;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.waf.model.ActivatedRule;
import software.amazon.awssdk.services.waf.model.CreateWebAclRequest;
import software.amazon.awssdk.services.waf.model.CreateWebAclResponse;
import software.amazon.awssdk.services.waf.model.GetWebAclResponse;
import software.amazon.awssdk.services.waf.model.UpdateWebAclRequest;
import software.amazon.awssdk.services.waf.model.WebACL;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates a regional waf acl.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::waf-web-acl-regional waf-web-acl-example
 *         name: "waf-web-acl-example"
 *         metric-name: "wafAclExample"
 *         default-action
 *             type: "ALLOW"
 *         end
 *
 *         rule
 *             action
 *                 type: "ALLOW"
 *             end
 *             priority: 1
 *             rule: $(aws::waf-rule-regional rule-example-waf)
 *         end
 *
 *         rule
 *             action
 *                 type: "ALLOW"
 *             end
 *             priority: 2
 *             rule: $(aws::waf-rate-rule-regional rate-rule-example-waf)
 *         end
 *     end
 */
@Type("waf-web-acl-regional")
public class WebAclResource extends gyro.aws.waf.common.WebAclResource {
    private Set<ActivatedRuleResource> rule;
    private ApplicationLoadBalancerResource loadBalancer;

    /**
     * A set of activated rules specifying the connection between waf acl and rule.
     *
     * @subresource gyro.aws.waf.regional.ActivatedRuleResource
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

    @Updatable
    public ApplicationLoadBalancerResource getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(ApplicationLoadBalancerResource loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    @Override
    protected WebACL getWebAcl() {
        if (ObjectUtils.isBlank(getWebAclId())) {
            return null;
        }

        GetWebAclResponse response = getRegionalClient().getWebACL(
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
        WafRegionalClient client = getRegionalClient();

        CreateWebAclResponse response = client.createWebACL(builder.changeToken(client.getChangeToken().changeToken())
            .build());

        try {
            if (getLoadBalancer() != null) {
                client.associateWebACL(r -> r.webACLId(response.webACL().webACLId())
                    .resourceArn(getLoadBalancer().getArn()));
            }
        } catch (Exception ex) {
            System.out.println("Failed to associate Webacl to ALB");
        }

        return response;
    }

    @Override
    protected void doUpdate(UpdateWebAclRequest.Builder builder) {
        WafRegionalClient client = getRegionalClient();

        client.updateWebACL(builder.changeToken(client.getChangeToken().changeToken()).build());

        if (getLoadBalancer() != null) {
            client.associateWebACL(r -> r.webACLId(getWebAclId())
                .resourceArn(getLoadBalancer().getArn()));
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        WafRegionalClient client = getRegionalClient();

        client.deleteWebACL(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .webACLId(getWebAclId())
        );
    }
}
