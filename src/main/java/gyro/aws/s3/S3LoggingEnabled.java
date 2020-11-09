/*
 * Copyright 2019, Perfect Sense, Inc.
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

package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

import gyro.core.validation.Required;
import software.amazon.awssdk.services.s3.model.LoggingEnabled;

public class S3LoggingEnabled extends Diffable implements Copyable<LoggingEnabled> {
    private BucketResource bucket;
    private String prefix;

    /**
     * The target destination bucket for the logs.
     */
    @Required
    @Updatable
    public BucketResource getBucket() {
        return bucket;
    }

    public void setBucket(BucketResource targetBucket) {
        this.bucket = targetBucket;
    }

    /**
     * The destination prefix on the bucket to place logs.
     */
    @Updatable
    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String primaryKey() {
        return "logging enabled";
    }

    @Override
    public void copyFrom(LoggingEnabled loggingEnabled) {
        setBucket(findById(BucketResource.class, loggingEnabled.targetBucket()));
        setPrefix(loggingEnabled.targetPrefix());
    }

    LoggingEnabled toLoggingEnabled() {
        String prefix = getPrefix() == null ? "" : getPrefix();

        return LoggingEnabled.builder()
                .targetBucket(getBucket().getName())
                .targetPrefix(prefix)
                .build();
    }
}
