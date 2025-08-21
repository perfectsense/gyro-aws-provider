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
import software.amazon.awssdk.services.s3control.model.Region;
import software.amazon.awssdk.services.s3control.model.RegionReport;

/**
 * Configuration for a region in an S3 Multi-Region Access Point.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     region
 *         bucket: $(aws::s3-bucket bucket-us-east-1)
 *     end
 */
public class S3MultiRegionAccessPointRegion extends Diffable implements Copyable<Region> {

    private BucketResource bucket;

    /**
     * The S3 bucket for this region.
     */
    @Required
    @Updatable
    public BucketResource getBucket() {
        return bucket;
    }

    public void setBucket(BucketResource bucket) {
        this.bucket = bucket;
    }

    @Override
    public void copyFrom(Region region) {
        // Note: The Region object from AWS contains the bucket name, but we need to resolve it to a BucketResource
        // For now, we'll create a minimal bucket resource with just the name
        if (region.bucket() != null) {
            BucketResource bucketResource = newSubresource(BucketResource.class);
            bucketResource.setName(region.bucket());
            setBucket(bucketResource);
        }
    }

    public void copyFromRegionReport(RegionReport regionReport) {
        // Note: The RegionReport object from AWS contains the bucket name, but we need to resolve it to a BucketResource
        if (regionReport.bucket() != null) {
            BucketResource bucketResource = newSubresource(BucketResource.class);
            bucketResource.setName(regionReport.bucket());
            setBucket(bucketResource);
        }
    }

    @Override
    public String primaryKey() {
        return getBucket() != null ? getBucket().getName() : "";
    }

    public Region toRegion() {
        return Region.builder()
            .bucket(getBucket() != null ? getBucket().getName() : null)
            .build();
    }
}