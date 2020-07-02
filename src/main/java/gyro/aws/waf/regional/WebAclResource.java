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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.elbv2.ApplicationLoadBalancerResource;
import gyro.aws.elbv2.LoadBalancerResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import software.amazon.awssdk.services.waf.model.ActivatedRule;
import software.amazon.awssdk.services.waf.model.CreateWebAclRequest;
import software.amazon.awssdk.services.waf.model.CreateWebAclResponse;
import software.amazon.awssdk.services.waf.model.GetWebAclResponse;
import software.amazon.awssdk.services.waf.model.ListResourcesForWebAclResponse;
import software.amazon.awssdk.services.waf.model.ResourceType;
import software.amazon.awssdk.services.waf.model.UpdateWebAclRequest;
import software.amazon.awssdk.services.waf.model.WebACL;
import software.amazon.awssdk.services.waf.regional.WafRegionalClient;

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
    private Set<ApplicationLoadBalancerResource> loadBalancers;

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

    /**
     * A set of Application Load Balancer that will be associated with the waf acl.
     */
    @Updatable
    public Set<ApplicationLoadBalancerResource> getLoadBalancers() {
        if (loadBalancers == null) {
            loadBalancers = new HashSet<>();
        }

        return loadBalancers;
    }

    public void setLoadBalancers(Set<ApplicationLoadBalancerResource> loadBalancers) {
        this.loadBalancers = loadBalancers;
    }

    @Override
    public void copyFrom(WebACL webAcl) {
        super.copyFrom(webAcl);

        // Load associated ALB's
        getLoadBalancers().clear();
        getAssociatedAlbArns(getRegionalClient()).forEach(r -> getLoadBalancers().add(findById(ApplicationLoadBalancerResource.class, r)));
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

        if (!getLoadBalancers().isEmpty()) {
            for (ApplicationLoadBalancerResource loadBalancer : getLoadBalancers()) {
                try {
                    client.associateWebACL(r -> r.webACLId(response.webACL().webACLId())
                        .resourceArn(loadBalancer.getArn()));
                } catch (Exception ex) {
                    throw new GyroException(String.format("Failed to associate loadbalancer %s", loadBalancer.getArn()));
                }
            }
        }

        return response;
    }

    @Override
    protected void doUpdate(UpdateWebAclRequest.Builder builder, Resource current, Set<String> changedProperties) {
        WafRegionalClient client = getRegionalClient();

        client.updateWebACL(builder.changeToken(client.getChangeToken().changeToken()).build());

        if (changedProperties.contains("load-balancers")) {

            WebAclResource aclResource = (WebAclResource) current;

            Set<String> currentAlbArns = aclResource.getLoadBalancers()
                .stream()
                .map(LoadBalancerResource::getArn)
                .collect(Collectors.toSet());

            Set<String> pendingAlbArns = getLoadBalancers()
                .stream()
                .map(LoadBalancerResource::getArn)
                .collect(Collectors.toSet());

            List<String> removeAlbArns = currentAlbArns.stream()
                .filter(o -> !pendingAlbArns.contains(o))
                .collect(Collectors.toList());

            if (!removeAlbArns.isEmpty()) {
                for (String arn : removeAlbArns) {
                    try {
                        client.disassociateWebACL(r -> r.resourceArn(arn));
                    } catch (Exception ex) {
                        // ignore
                    }
                }
            }

            List<String> addAlbArns = pendingAlbArns.stream()
                .filter(o -> !currentAlbArns.contains(o))
                .collect(Collectors.toList());

            if (!addAlbArns.isEmpty()) {
                for (String arn : addAlbArns) {
                    try {
                        client.associateWebACL(r -> r.webACLId(getWebAclId()).resourceArn(arn));
                    } catch (Exception ex) {
                        throw new GyroException(String.format("Failed to associate loadbalancer %s", arn));
                    }
                }
            }
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        WafRegionalClient client = getRegionalClient();

        // Remove associated ALb before deleting
        List<String> associatedAlbArns = getAssociatedAlbArns(client);

        if (!associatedAlbArns.isEmpty()) {
            for (String arn : associatedAlbArns) {
                try {
                    client.disassociateWebACL(r -> r.resourceArn(arn));
                } catch (Exception ex) {
                    // ignore
                }
            }
        }

        client.deleteWebACL(
            r -> r.changeToken(client.getChangeToken().changeToken())
                .webACLId(getWebAclId())
        );
    }

    private List<String> getAssociatedAlbArns(WafRegionalClient client) {
        List<String> arns = new ArrayList<>();

        ListResourcesForWebAclResponse response = client.listResourcesForWebACL(r -> r
            .resourceType(ResourceType.APPLICATION_LOAD_BALANCER)
            .webACLId(getWebAclId()));

        if (response.hasResourceArns()) {
            arns = response.resourceArns();
        }

        return arns;
    }
}
