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

package gyro.aws.efs;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Range;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.efs.model.CreationInfo;

public class EfsCreationInfo extends Diffable implements Copyable<CreationInfo> {

    private Long ownerGroupId;
    private Long ownerUserId;
    private String permissions;

    /**
     * The POSIX group ID to apply to the root directory. Valid values range from ``0`` to ``4294967295``.
     */
    @Range(min = 0, max = 4294967295L)
    public Long getOwnerGroupId() {
        return ownerGroupId;
    }

    public void setOwnerGroupId(Long ownerGroupId) {
        this.ownerGroupId = ownerGroupId;
    }

    /**
     * The POSIX user ID to apply to the root directory. Valid values range from ``0`` to ``4294967295``.
     */
    @Range(min = 0, max = 4294967295L)
    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    /**
     * The octal number representation of the POSIX permissions to apply to the root directory. Valid values are ``0000`` or ``0700`` or ``0770`` or ``0777`` or ``0111`` or ``0222`` or ``0333`` or ``0444`` or ``0555`` or ``0666`` or ``0740``.
     */
    @ValidStrings({ "0000", "0700", "0770", "0777", "0111", "0222", "0333", "0444", "0555", "0666", "0740" })
    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(CreationInfo model) {
        setOwnerGroupId(model.ownerGid());
        setOwnerUserId(model.ownerUid());
        setPermissions(getPermissions());
    }

    public CreationInfo toCreationInfo() {
        return CreationInfo.builder()
            .ownerGid(getOwnerGroupId())
            .ownerUid(getOwnerGroupId())
            .permissions(getPermissions())
            .build();
    }
}
