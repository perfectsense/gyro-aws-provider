/*
 * Copyright 2020, Perfect Sense, Inc.
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

package gyro.aws.wafv2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.wafv2.model.VisibilityConfig;

public class VisibilityConfigResource extends Diffable implements Copyable<VisibilityConfig> {

    private String metricName;
    private Boolean cloudWatchMetricsEnabled;
    private Boolean sampledRequestsEnabled;

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public Boolean getCloudWatchMetricsEnabled() {
        if (cloudWatchMetricsEnabled == null) {
            cloudWatchMetricsEnabled = false;
        }

        return cloudWatchMetricsEnabled;
    }

    public void setCloudWatchMetricsEnabled(Boolean cloudWatchMetricsEnabled) {
        this.cloudWatchMetricsEnabled = cloudWatchMetricsEnabled;
    }

    public Boolean getSampledRequestsEnabled() {
        if (sampledRequestsEnabled == null) {
            sampledRequestsEnabled = false;
        }

        return sampledRequestsEnabled;
    }

    public void setSampledRequestsEnabled(Boolean sampledRequestsEnabled) {
        this.sampledRequestsEnabled = sampledRequestsEnabled;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(VisibilityConfig config) {
        setMetricName(config.metricName());
        setCloudWatchMetricsEnabled(config.cloudWatchMetricsEnabled());
        setSampledRequestsEnabled(config.sampledRequestsEnabled());
    }

    VisibilityConfig toVisibilityConfig() {
        return VisibilityConfig.builder()
            .metricName(getMetricName())
            .cloudWatchMetricsEnabled(getCloudWatchMetricsEnabled())
            .sampledRequestsEnabled(getSampledRequestsEnabled())
            .build();
    }
}
