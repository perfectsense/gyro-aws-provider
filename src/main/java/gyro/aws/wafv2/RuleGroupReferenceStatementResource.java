package gyro.aws.wafv2;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.wafv2.model.ExcludedRule;
import software.amazon.awssdk.services.wafv2.model.RuleGroupReferenceStatement;

public class RuleGroupReferenceStatementResource extends WafDiffable implements Copyable<RuleGroupReferenceStatement> {

    private RuleGroupResource ruleGroup;
    private Set<String> excludedRules;

    public RuleGroupResource getRuleGroup() {
        return ruleGroup;
    }

    public void setRuleGroup(RuleGroupResource ruleGroup) {
        this.ruleGroup = ruleGroup;
    }

    public Set<String> getExcludedRules() {
        if (excludedRules == null) {
            excludedRules = new HashSet<>();
        }

        return excludedRules;
    }

    public void setExcludedRules(Set<String> excludedRules) {
        this.excludedRules = excludedRules;
    }

    @Override
    public void copyFrom(RuleGroupReferenceStatement ruleGroupReferenceStatement) {
        getExcludedRules().clear();
        if (ruleGroupReferenceStatement.excludedRules() != null) {
            setExcludedRules(ruleGroupReferenceStatement.excludedRules()
                .stream()
                .map(ExcludedRule::name)
                .collect(Collectors.toSet()));
        }

        setRuleGroup(findById(RuleGroupResource.class, ruleGroupReferenceStatement.arn()));
        setHashCode(ruleGroupReferenceStatement.hashCode());
    }

    RuleGroupReferenceStatement toRuleGroupReferenceStatement() {
        RuleGroupReferenceStatement.Builder builder = RuleGroupReferenceStatement.builder()
            .arn(getRuleGroup().getArn());

        if (!getExcludedRules().isEmpty()) {
            builder = builder.excludedRules(getExcludedRules().stream()
                .map(o -> ExcludedRule.builder().name(o).build())
                .collect(Collectors.toList()));
        }

        return builder.build();
    }
}
