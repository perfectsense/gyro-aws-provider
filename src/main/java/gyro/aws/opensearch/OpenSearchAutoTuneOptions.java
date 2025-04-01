/*
 * Copyright 2024, Brightspot.
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

package gyro.aws.opensearch;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.opensearch.model.AutoTuneDesiredState;
import software.amazon.awssdk.services.opensearch.model.AutoTuneOptions;
import software.amazon.awssdk.services.opensearch.model.AutoTuneOptionsInput;
import software.amazon.awssdk.services.opensearch.model.RollbackOnDisable;

public class OpenSearchAutoTuneOptions extends Diffable implements Copyable<AutoTuneOptions> {

    private AutoTuneDesiredState desiredState;
    private RollbackOnDisable rollbackOnDisable;
    private Set<OpenSearchAutoTuneMaintenanceSchedule> maintenanceSchedules;
    private Boolean useOffPeakWindow;

    /**
     * The desired state of the Auto-Tune options.
     */
    @Required
    @Updatable
    @ValidStrings({ "ENABLED", "DISABLED" })
    public AutoTuneDesiredState getDesiredState() {
        return desiredState;
    }

    public void setDesiredState(AutoTuneDesiredState desiredState) {
        this.desiredState = desiredState;
    }

    /**
     * The rollback behavior when Auto-Tune is disabled.
     */
    @Updatable
    @ValidStrings({ "DEFAULT_ROLLBACK", "NO_ROLLBACK" })
    public RollbackOnDisable getRollbackOnDisable() {
        return rollbackOnDisable;
    }

    public void setRollbackOnDisable(RollbackOnDisable rollbackOnDisable) {
        this.rollbackOnDisable = rollbackOnDisable;
    }

    /**
     * The maintenance schedules for the Auto-Tune options.
     *
     * @subresource gyro.aws.opensearch.OpenSearchAutoTuneMaintenanceSchedule
     */
    @Updatable
    public Set<OpenSearchAutoTuneMaintenanceSchedule> getMaintenanceSchedules() {
        if (maintenanceSchedules == null) {
            maintenanceSchedules = new HashSet<>();
        }

        return maintenanceSchedules;
    }

    public void setMaintenanceSchedules(Set<OpenSearchAutoTuneMaintenanceSchedule> maintenanceSchedules) {
        this.maintenanceSchedules = maintenanceSchedules;
    }

    /**
     * When set to `true`, Auto-Tune will only run during the off-peak window.
     */
    @Required
    @Updatable
    public Boolean getUseOffPeakWindow() {
        return useOffPeakWindow;
    }

    public void setUseOffPeakWindow(Boolean useOffPeakWindow) {
        this.useOffPeakWindow = useOffPeakWindow;
    }

    @Override
    public void copyFrom(AutoTuneOptions model) {
        setDesiredState(model.desiredState());
        setRollbackOnDisable(model.rollbackOnDisable());
        setUseOffPeakWindow(model.useOffPeakWindow());

        getMaintenanceSchedules().clear();
        if (model.maintenanceSchedules() != null) {
            model.maintenanceSchedules().forEach(maintenanceSchedule -> {
                OpenSearchAutoTuneMaintenanceSchedule autoTuneMaintenanceSchedule = newSubresource(
                    OpenSearchAutoTuneMaintenanceSchedule.class);
                autoTuneMaintenanceSchedule.copyFrom(maintenanceSchedule);
                getMaintenanceSchedules().add(autoTuneMaintenanceSchedule);
            });
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    AutoTuneOptions toAutoTuneOptions() {
        AutoTuneOptions.Builder builder = AutoTuneOptions.builder()
            .desiredState(getDesiredState())
            .rollbackOnDisable(getRollbackOnDisable())
            .useOffPeakWindow(getUseOffPeakWindow());

        if (!getMaintenanceSchedules().isEmpty()) {
            builder.maintenanceSchedules(getMaintenanceSchedules().stream()
                .map(OpenSearchAutoTuneMaintenanceSchedule::toAutoTuneMaintenanceSchedule)
                .collect(Collectors.toList()));
        }

        return builder.build();
    }

    AutoTuneOptionsInput toAutoTuneOptionsInput() {
        AutoTuneOptionsInput.Builder builder = AutoTuneOptionsInput.builder()
            .desiredState(getDesiredState())
            .useOffPeakWindow(getUseOffPeakWindow());

        if (!getMaintenanceSchedules().isEmpty()) {
            builder.maintenanceSchedules(getMaintenanceSchedules().stream()
                .map(OpenSearchAutoTuneMaintenanceSchedule::toAutoTuneMaintenanceSchedule)
                .collect(Collectors.toList()));
        }

        return builder.build();
    }
}
