package gyro.aws.wafv2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.wafv2.Wafv2Client;
import software.amazon.awssdk.services.wafv2.model.ListRuleGroupsRequest;
import software.amazon.awssdk.services.wafv2.model.ListRuleGroupsResponse;
import software.amazon.awssdk.services.wafv2.model.ListWebAcLsRequest;
import software.amazon.awssdk.services.wafv2.model.ListWebAcLsResponse;
import software.amazon.awssdk.services.wafv2.model.RuleGroup;
import software.amazon.awssdk.services.wafv2.model.Scope;
import software.amazon.awssdk.services.wafv2.model.WafNonexistentItemException;
import software.amazon.awssdk.services.wafv2.model.WebACL;

@Type("waf-rule-group")
public class RuleGroupFinder extends AwsFinder<Wafv2Client, RuleGroup, RuleGroupResource> {

    private String id;
    private String name;
    private String scope;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    protected List<RuleGroup> findAllAws(Wafv2Client client) {
        List<RuleGroup> ruleGroups = new ArrayList<>();
        ListRuleGroupsResponse response;
        String marker = null;

        do {
            response = client.listRuleGroups(ListRuleGroupsRequest.builder()
                .scope(Scope.CLOUDFRONT)
                .nextMarker(marker)
                .build());

            marker = response.nextMarker();

            ruleGroups.addAll(response.ruleGroups()
                .stream()
                .map(o -> getRuleGroup(client, o.id(), o.name(), Scope.CLOUDFRONT.toString()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        } while (!ObjectUtils.isBlank(marker));

        marker = null;

        do {
            response = client.listRuleGroups(ListRuleGroupsRequest.builder()
                .scope(Scope.REGIONAL)
                .nextMarker(marker)
                .build());

            marker = response.nextMarker();

            ruleGroups.addAll(response.ruleGroups()
                .stream()
                .map(o -> getRuleGroup(client, o.id(), o.name(), Scope.REGIONAL.toString()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        } while (!ObjectUtils.isBlank(marker));

        return ruleGroups;
    }

    @Override
    protected List<RuleGroup> findAws(Wafv2Client client, Map<String, String> filters) {
        List<RuleGroup> ruleGroups = new ArrayList<>();

        RuleGroup ruleGroup = getRuleGroup(client, filters.get("id"), filters.get("name"), filters.get("scope"));

        if (ruleGroup != null) {
            ruleGroups.add(ruleGroup);
        }

        return ruleGroups;
    }

    private RuleGroup getRuleGroup(Wafv2Client client, String id, String name, String scope) {
        try {
            return client.getRuleGroup(r -> r.id(id).name(name).scope(scope)).ruleGroup();
        } catch (WafNonexistentItemException ex) {
            return null;
        }
    }
}
