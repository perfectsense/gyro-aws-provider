/*
 * Copyright 2021, Brightspot.
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
     * The name of a backup plan.
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
     * The list of rules, each of which specifies a scheduled task that is used to back up a selection of resources.
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
