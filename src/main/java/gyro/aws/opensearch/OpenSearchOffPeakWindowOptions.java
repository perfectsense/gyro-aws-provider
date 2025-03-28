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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.DependsOn;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.opensearch.model.OffPeakWindow;
import software.amazon.awssdk.services.opensearch.model.OffPeakWindowOptions;
import software.amazon.awssdk.services.opensearch.model.WindowStartTime;

public class OpenSearchOffPeakWindowOptions extends Diffable implements Copyable<OffPeakWindowOptions> {

    private Boolean offPeakEnabled;
    private Long offPeakWindowHour;
    private Long offPeakWindowMinutes;

    /**
     * When set to `true`, the off-peak window is enabled.
     */
    @Required
    @Updatable
    public Boolean getOffPeakEnabled() {
        return offPeakEnabled;
    }

    public void setOffPeakEnabled(Boolean offPeakEnabled) {
        this.offPeakEnabled = offPeakEnabled;
    }

    /**
     * The hour at which the off-peak window starts.
     */
    @Updatable
    @DependsOn("off-peak-window-minutes")
    public Long getOffPeakWindowHour() {
        return offPeakWindowHour;
    }

    public void setOffPeakWindowHour(Long offPeakWindowHour) {
        this.offPeakWindowHour = offPeakWindowHour;
    }

    @Updatable
    @DependsOn("off-peak-window-hour")
    public Long getOffPeakWindowMinutes() {
        return offPeakWindowMinutes;
    }

    public void setOffPeakWindowMinutes(Long offPeakWindowMinutes) {
        this.offPeakWindowMinutes = offPeakWindowMinutes;
    }

    @Override
    public void copyFrom(OffPeakWindowOptions model) {
        setOffPeakEnabled(model.enabled());
        setOffPeakWindowHour(null);
        setOffPeakWindowMinutes(null);
        if (model.offPeakWindow() != null) {
            setOffPeakWindowHour(model.offPeakWindow().windowStartTime().hours());
            setOffPeakWindowMinutes(model.offPeakWindow().windowStartTime().minutes());
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    OffPeakWindowOptions toOffPeakWindowOptions() {
        OffPeakWindowOptions.Builder enabled = OffPeakWindowOptions.builder()
            .enabled(getOffPeakEnabled());

        if (Boolean.TRUE.equals(getOffPeakEnabled())) {
            enabled = enabled.offPeakWindow(OffPeakWindow.builder()
                .windowStartTime(WindowStartTime.builder()
                    .hours(getOffPeakWindowHour())
                    .minutes(getOffPeakWindowMinutes())
                    .build())
                .build());
        }

        return enabled.build();
    }
}
