/*
 * Copyright 2020, Brightspot.
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

package gyro.aws.apigatewayv2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.apigatewayv2.model.LoggingLevel;
import software.amazon.awssdk.services.apigatewayv2.model.RouteSettings;

public class ApiRouteSettings extends Diffable implements Copyable<RouteSettings> {

    private String key;
    private Boolean dataTraceEnabled;
    private Boolean detailedMetricsEnabled;
    private LoggingLevel loggingLevel;
    private Integer throttlingBurstLimit;
    private Double throttlingRateLimit;

    /**
     * The route key for which the configure the settings.
     */
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * When set to ``true``, the data trace logging is enabled for this route.
     */
    public Boolean getDataTraceEnabled() {
        return dataTraceEnabled;
    }

    public void setDataTraceEnabled(Boolean dataTraceEnabled) {
        this.dataTraceEnabled = dataTraceEnabled;
    }

    /**
     * When set to ``true``, the detailed metrics are enabled.
     */
    public Boolean getDetailedMetricsEnabled() {
        return detailedMetricsEnabled;
    }

    public void setDetailedMetricsEnabled(Boolean detailedMetricsEnabled) {
        this.detailedMetricsEnabled = detailedMetricsEnabled;
    }

    /**
     * The logging level for this route.
     */
    @ValidStrings({ "ERROR", "INFO", "OFF" })
    public LoggingLevel getLoggingLevel() {
        return loggingLevel;
    }

    public void setLoggingLevel(LoggingLevel loggingLevel) {
        this.loggingLevel = loggingLevel;
    }

    /**
     * The throttling burst limit of the route.
     */
    public Integer getThrottlingBurstLimit() {
        return throttlingBurstLimit;
    }

    public void setThrottlingBurstLimit(Integer throttlingBurstLimit) {
        this.throttlingBurstLimit = throttlingBurstLimit;
    }

    /**
     * The throttling rate limit of the route.
     */
    public Double getThrottlingRateLimit() {
        return throttlingRateLimit;
    }

    public void setThrottlingRateLimit(Double throttlingRateLimit) {
        this.throttlingRateLimit = throttlingRateLimit;
    }

    @Override
    public String primaryKey() {
        StringBuilder sb = new StringBuilder("Api Route Settings - ");

        if (getKey() != null) {
            sb.append("Key: ").append(getKey()).append(" ");
        }

        if (getDataTraceEnabled() != null) {
            sb.append("Data Trace Enabled: ").append(getDataTraceEnabled()).append(" ");
        }

        if (getDetailedMetricsEnabled() != null) {
            sb.append("Detailed Metrics Enabled: ").append(getDetailedMetricsEnabled()).append(" ");
        }

        if (getLoggingLevel() != null) {
            sb.append("Logging Level: ").append(getLoggingLevel()).append(" ");
        }

        if (getThrottlingBurstLimit() != null) {
            sb.append("Throttling Burst Limit: ").append(getThrottlingBurstLimit()).append(" ");
        }

        if (getThrottlingRateLimit() != null) {
            sb.append("Throttling Rate Limit: ").append(getThrottlingRateLimit());
        }

        return sb.toString();
    }

    @Override
    public void copyFrom(RouteSettings model) {
        setDataTraceEnabled(model.dataTraceEnabled());
        setDetailedMetricsEnabled(model.detailedMetricsEnabled());
        setLoggingLevel(model.loggingLevel());
        setThrottlingBurstLimit(model.throttlingBurstLimit());
        setThrottlingRateLimit(model.throttlingRateLimit());
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getKey() == null && getDataTraceEnabled() == null && getDetailedMetricsEnabled() == null
            && getLoggingLevel() == null && getThrottlingBurstLimit() == null && getThrottlingRateLimit() == null) {
            errors.add(new ValidationError(this, null,
                "At least one of 'key', 'data-trace-enabled', 'detailed-metrics-enabled', 'logging-level', "
                    + "'throttling-burst-limit' or 'throttling-rate-limit' has to be set."
            ));
        }

        return errors;
    }

    public RouteSettings toRouteSettings() {
        return RouteSettings.builder().dataTraceEnabled(getDataTraceEnabled())
            .detailedMetricsEnabled(getDetailedMetricsEnabled())
            .loggingLevel(getLoggingLevel())
            .throttlingBurstLimit(getThrottlingBurstLimit())
            .throttlingRateLimit(getThrottlingRateLimit())
            .build();
    }
}
