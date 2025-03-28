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

import java.util.Date;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.opensearch.model.AutoTuneMaintenanceSchedule;
import software.amazon.awssdk.services.opensearch.model.Duration;
import software.amazon.awssdk.services.opensearch.model.TimeUnit;

public class OpenSearchAutoTuneMaintenanceSchedule extends Diffable implements Copyable<AutoTuneMaintenanceSchedule> {

    private Long duration;
    private TimeUnit durationUnit;
    private String cronExpressionForRecurrence;
    private Date startAt;

    /**
     * The duration of the maintenance schedule.
     */
    @Required
    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    /**
     * The unit of the duration.
     */
    @Required
    @ValidStrings("HOURS")
    public TimeUnit getDurationUnit() {
        return durationUnit;
    }

    public void setDurationUnit(TimeUnit durationUnit) {
        this.durationUnit = durationUnit;
    }

    /**
     * The cron expression for the recurrence of the maintenance schedule.
     */
    @Updatable
    public String getCronExpressionForRecurrence() {
        return cronExpressionForRecurrence;
    }

    public void setCronExpressionForRecurrence(String cronExpressionForRecurrence) {
        this.cronExpressionForRecurrence = cronExpressionForRecurrence;
    }

    /**
     * The start time of the maintenance schedule.
     */
    @Required
    public Date getStartAt() {
        return startAt;
    }

    public void setStartAt(Date startAt) {
        this.startAt = startAt;
    }

    @Override
    public String primaryKey() {
        return String.format(
            "duration: %s, duration-unit: %s, start-at: %s",
            getDuration(),
            getDurationUnit(),
            getStartAt());
    }

    @Override
    public void copyFrom(AutoTuneMaintenanceSchedule model) {
        setDuration(model.duration().value());
        setDurationUnit(model.duration().unit());
        setCronExpressionForRecurrence(model.cronExpressionForRecurrence());
        setStartAt(Date.from(model.startAt()));
    }

    AutoTuneMaintenanceSchedule toAutoTuneMaintenanceSchedule() {
        return AutoTuneMaintenanceSchedule.builder()
            .duration(Duration.builder().value(getDuration()).unit(getDurationUnit()).build())
            .cronExpressionForRecurrence(getCronExpressionForRecurrence())
            .startAt(getStartAt().toInstant())
            .build();
    }
}
