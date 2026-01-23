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
import software.amazon.awssdk.services.opensearch.model.LogPublishingOption;

public class OpenSearchLogPublishingOption extends Diffable implements Copyable<LogPublishingOption> {

    private String cloudWatchLogsLogGroupArn;
    private Boolean enabled;

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

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(LogPublishingOption model) {
        setEnabled(model.enabled());
        setCloudWatchLogsLogGroupArn(model.cloudWatchLogsLogGroupArn());
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
