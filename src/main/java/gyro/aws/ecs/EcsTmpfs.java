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
     * The absolute file path where the tmpfs volume is to be mounted. (Required)
     */
    @Required
    public String getContainerPath() {
        return containerPath;
    }

    public void setContainerPath(String containerPath) {
        this.containerPath = containerPath;
    }

    /**
     * The size (in MiB) of the tmpfs volume. (Required)
     * The minimum value is ``1``.
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
     * Valid values are:
     * ``defaults``, ``ro``, ``rw``, ``suid``, ``nosuid``,
     * ``dev``, ``nodev``, ``exec``, ``noexec``, ``sync``,
     * ``async``, ``dirsync``, ``remount``, ``mand``, ``nomand``,
     * ``atime``, ``noatime``, ``diratime``, ``nodiratime``, ``bind``,
     * ``rbind``, ``unbindable``, ``runbindable``, ``private``, ``rprivate``,
     * ``shared``, ``rshared``, ``slave``, ``rslave``, ``relatime``,
     * ``norelatime``, ``strictatime``, ``nostrictatime``, ``mode``, ``uid``,
     * ``gid``, ``nr_inodes``, ``nr_blocks``, ``mpol``
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
