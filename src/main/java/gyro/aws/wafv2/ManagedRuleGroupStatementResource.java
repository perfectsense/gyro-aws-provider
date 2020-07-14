package gyro.aws.wafv2;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import software.amazon.awssdk.services.wafv2.model.ExcludedRule;
import software.amazon.awssdk.services.wafv2.model.ManagedRuleGroupStatement;

public class ManagedRuleGroupStatementResource extends WafDiffable implements Copyable<ManagedRuleGroupStatement> {

    private Set<String> excludedRules;
    private String name;
    private String vendorName;

    public Set<String> getExcludedRules() {
        if (excludedRules == null) {
            excludedRules = new HashSet<>();
        }

        return excludedRules;
    }

    public void setExcludedRules(Set<String> excludedRules) {
        this.excludedRules = excludedRules;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    @Override
    public void copyFrom(ManagedRuleGroupStatement managedRuleGroupStatement) {
        getExcludedRules().clear();
        if (managedRuleGroupStatement.excludedRules() != null) {
            setExcludedRules(managedRuleGroupStatement.excludedRules()
                .stream()
                .map(ExcludedRule::name)
                .collect(Collectors.toSet()));
        }

        setName(managedRuleGroupStatement.name());
        setVendorName(managedRuleGroupStatement.vendorName());
        setHashCode(managedRuleGroupStatement.hashCode());
    }

    ManagedRuleGroupStatement toManagedRuleGroupStatement() {
        ManagedRuleGroupStatement.Builder builder = ManagedRuleGroupStatement.builder()
            .name(getName())
            .vendorName(getVendorName());

        if (!getExcludedRules().isEmpty()) {
            builder = builder.excludedRules(getExcludedRules().stream()
                .map(o -> ExcludedRule.builder().name(o).build())
                .collect(Collectors.toList()));
        }

        return builder.build();
    }
}
