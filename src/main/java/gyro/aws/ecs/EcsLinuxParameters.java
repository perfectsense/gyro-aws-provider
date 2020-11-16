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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.core.resource.Diffable;
import gyro.core.validation.DependsOn;
import gyro.core.validation.Min;
import gyro.core.validation.Range;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ecs.model.Compatibility;
import software.amazon.awssdk.services.ecs.model.LinuxParameters;

public class EcsLinuxParameters extends Diffable {

    private EcsKernelCapabilities capabilities;
    private List<EcsDevice> device;
    private Boolean initProcessEnabled;
    private Integer sharedMemorySize;
    private List<EcsTmpfs> tmpfs;
    private Integer maxSwap;
    private Integer swappiness;

    /**
     * The Linux capabilities for the container that are added to or dropped from the default configuration provided by Docker.
     *
     * @subresource gyro.aws.ecs.EcsKernelCapabilities
     */
    public EcsKernelCapabilities getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(EcsKernelCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    /**
     * Any host devices to expose to the container.
     * Not supported under task definitions whose ``requires-compatibilities`` parameter contains ``FARGATE``.
     *
     * @subresource gyro.aws.ecs.EcsDevice
     */
    public List<EcsDevice> getDevice() {
        if (device == null) {
            device = new ArrayList<>();
        }

        return device;
    }

    public void setDevice(List<EcsDevice> device) {
        this.device = device;
    }

    /**
     * Enable to run an init process inside the container that forwards signals and reaps processes.
     */
    public Boolean getInitProcessEnabled() {
        return initProcessEnabled;
    }

    public void setInitProcessEnabled(Boolean initProcessEnabled) {
        this.initProcessEnabled = initProcessEnabled;
    }

    /**
     * The value for the size (in MiB) of the /dev/shm volume.
     * Not supported under task definitions whose ``requires-compatibilities`` parameter contains ``FARGATE``.
     */
    public Integer getSharedMemorySize() {
        return sharedMemorySize;
    }

    public void setSharedMemorySize(Integer sharedMemorySize) {
        this.sharedMemorySize = sharedMemorySize;
    }

    /**
     * The container path, mount options, and size (in MiB) of the tmpfs mount.
     * Not supported under task definitions whose ``requires-compatibilities`` parameter contains ``FARGATE``.
     *
     * @subresource gyro.aws.ecs.EcsTmpfs
     */
    public List<EcsTmpfs> getTmpfs() {
        if (tmpfs == null) {
            tmpfs = new ArrayList<>();
        }

        return tmpfs;
    }

    public void setTmpfs(List<EcsTmpfs> tmpfs) {
        this.tmpfs = tmpfs;
    }

    /**
     * The total amount of swap memory (in MiB) a container can use.
     * If a value of ``0`` is specified, the container will not use swap.
     * Not supported under task definitions whose ``requires-compatibilities`` parameter contains ``FARGATE``.
     */
    @Min(0)
    public Integer getMaxSwap() {
        return maxSwap;
    }

    public void setMaxSwap(Integer maxSwap) {
        this.maxSwap = maxSwap;
    }

    /**
     * This allows you to tune a container's memory swappiness behavior. A swappiness value of ``0`` will cause swapping to not happen unless absolutely necessary. A swappiness value of ``100`` will cause pages to be swapped very aggressively. Defaults to ``60``.
     */
    @Range(min = 0, max = 100)
    @DependsOn("max-swap")
    public Integer getSwappiness() {
        return swappiness;
    }

    public void setSwappiness(Integer swappiness) {
        this.swappiness = swappiness;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public void copyFrom(LinuxParameters model) {
        EcsKernelCapabilities kernelCapabilities = null;
        if (model.capabilities() != null) {
            kernelCapabilities = newSubresource(EcsKernelCapabilities.class);
            kernelCapabilities.copyFrom(model.capabilities());
        }

        setCapabilities(kernelCapabilities);
        setDevice(
            model.devices().stream().map(o -> {
                EcsDevice device = newSubresource(EcsDevice.class);
                device.copyFrom(o);
                return device;
            }).collect(Collectors.toList())
        );
        setInitProcessEnabled(model.initProcessEnabled());
        setSharedMemorySize(model.sharedMemorySize());
        setTmpfs(
            model.tmpfs().stream().map(o -> {
                EcsTmpfs tmpfs = newSubresource(EcsTmpfs.class);
                tmpfs.copyFrom(o);
                return tmpfs;
            }).collect(Collectors.toList())
        );
        setMaxSwap(model.maxSwap());
        setSwappiness(model.swappiness());
    }

    public LinuxParameters copyTo() {
        return LinuxParameters.builder()
            .capabilities(getCapabilities() != null ? getCapabilities().copyTo() : null)
            .devices(getDevice().stream()
                .map(EcsDevice::copyTo)
                .collect(Collectors.toList()))
            .initProcessEnabled(getInitProcessEnabled())
            .sharedMemorySize(getSharedMemorySize())
            .tmpfs(getTmpfs().stream()
                .map(EcsTmpfs::copyTo)
                .collect(Collectors.toList()))
            .maxSwap(getMaxSwap())
            .swappiness(getSwappiness())
            .build();
    }

    EcsContainerDefinition getParentContainerDefinition() {
        return (EcsContainerDefinition) parent();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        EcsTaskDefinitionResource taskDefinition = getParentContainerDefinition().getParentTaskDefinition();

        if (taskDefinition.getRequiresCompatibilities().contains(Compatibility.FARGATE.toString())) {
            if (configuredFields.contains("device")) {
                errors.add(new ValidationError(
                    this,
                    "device",
                    "The 'device' parameter is not supported when the task definition's 'requires-compatibilities' parameter contains 'FARGATE'."
                ));
            }

            if (configuredFields.contains("shared-memory-size")) {
                errors.add(new ValidationError(
                    this,
                    "shared-memory-size",
                    "The 'shared-memory-size' parameter is not supported when the task definition's 'requires-compatibilities' parameter contains 'FARGATE'."
                ));
            }

            if (configuredFields.contains("tmpfs")) {
                errors.add(new ValidationError(
                    this,
                    "tmpfs",
                    "The 'tmpfs' parameter is not supported when the task definition's 'requires-compatibilities' parameter contains 'FARGATE'."
                ));
            }

            if (configuredFields.contains("max-swap")) {
                errors.add(new ValidationError(
                    this,
                    "max-swap",
                    "The 'max-swap' parameter is not supported when the task definition's 'requires-compatibilities' parameter contains 'FARGATE'."
                ));
            }

            if (configuredFields.contains("swappiness")) {
                errors.add(new ValidationError(
                    this,
                    "swappiness",
                    "The 'swappiness' parameter is not supported when the task definition's 'requires-compatibilities' parameter contains 'FARGATE'."
                ));
            }
        }

        return errors;
    }
}
