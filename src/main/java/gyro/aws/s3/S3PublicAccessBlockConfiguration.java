/*
 * Copyright 2023, Perfect Sense, Inc.
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
import software.amazon.awssdk.services.s3.model.PublicAccessBlockConfiguration;

public class S3PublicAccessBlockConfiguration extends Diffable implements Copyable<PublicAccessBlockConfiguration> {

    private Boolean blockPublicAcls;
    private Boolean ignorePublicAcls;
    private Boolean blockPublicPolicy;
    private Boolean restrictPublicBuckets;

    /**
     * When set to ``true``, Amazon S3 will block public access control lists (ACLs) for this bucket and objects in this bucket.
     */
    @Updatable
    public Boolean getBlockPublicAcls() {
        return blockPublicAcls;
    }

    public void setBlockPublicAcls(Boolean blockPublicAcls) {
        this.blockPublicAcls = blockPublicAcls;
    }

    /**
     * When set to ``true``, Amazon S3 will ignore all public ACLs on this bucket and objects in this bucket.
     */
    @Updatable
    public Boolean getIgnorePublicAcls() {
        return ignorePublicAcls;
    }

    public void setIgnorePublicAcls(Boolean ignorePublicAcls) {
        this.ignorePublicAcls = ignorePublicAcls;
    }

    /**
     * When set to ``true``, Amazon S3 will reject calls to PUT Bucket policy if the specified bucket policy allows public access.
     */
    @Updatable
    public Boolean getBlockPublicPolicy() {
        return blockPublicPolicy;
    }

    public void setBlockPublicPolicy(Boolean blockPublicPolicy) {
        this.blockPublicPolicy = blockPublicPolicy;
    }

    /**
     * When set to ``true``, Amazon S3 will restrict access to this bucket to only Amazon Web Service principals and authorized users within this account if the bucket has a public policy.
     */
    @Updatable
    public Boolean getRestrictPublicBuckets() {
        return restrictPublicBuckets;
    }

    public void setRestrictPublicBuckets(Boolean restrictPublicBuckets) {
        this.restrictPublicBuckets = restrictPublicBuckets;
    }

    @Override
    public void copyFrom(PublicAccessBlockConfiguration model) {
        setBlockPublicAcls(model.blockPublicAcls());
        setIgnorePublicAcls(model.ignorePublicAcls());
        setBlockPublicPolicy(model.blockPublicPolicy());
        setRestrictPublicBuckets(model.restrictPublicBuckets());
    }

    @Override
    public String primaryKey() {
        return "PublicAccessBlockConfig";
    }

    public PublicAccessBlockConfiguration toPublicAccessBlockConfiguration() {
        return PublicAccessBlockConfiguration.builder()
            .blockPublicAcls(getBlockPublicAcls())
            .ignorePublicAcls(getIgnorePublicAcls())
            .blockPublicPolicy(getBlockPublicPolicy())
            .restrictPublicBuckets(getRestrictPublicBuckets())
            .build();
    }
}
