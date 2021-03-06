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

package gyro.aws.ecs;

import java.util.List;

import gyro.core.resource.Diffable;
import gyro.core.validation.Min;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.ecs.model.Tmpfs;

public class EcsTmpfs extends Diffable {

    private String containerPath;
    private Integer size;
    private List<String> mountOptions;

    /**
     * The absolute file path where the tmpfs volume is to be mounted.
     */
    @Required
    public String getContainerPath() {
        return containerPath;
    }

    public void setContainerPath(String containerPath) {
        this.containerPath = containerPath;
    }

    /**
     * The size (in MiB) of the tmpfs volume.
     */
    @Required
    @Min(1)
    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * The list of tmpfs volume mount options.
     */
    @ValidStrings({"defaults", "ro", "rw",
        "suid", "nosuid", "dev", "nodev", "exec",
        "noexec", "sync", "async", "dirsync", "remount",
        "mand", "nomand", "atime", "noatime", "diratime",
        "nodiratime", "bind", "rbind", "unbindable", "runbindable",
        "private", "rprivate", "shared", "rshared", "slave",
        "rslave", "relatime", "norelatime", "strictatime",
        "nostrictatime", "mode", "uid", "gid", "nr_inodes",
        "nr_blocks", "mpol"})
    public List<String> getMountOptions() {
        return mountOptions;
    }

    public void setMountOptions(List<String> mountOptions) {
        this.mountOptions = mountOptions;
    }

    @Override
    public String primaryKey() {
        return getContainerPath();
    }

    public void copyFrom(Tmpfs model) {
        setContainerPath(model.containerPath());
        setSize(model.size());
        setMountOptions(model.mountOptions());
    }

    public Tmpfs copyTo() {
        return Tmpfs.builder()
            .containerPath(getContainerPath())
            .size(getSize())
            .mountOptions(getMountOptions())
            .build();
    }
}
