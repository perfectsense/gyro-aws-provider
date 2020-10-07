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

package gyro.aws.kendra;

import gyro.aws.Copyable;
import gyro.aws.s3.BucketResource;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.kendra.model.S3Path;

public class KendraS3Path extends Diffable implements Copyable<S3Path> {

    private BucketResource bucket;
    private String key;

    /**
     * The S3 bucket that contains the file. (Required)
     */
    @Required
    public BucketResource getBucket() {
        return bucket;
    }

    public void setBucket(BucketResource bucket) {
        this.bucket = bucket;
    }

    /**
     * The name of the file.
     */
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(S3Path model) {
        setBucket(findById(BucketResource.class, model.bucket()));
        setKey(model.key());
    }

    public S3Path toS3Path() {
        return S3Path.builder().bucket(getBucket().getName()).key(getKey()).build();
    }
}
