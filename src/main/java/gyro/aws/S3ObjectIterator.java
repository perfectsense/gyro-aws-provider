/*
 * Copyright 2020, Brightspot, Inc.
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

package gyro.aws;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

public class S3ObjectIterator implements Iterator<S3Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3ObjectIterator.class);

    private String bucket;
    private String prefix;
    private S3Client client;

    private List<S3Object> s3Objects = new ArrayList<>();
    private int index;
    private String nextPageToken;

    public S3ObjectIterator(String bucket, String prefix, S3Client client) {
        this.bucket = bucket;
        this.prefix = prefix;
        this.client = client;
    }

    @Override
    public boolean hasNext() {
        if (index < s3Objects.size()) {
            return true;
        }

        if (index > 0 && nextPageToken == null) {
            return false;
        }

        ListObjectsV2Response response = client.listObjectsV2(r -> r
            .bucket(bucket)
            .prefix(prefix)
            .continuationToken(nextPageToken)
            .maxKeys(100));
        List<S3Object> s3Objects = response.contents();

        if (!s3Objects.isEmpty()) {
            index = 0;
            this.s3Objects.clear();
            this.s3Objects.addAll(s3Objects);
        }

        nextPageToken = response.nextContinuationToken();

        return !this.s3Objects.isEmpty();
    }

    @Override
    public S3Object next() {
        if (hasNext()) {
            return s3Objects.get(index++);
        }
        throw new NoSuchElementException();
    }
}
