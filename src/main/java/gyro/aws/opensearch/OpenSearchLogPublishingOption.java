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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.resource.Id;
import software.amazon.awssdk.services.opensearch.model.LogPublishingOption;
import software.amazon.awssdk.services.opensearch.model.LogType;

public class OpenSearchLogPublishingOption extends Diffable implements Copyable<LogPublishingOption> {

    private String name;
    private String cloudWatchLogsLogGroupArn;
    private Boolean enabled;

    /**
     * The log type name (e.g., INDEX_SLOW_LOGS, SEARCH_SLOW_LOGS, ES_APPLICATION_LOGS, AUDIT_LOGS).
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * ARN of the CloudWatch log group for publishing logs.
     */
    @Updatable
    public String getCloudWatchLogsLogGroupArn() {
        return cloudWatchLogsLogGroupArn;
    }

    public void setCloudWatchLogsLogGroupArn(String cloudWatchLogsLogGroupArn) {
        this.cloudWatchLogsLogGroupArn = cloudWatchLogsLogGroupArn;
    }

    /**
     * Whether log publishing is enabled
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
        return getName();
    }

    @Override
    public void copyFrom(LogPublishingOption model) {
        setEnabled(model.enabled());
        if (Boolean.TRUE.equals(model.enabled())) {
            setCloudWatchLogsLogGroupArn(model.cloudWatchLogsLogGroupArn());
        }
    }

    public void copyFrom(LogType logType, LogPublishingOption model) {
        setName(logType.toString());
        copyFrom(model);
    }

    public LogPublishingOption toLogPublishingOption() {
        if (!getEnabled()) {
            return LogPublishingOption.builder().enabled(false).build();
        }

        if (getCloudWatchLogsLogGroupArn() == null || getCloudWatchLogsLogGroupArn().isEmpty()) {
            throw new IllegalStateException("'cloud-watch-logs-log-group-arn' is required when 'enabled' is set to true");
        }

        return LogPublishingOption.builder()
            .enabled(true)
            .cloudWatchLogsLogGroupArn(getCloudWatchLogsLogGroupArn())
            .build();
    }
}
