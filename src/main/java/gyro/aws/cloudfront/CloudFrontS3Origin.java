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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.cloudfront.model.S3OriginConfig;

public class CloudFrontS3Origin extends Diffable implements Copyable<S3OriginConfig> {

    private String originAccessIdentity;

    /**
     * Origin access identity for serving private content through S3.
     */
    public String getOriginAccessIdentity() {
        if (originAccessIdentity == null) {
            return "";
        }

        return originAccessIdentity;
    }

    public void setOriginAccessIdentity(String originAccessIdentity) {
        this.originAccessIdentity = originAccessIdentity;
    }

    @Override
    public void copyFrom(S3OriginConfig s3OriginConfig) {
        setOriginAccessIdentity(s3OriginConfig.originAccessIdentity());
    }

    @Override
    public String primaryKey() {
        return "s3-origin";
    }

    S3OriginConfig toS3OriginConfig() {
        return S3OriginConfig.builder()
            .originAccessIdentity(getOriginAccessIdentity())
            .build();
    }
}
