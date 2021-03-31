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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;

public class AdvancedBackupSetting
    extends Diffable implements Copyable<software.amazon.awssdk.services.backup.model.AdvancedBackupSetting> {

    private Map<String, String> backupOptions;
    private String resourceType;

    /**
     * The backup option for a selected resource.
     */
    @Updatable
    public Map<String, String> getBackupOptions() {
        if (backupOptions == null) {
            backupOptions = new HashMap<>();
        }

        return backupOptions;
    }

    public void setBackupOptions(Map<String, String> backupOptions) {
        this.backupOptions = backupOptions;
    }

    /**
     * The object containing resource type and backup options.
     */
    @ValidStrings("EC2")
    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.backup.model.AdvancedBackupSetting model) {
        setBackupOptions(model.backupOptions());
        setResourceType(model.resourceType());
    }

    @Override
    public String primaryKey() {
        return String.format("Resource Type: %s, Options: (%s)", getResourceType(), getBackupOptions().toString());
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        ArrayList<ValidationError> errors = new ArrayList<>();

        for (Map.Entry<String, String> entry : getBackupOptions().entrySet()) {
            if (!entry.getKey().equals("WindowsVSS") ||
                !(entry.getValue().equals("enabled") || entry.getValue().equals("disabled"))) {
                errors.add(new ValidationError(this, "backup-options",
                    "The valid options for 'backup-options' are 'WindowsVSS : enabled' or 'WindowsVSS : disabled'."));
            }
        }

        return errors;
    }

    software.amazon.awssdk.services.backup.model.AdvancedBackupSetting toAdvancedBackupSetting() {
        return software.amazon.awssdk.services.backup.model.AdvancedBackupSetting.builder()
            .backupOptions(getBackupOptions()).resourceType(getResourceType()).build();
    }
}
