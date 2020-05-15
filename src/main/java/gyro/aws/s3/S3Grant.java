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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.s3.model.Grant;
import software.amazon.awssdk.services.s3.model.Permission;

public class S3Grant extends Diffable implements Copyable<Grant> {

    private S3Grantee grantee;
    private Permission permission;

    /**
     * The object being granted the permission. (Required)
     *
     * @subresource
     */
    @Required
    public S3Grantee getGrantee() {
        return grantee;
    }

    public void setGrantee(S3Grantee grantee) {
        this.grantee = grantee;
    }

    /**
     * The permission to be granted. (Required)
     */
    @Required
    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    @Override
    public void copyFrom(Grant model) {
        setPermission(model.permission());
        S3Grantee s3Grantee = newSubresource(S3Grantee.class);
        s3Grantee.copyFrom(model.grantee());
        setGrantee(s3Grantee);
    }

    @Override
    public String primaryKey() {
        return String.format("Grantee: %s Permission: %s", getGrantee().uniqueKey(), getPermission().toString());
    }

    Grant toGrant() {
        return Grant.builder().permission(getPermission()).grantee(getGrantee().toGrantee()).build();
    }
}
