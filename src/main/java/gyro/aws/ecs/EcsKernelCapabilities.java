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

package gyro.aws.ecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import gyro.core.resource.Diffable;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ecs.model.Compatibility;
import software.amazon.awssdk.services.ecs.model.KernelCapabilities;

public class EcsKernelCapabilities extends Diffable {

    private List<String> add;
    private List<String> drop;

    /**
     * The Linux capabilities for the container that have been added to the default configuration provided by Docker.
     * Valid values are:
     * ``ALL``, ``AUDIT_CONTROL``, ``AUDIT_WRITE``, ``BLOCK_SUSPEND``,
     * ``CHOWN``, ``DAC_OVERRIDE``, ``DAC_READ_SEARCH``, ``FOWNER``,
     * ``FSETID``, ``IPC_LOCK``, ``IPC_OWNER``, ``KILL``,
     * ``LEASE``, ``LINUX_IMMUTABLE``, ``MAC_ADMIN``, ``MAC_OVERRIDE``,
     * ``MKNOD``, ``NET_ADMIN``, ``NET_BIND_SERVICE``, ``NET_BROADCAST``,
     * ``NET_RAW``, ``SETFCAP``, ``SETGID``, ``SETPCAP``,
     * ``SETUID``, ``SYS_ADMIN``, ``SYS_BOOT``, ``SYS_CHROOT``,
     * ``SYS_MODULE``, ``SYS_NICE``, ``SYS_PACCT``, ``SYS_PTRACE``,
     * ``SYS_RAWIO``, ``SYS_RESOURCE``, ``SYS_TIME``, ``SYS_TTY_CONFIG``,
     * ``SYSLOG``, ``WAKE_ALARM``
     */
    @ValidStrings({ "ALL", "AUDIT_CONTROL", "AUDIT_WRITE",
        "BLOCK_SUSPEND", "CHOWN", "DAC_OVERRIDE", "DAC_READ_SEARCH", "FOWNER",
        "FSETID", "IPC_LOCK", "IPC_OWNER", "KILL", "LEASE",
        "LINUX_IMMUTABLE", "MAC_ADMIN", "MAC_OVERRIDE", "MKNOD", "NET_ADMIN",
        "NET_BIND_SERVICE", "NET_BROADCAST", "NET_RAW", "SETFCAP", "SETGID",
        "SETPCAP", "SETUID", "SYS_ADMIN", "SYS_BOOT", "SYS_CHROOT",
        "SYS_MODULE", "SYS_NICE", "SYS_PACCT", "SYS_PTRACE", "SYS_RAWIO",
        "SYS_RESOURCE", "SYS_TIME", "SYS_TTY_CONFIG", "SYSLOG", "WAKE_ALARM"})
    public List<String> getAdd() {
        if (add == null) {
            add = new ArrayList<>();
        }

        return add;
    }

    public void setAdd(List<String> add) {
        this.add = add;
    }

    /**
     * The Linux capabilities for the container that have been removed from the default configuration provided by Docker.
     * Valid values are:
     * ``ALL``, ``AUDIT_CONTROL``, ``AUDIT_WRITE``, ``BLOCK_SUSPEND``,
     * ``CHOWN``, ``DAC_OVERRIDE``, ``DAC_READ_SEARCH``, ``FOWNER``,
     * ``FSETID``, ``IPC_LOCK``, ``IPC_OWNER``, ``KILL``,
     * ``LEASE``, ``LINUX_IMMUTABLE``, ``MAC_ADMIN``, ``MAC_OVERRIDE``,
     * ``MKNOD``, ``NET_ADMIN``, ``NET_BIND_SERVICE``, ``NET_BROADCAST``,
     * ``NET_RAW``, ``SETFCAP``, ``SETGID``, ``SETPCAP``,
     * ``SETUID``, ``SYS_ADMIN``, ``SYS_BOOT``, ``SYS_CHROOT``,
     * ``SYS_MODULE``, ``SYS_NICE``, ``SYS_PACCT``, ``SYS_PTRACE``,
     * ``SYS_RAWIO``, ``SYS_RESOURCE``, ``SYS_TIME``, ``SYS_TTY_CONFIG``,
     * ``SYSLOG``, ``WAKE_ALARM``
     */
    @ValidStrings({ "ALL", "AUDIT_CONTROL", "AUDIT_WRITE",
        "BLOCK_SUSPEND", "CHOWN", "DAC_OVERRIDE", "DAC_READ_SEARCH", "FOWNER",
        "FSETID", "IPC_LOCK", "IPC_OWNER", "KILL", "LEASE",
        "LINUX_IMMUTABLE", "MAC_ADMIN", "MAC_OVERRIDE", "MKNOD", "NET_ADMIN",
        "NET_BIND_SERVICE", "NET_BROADCAST", "NET_RAW", "SETFCAP", "SETGID",
        "SETPCAP", "SETUID", "SYS_ADMIN", "SYS_BOOT", "SYS_CHROOT",
        "SYS_MODULE", "SYS_NICE", "SYS_PACCT", "SYS_PTRACE", "SYS_RAWIO",
        "SYS_RESOURCE", "SYS_TIME", "SYS_TTY_CONFIG", "SYSLOG", "WAKE_ALARM"})
    public List<String> getDrop() {
        if (drop == null) {
            drop = new ArrayList<>();
        }

        return drop;
    }

    public void setDrop(List<String> drop) {
        this.drop = drop;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public void copyFrom(KernelCapabilities model) {
        setAdd(model.add());
        setDrop(model.drop());
    }

    public KernelCapabilities copyTo() {
        return KernelCapabilities.builder()
            .add(getAdd())
            .drop(getDrop())
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (configuredFields.contains("add")) {
            EcsTaskDefinitionResource taskDefinition = ((EcsLinuxParameters) parent())
                .getParentContainerDefinition()
                .getParentTaskDefinition();

            if (taskDefinition.getRequiresCompatibilities().contains(Compatibility.FARGATE.toString())) {
                errors.add(new ValidationError(
                    this,
                    "add",
                    "The 'add' parameter is not supported when the task definition's 'requires-compatibilities' parameter contains 'FARGATE'."
                ));
            }
        }

        return errors;
    }
}
