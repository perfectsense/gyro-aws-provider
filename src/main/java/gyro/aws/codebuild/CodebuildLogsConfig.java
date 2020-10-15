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
import software.amazon.awssdk.services.codebuild.model.LogsConfig;

public class CodebuildLogsConfig extends Diffable implements Copyable<LogsConfig> {

    private CodebuildCloudWatchLogsConfig cloudWatchLogs;
    private CodebuildS3LogsConfig s3Logs;

    /**
     * The configuration for cloud watch logs.
     *
     * @subresource gyro.aws.codebuild.CodebuildCloudWatchLogsConfig
     */
    @Updatable
    public CodebuildCloudWatchLogsConfig getCloudWatchLogs() {
        return cloudWatchLogs;
    }

    public void setCloudWatchLogs(CodebuildCloudWatchLogsConfig cloudWatchLogs) {
        this.cloudWatchLogs = cloudWatchLogs;
    }

    /**
     * The configuration for the S3 logs.
     *
     * @subresource gyro.aws.codebuild.CodebuildS3LogsConfig
     */
    @Updatable
    public CodebuildS3LogsConfig getS3Logs() {
        return s3Logs;
    }

    public void setS3Logs(CodebuildS3LogsConfig s3Logs) {
        this.s3Logs = s3Logs;
    }

    @Override
    public void copyFrom(LogsConfig model) {

        if (model.cloudWatchLogs() != null) {
            CodebuildCloudWatchLogsConfig cloudWatchLogsConfig = newSubresource(CodebuildCloudWatchLogsConfig.class);
            cloudWatchLogsConfig.copyFrom(model.cloudWatchLogs());
            setCloudWatchLogs(cloudWatchLogsConfig);
        } else {
            setCloudWatchLogs(null);
        }

        if (model.s3Logs() != null) {
            CodebuildS3LogsConfig s3LogsConfig = newSubresource(CodebuildS3LogsConfig.class);
            s3LogsConfig.copyFrom(model.s3Logs());
            setS3Logs(s3LogsConfig);
        } else {
            setS3Logs(null);
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public LogsConfig toLogsConfig() {
        return LogsConfig.builder()
            .cloudWatchLogs(getCloudWatchLogs() != null ? getCloudWatchLogs().toCloudWatchLogsConfig() : null)
            .s3Logs(getS3Logs() != null ? getS3Logs().toS3LogsConfig() : null)
            .build();
    }
}
