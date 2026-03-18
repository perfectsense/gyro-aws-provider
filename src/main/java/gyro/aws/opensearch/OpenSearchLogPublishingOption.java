/*
 * Copyright 2026, Brightspot.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.aws.Copyable;
import gyro.aws.cloudwatch.LogGroupResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.resource.Id;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.opensearch.model.LogPublishingOption;
import software.amazon.awssdk.services.opensearch.model.LogType;

public class OpenSearchLogPublishingOption extends Diffable implements Copyable<LogPublishingOption> {

    private LogType name;
    private LogGroupResource cloudWatchLogsLogGroup;
    private Boolean enabled;

    /**
     * The log type name.
     */
    @ValidStrings({"INDEX_SLOW_LOGS", "SEARCH_SLOW_LOGS", "ES_APPLICATION_LOGS", "AUDIT_LOGS"})
    public LogType getName() {
        return name;
    }

    public void setName(LogType name) {
        this.name = name;
    }

    /**
     * The CloudWatch log group for publishing logs.
     *
     * @subresource gyro.aws.cloudwatch.LogGroupResource
     */
    @Updatable
    public LogGroupResource getCloudWatchLogsLogGroup() {
        return cloudWatchLogsLogGroup;
    }

    public void setCloudWatchLogsLogGroup(LogGroupResource cloudWatchLogsLogGroup) {
        this.cloudWatchLogsLogGroup = cloudWatchLogsLogGroup;
    }

    /**
     *  When set to ``true``, log publishing is enabled
     */
    @Updatable
    public Boolean getEnabled() {
        return enabled != null ? enabled : false;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Id
    @Override
    public String primaryKey() {
        return getName().toString();
    }

    @Override
    public void copyFrom(LogPublishingOption model) {
        setEnabled(model.enabled());
        if (Boolean.TRUE.equals(model.enabled())) {
            setCloudWatchLogsLogGroup(findById(LogGroupResource.class, model.cloudWatchLogsLogGroupArn()));
        }
    }

    public void copyFrom(LogType logType, LogPublishingOption model) {
        setName(logType);
        copyFrom(model);
    }

    public LogPublishingOption toLogPublishingOption() {
        LogPublishingOption.Builder builder = LogPublishingOption.builder().enabled(getEnabled());

        if (getEnabled()) {
            builder.cloudWatchLogsLogGroupArn(getCloudWatchLogsLogGroup().getLogGroupArn());
        }

        return builder.build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getEnabled() && getCloudWatchLogsLogGroup() == null) {
            errors.add(new ValidationError(this, null, "'cloud-watch-logs-log-group' is required when 'enabled' is set to true."));
        }

        return errors;
    }
}
