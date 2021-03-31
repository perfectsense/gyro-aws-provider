package gyro.aws.backup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;

public class BackupPlan extends Diffable
    implements Copyable<software.amazon.awssdk.services.backup.model.BackupPlan> {

    private List<AdvancedBackupSetting> advancedBackupSettings;
    private String name;
    private List<BackupRule> rule;

    /**
     * The list of backup options for each resource type.
     */
    @Updatable
    public List<AdvancedBackupSetting> getAdvancedBackupSettings() {
        if (advancedBackupSettings == null) {
            advancedBackupSettings = new ArrayList<>();
        }

        return advancedBackupSettings;
    }

    public void setAdvancedBackupSettings(List<AdvancedBackupSetting> advancedBackupSettings) {
        this.advancedBackupSettings = advancedBackupSettings;
    }

    /**
     * The display name of a backup plan.
     */
    @Required
    @Updatable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The array of rules, each of which specifies a scheduled task that is used to back up a selection of resources.
     */
    @Updatable
    @Required
    public List<BackupRule> getRule() {
        if (rule == null) {
            rule = new ArrayList<>();
        }

        return rule;
    }

    public void setRule(List<BackupRule> rule) {
        this.rule = rule;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.backup.model.BackupPlan model) {
        setName(model.backupPlanName());

        setRule(null);
        if (model.hasRules()) {
            setRule(model.rules().stream().map(r -> {
                BackupRule backupRule = newSubresource(BackupRule.class);
                backupRule.copyFrom(r);
                return backupRule;
            }).collect(Collectors.toList()));
        }

        if (model.hasAdvancedBackupSettings()) {
            setAdvancedBackupSettings(model.advancedBackupSettings().stream().map(s -> {
                AdvancedBackupSetting setting = newSubresource(AdvancedBackupSetting.class);
                setting.copyFrom(s);
                return setting;
            }).collect(Collectors.toList()));
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    software.amazon.awssdk.services.backup.model.BackupPlanInput toBackupPlanInput() {
        return software.amazon.awssdk.services.backup.model.BackupPlanInput.builder()
            .advancedBackupSettings(getAdvancedBackupSettings().stream()
                .map(AdvancedBackupSetting::toAdvancedBackupSetting).collect(Collectors.toList()))
            .backupPlanName(getName())
            .rules(getRule().stream().map(BackupRule::toBackupRuleInput).collect(Collectors.toList()))
            .build();
    }
}
