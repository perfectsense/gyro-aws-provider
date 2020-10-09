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

package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.codebuild.model.CloudWatchLogsConfig;

public class CodebuildCloudWatchLogsConfig extends Diffable implements Copyable<CloudWatchLogsConfig> {

    private String status;
    private String groupName;
    private String streamName;

    /**
     * The status configuration for the cloud watch logs for a build project.
     */
    @Updatable
    @Required
    @ValidStrings({ "ENABLED", "DISABLED" })
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * The group name of the cloud watch logs.
     */
    @Updatable
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * The prefix of the stream name of the cloud watch Logs
     */
    @Updatable
    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    @Override
    public void copyFrom(CloudWatchLogsConfig model) {
        setStatus(model.statusAsString());
        setGroupName(model.groupName());
        setStreamName(model.streamName());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public CloudWatchLogsConfig toCloudWatchLogsConfig() {
        return CloudWatchLogsConfig.builder()
            .groupName(getGroupName())
            .status(getStatus())
            .streamName(getStreamName())
            .build();
    }
}
