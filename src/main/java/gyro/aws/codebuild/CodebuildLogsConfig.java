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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.codebuild.model.LogsConfig;

public class CodebuildLogsConfig extends Diffable implements Copyable<LogsConfig> {

    private CodebuildCloudWatchLogsConfig cloudWatchLog;
    private CodebuildS3LogsConfig s3Log;

    /**
     * The configuration for cloud watch logs.
     *
     * @subresource gyro.aws.codebuild.CodebuildCloudWatchLogsConfig
     */
    @Updatable
    public CodebuildCloudWatchLogsConfig getCloudWatchLog() {
        return cloudWatchLog;
    }

    public void setCloudWatchLog(CodebuildCloudWatchLogsConfig cloudWatchLog) {
        this.cloudWatchLog = cloudWatchLog;
    }

    /**
     * The configuration for the S3 logs.
     *
     * @subresource gyro.aws.codebuild.CodebuildS3LogsConfig
     */
    @Updatable
    public CodebuildS3LogsConfig getS3Log() {
        return s3Log;
    }

    public void setS3Log(CodebuildS3LogsConfig s3Log) {
        this.s3Log = s3Log;
    }

    @Override
    public void copyFrom(LogsConfig model) {

        setCloudWatchLog(null);
        if (model.cloudWatchLogs() != null) {
            CodebuildCloudWatchLogsConfig cloudWatchLogsConfig = newSubresource(CodebuildCloudWatchLogsConfig.class);
            cloudWatchLogsConfig.copyFrom(model.cloudWatchLogs());
            setCloudWatchLog(cloudWatchLogsConfig);
        }

        setS3Log(null);
        if (model.s3Logs() != null) {
            CodebuildS3LogsConfig s3LogsConfig = newSubresource(CodebuildS3LogsConfig.class);
            s3LogsConfig.copyFrom(model.s3Logs());
            setS3Log(s3LogsConfig);
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public LogsConfig toLogsConfig() {
        return LogsConfig.builder()
            .cloudWatchLogs(getCloudWatchLog() != null ? getCloudWatchLog().toCloudWatchLogsConfig() : null)
            .s3Logs(getS3Log() != null ? getS3Log().toS3LogsConfig() : null)
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (getCloudWatchLog() == null && getS3Log() == null) {
            errors.add(new ValidationError(this, null, "At least one of 'cloud-watch-log' or 's3-log' is required."));
        }

        return errors;
    }
}
