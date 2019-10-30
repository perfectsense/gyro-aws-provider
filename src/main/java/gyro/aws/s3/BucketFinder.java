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

import gyro.aws.AwsFinder;
import gyro.core.Type;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Query bucket name.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *   bucket-name: $(external-query aws::s3-bucket { name: 'bucket-example'})
 */
@Type("s3-bucket")
public class BucketFinder extends AwsFinder<S3Client, Bucket, BucketResource> {
    private String name;

    /**
     * The name of the bucket. This should be a unique value.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected List<Bucket> findAllAws(S3Client client) {
        return client.listBuckets().buckets();
    }

    @Override
    protected List<Bucket> findAws(S3Client client, Map<String, String> filters) {
        List<Bucket> buckets = findAllAws(client);

        if (filters.containsKey("name")) {
            return buckets.stream().filter(o -> o.name().equals(filters.get("name"))).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }
}
