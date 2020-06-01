/*
 * Copyright 2020, Perfect Sense, Inc.
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AccessControlPolicy;
import software.amazon.awssdk.services.s3.model.GetBucketAclResponse;
import software.amazon.awssdk.services.s3.model.Owner;

public class S3AccessControlPolicy extends Diffable implements Copyable<GetBucketAclResponse> {

    private List<S3Grant> grant;

    /**
     * The list of grants for the bucket. (Required)
     *
     * @subresource gyro.aws.s3.S3Grant
     */
    @Required
    @Updatable
    public List<S3Grant> getGrant() {
        if (grant == null) {
            grant = new ArrayList<>();
        }

        return grant;
    }

    public void setGrant(List<S3Grant> grant) {
        this.grant = grant;
    }

    @Override
    public void copyFrom(GetBucketAclResponse model) {
        setGrant(model.grants().stream().map(g -> {
            S3Grant grant = newSubresource(S3Grant.class);
            grant.copyFrom(g);
            return grant;
        }).collect(Collectors.toList()));
    }

    @Override
    public String primaryKey() {
        return "";
    }

    AccessControlPolicy toAccessControlPolicy(S3Client client) {
        Owner owner = client.getBucketAcl(r -> r.bucket(((BucketResource) parentResource()).getName())).owner();

        return AccessControlPolicy.builder().owner(owner).grants(getGrant()
                .stream().map(S3Grant::toGrant).collect(Collectors.toList())).build();
    }
}
