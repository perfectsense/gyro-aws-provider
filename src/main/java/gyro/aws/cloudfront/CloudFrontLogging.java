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

package gyro.aws.cloudfront;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.aws.s3.BucketResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.cloudfront.model.LoggingConfig;

public class CloudFrontLogging extends Diffable implements Copyable<LoggingConfig> {
    private BucketResource bucket;
    private String bucketPrefix;
    private Boolean includeCookies;

    /**
     * The bucket to save access logs. (Required)
     */
    @Required
    @Updatable
    public BucketResource getBucket() {
        return bucket;
    }

    public void setBucket(BucketResource bucket) {
        this.bucket = bucket;
    }

    /**
     * Directory within bucket ot save access logs.
     */
    @Updatable
    public String getBucketPrefix() {
        if (bucketPrefix == null) {
            bucketPrefix = "";
        }

        if (bucketPrefix.startsWith("/")) {
            bucketPrefix = bucketPrefix.replaceFirst("/", "");
        }

        return bucketPrefix;
    }

    public void setBucketPrefix(String bucketPrefix) {
        this.bucketPrefix = bucketPrefix;
    }

    /**
     * Whether to include cookies logs.
     */
    @Updatable
    public Boolean getIncludeCookies() {
        if (includeCookies == null) {
            includeCookies = false;
        }

        return includeCookies;
    }

    public void setIncludeCookies(Boolean includeCookies) {
        this.includeCookies = includeCookies;
    }

    @Override
    public void copyFrom(LoggingConfig loggingConfig) {
        setBucket(!ObjectUtils.isBlank(loggingConfig.bucket()) ? findById(BucketResource.class, loggingConfig.bucket().split(".s3.")[0]) : null);
        setBucketPrefix(loggingConfig.prefix());
        setIncludeCookies(loggingConfig.includeCookies());
    }

    @Override
    public String primaryKey() {
        return "logging";
    }

    LoggingConfig toLoggingConfig() {
        return LoggingConfig.builder()
            .bucket(getBucket() != null ? getBucket().getDomainName() : "")
            .prefix(getBucketPrefix())
            .includeCookies(getIncludeCookies())
            .enabled(true).build();
    }

    static LoggingConfig defaultLoggingConfig() {
        return LoggingConfig.builder()
            .bucket("")
            .prefix("")
            .includeCookies(false)
            .enabled(false).build();
    }
}
