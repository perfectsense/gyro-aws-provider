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

package gyro.aws.efs;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.efs.model.RootDirectory;

public class EfsRootDirectory extends Diffable implements Copyable<RootDirectory> {

    private String path;
    private EfsCreationInfo creationInfo;

    /**
     * The path on the EFS file system to expose as the root directory to NFS clients using the access point.
     */
    @Required
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * The POSIX IDs and permissions to apply to the access point's RootDirectory.
     *
     * @subresource gyro.aws.efs.EfsCreationInfo
     */
    public EfsCreationInfo getCreationInfo() {
        return creationInfo;
    }

    public void setCreationInfo(EfsCreationInfo creationInfo) {
        this.creationInfo = creationInfo;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(RootDirectory model) {
        setPath(getPath());

        if (model.creationInfo() != null) {
            EfsCreationInfo efsCreationInfo = newSubresource(EfsCreationInfo.class);
            efsCreationInfo.copyFrom(model.creationInfo());
            setCreationInfo(efsCreationInfo);
        }
    }

    public RootDirectory toRootDirectory() {
        return RootDirectory.builder().path(getPath()).creationInfo(getCreationInfo().toCreationInfo()).build();
    }
}
