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

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Range;
import software.amazon.awssdk.services.efs.model.PosixUser;

public class EfsPosixUser extends Diffable implements Copyable<PosixUser> {

    private Long userId;
    private Long groupId;
    private List<Long> secondaryGroupIds;

    /**
     * The POSIX user ID used for all file system operations using the parent access point. Accepts values ranging from 0 to 4294967295.
     */
    @Range(min = 0, max = 4294967295L)
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * The POSIX user ID used for all file system operations using the parent access point. Accepts values ranging from 0 to 4294967295.
     */
    @Range(min = 0, max = 4294967295L)
    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    /**
     * The list of secondary POSIX group IDs used for all file system operations using the parent access point.
     */
    public List<Long> getSecondaryGroupIds() {
        if (secondaryGroupIds == null) {
            secondaryGroupIds = new ArrayList<>();
        }

        return secondaryGroupIds;
    }

    public void setSecondaryGroupIds(List<Long> secondaryGroupIds) {
        this.secondaryGroupIds = secondaryGroupIds;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(PosixUser model) {
        setGroupId(model.gid());
        setUserId(model.uid());
        setSecondaryGroupIds(model.secondaryGids());
    }

    public PosixUser toPosixUser() {
        return PosixUser.builder().gid(getGroupId()).uid(getUserId()).secondaryGids(getSecondaryGroupIds()).build();
    }
}
