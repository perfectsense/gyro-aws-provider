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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.core.resource.Diffable;
import gyro.core.validation.Min;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ecs.model.ContainerDefinition;
import software.amazon.awssdk.services.ecs.model.KeyValuePair;
import software.amazon.awssdk.services.ecs.model.ResourceType;
import software.amazon.awssdk.services.ecs.model.TransportProtocol;

public class EcsContainerDefinition extends Diffable {

    private String name;
    private String image;
    private Integer cpu;
    private Integer memory;
    private Integer memoryReservation;
    private List<String> links;
    private List<EcsPortMapping> portMappings;
    private Boolean essential;
    private List<String> entryPoint;
    private List<String> command;
    private Map<String, String> environment;
    private List<EcsMountPoint> mountPoints;
    private List<EcsVolumeFrom> volumesFrom;
    private List<EcsResourceRequirement> resourceRequirements;

    /**
     * The name of the container. (Required)
     * Valid values consist of 1 to 255 letters, numbers, and hyphens.
     */
    @Required
    @Regex(value = "[-a-zA-Z0-9]{1,255}", message = "1 to 255 letters, numbers, and hyphens.")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Required
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getCpu() {
        return cpu;
    }

    public void setCpu(Integer cpu) {
        this.cpu = cpu;
    }

    @Min(4)
    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer memory) {
        this.memory = memory;
    }

    @Min(4)
    public Integer getMemoryReservation() {
        return memoryReservation;
    }

    public void setMemoryReservation(Integer memoryReservation) {
        this.memoryReservation = memoryReservation;
    }

    public List<String> getLinks() {
        if (links == null) {
            links = new ArrayList<>();
        }

        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }

    /**
     *
     * @subresource gyro.aws.ecs.EcsPortMapping
     */
    public List<EcsPortMapping> getPortMappings() {
        if (portMappings == null) {
            portMappings = new ArrayList<>();
        }

        return portMappings;
    }

    public void setPortMappings(List<EcsPortMapping> portMappings) {
        this.portMappings = portMappings;
    }

    public Boolean getEssential() {
        return essential;
    }

    public void setEssential(Boolean essential) {
        this.essential = essential;
    }

    public List<String> getEntryPoint() {
        if (entryPoint == null) {
            entryPoint = new ArrayList<>();
        }

        return entryPoint;
    }

    public void setEntryPoint(List<String> entryPoint) {
        this.entryPoint = entryPoint;
    }

    public List<String> getCommand() {
        if (command == null) {
            command = new ArrayList<>();
        }

        return command;
    }

    public void setCommand(List<String> command) {
        this.command = command;
    }

    public Map<String, String> getEnvironment() {
        if (environment == null) {
            environment = new HashMap<>();
        }

        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    /**
     *
     * @subresource gyro.aws.ecs.EcsMountPoint
     */
    public List<EcsMountPoint> getMountPoints() {
        if (mountPoints == null) {
            mountPoints = new ArrayList<>();
        }

        return mountPoints;
    }

    public void setMountPoints(List<EcsMountPoint> mountPoints) {
        this.mountPoints = mountPoints;
    }

    /**
     *
     * @subresource gyro.aws.ecs.EcsVolumeFrom
     */
    public List<EcsVolumeFrom> getVolumesFrom() {
        if (volumesFrom == null) {
            volumesFrom = new ArrayList<>();
        }

        return volumesFrom;
    }

    public void setVolumesFrom(List<EcsVolumeFrom> volumesFrom) {
        this.volumesFrom = volumesFrom;
    }

    /**
     *
     * @subresource gyro.aws.ecs.EcsResourceRequirement
     */
    public List<EcsResourceRequirement> getResourceRequirements() {
        if (resourceRequirements == null) {
            resourceRequirements = new ArrayList<>();
        }

        return resourceRequirements;
    }

    public void setResourceRequirements(List<EcsResourceRequirement> resourceRequirements) {
        this.resourceRequirements = resourceRequirements;
    }

    @Override
    public String primaryKey() {
        return getName();
    }

    public void copyFrom(ContainerDefinition model) {
        setName(model.name());
        setImage(model.image());
        setCpu(model.cpu());
        setMemory(model.memory());
        setMemoryReservation(model.memoryReservation());
        setLinks(model.links());
        setPortMappings(
            model.portMappings().stream().map(o -> {
                EcsPortMapping portMapping = newSubresource(EcsPortMapping.class);
                portMapping.copyFrom(o);
                return portMapping;
            }).collect(Collectors.toList())
        );
        setEssential(model.essential());
        setEntryPoint(model.entryPoint());
        setCommand(model.command());
        setEnvironment(
            model.environment().stream().collect(Collectors.toMap(KeyValuePair::name, KeyValuePair::value))
        );
        setMountPoints(
            model.mountPoints().stream().map(o -> {
                EcsMountPoint mountPoint = newSubresource(EcsMountPoint.class);
                mountPoint.copyFrom(o);
                return mountPoint;
            }).collect(Collectors.toList())
        );
        setVolumesFrom(
            model.volumesFrom().stream().map(o -> {
                EcsVolumeFrom volumeFrom = newSubresource(EcsVolumeFrom.class);
                volumeFrom.copyFrom(o);
                return volumeFrom;
            }).collect(Collectors.toList())
        );
        setResourceRequirements(
            model.resourceRequirements().stream().map(o -> {
                EcsResourceRequirement resourceRequirement = newSubresource(EcsResourceRequirement.class);
                resourceRequirement.copyFrom(o);
                return resourceRequirement;
            }).collect(Collectors.toList())
        );
    }

    public ContainerDefinition copyTo() {
        return ContainerDefinition.builder()
            .name(getName())
            .image(getImage())
            .cpu(getCpu())
            .memory(getMemory())
            .memoryReservation(getMemoryReservation())
            .links(getLinks())
            .portMappings(getPortMappings().stream()
                .map(EcsPortMapping::copyTo)
                .collect(Collectors.toList()))
            .essential(getEssential())
            .entryPoint(getEntryPoint())
            .command(getCommand())
            .environment(getEnvironment().entrySet().stream()
                .map(o -> KeyValuePair.builder().name(o.getKey()).value(o.getValue()).build())
                .collect(Collectors.toList()))
            .mountPoints(getMountPoints().stream()
                .map(EcsMountPoint::copyTo)
                .collect(Collectors.toList()))
            .volumesFrom(getVolumesFrom().stream()
                .map(EcsVolumeFrom::copyTo)
                .collect(Collectors.toList()))
            .resourceRequirements(getResourceRequirements().stream()
                .map(EcsResourceRequirement::copyTo)
                .collect(Collectors.toList()))
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (configuredFields.contains("resource-requirements")) {
            int gpuCount = 0;
            int iaCount = 0;

            for (EcsResourceRequirement resourceRequirement : getResourceRequirements()) {
                if (resourceRequirement.getType() == ResourceType.INFERENCE_ACCELERATOR) {
                    iaCount++;
                }
                if (resourceRequirement.getType() == ResourceType.GPU) {
                    gpuCount++;
                }
                if (gpuCount > 1 || iaCount > 1) {
                    errors.add(new ValidationError(
                        this,
                        "resource-requirements",
                        "A container definition may not contain multiple resource requirements of the same type."
                    ));
                    break;
                }
            }
        }

        if (configuredFields.contains("port-mappings")) {
            Map<Integer, TransportProtocol> containerPortProtocols = new HashMap<>();

            for (EcsPortMapping portMapping : getPortMappings()) {
                TransportProtocol protocol = containerPortProtocols.get(portMapping.getContainerPort());

                if (protocol == null) {
                    containerPortProtocols.put(portMapping.getContainerPort(), portMapping.getProtocol());

                } else {
                    if (protocol != portMapping.getProtocol()) {
                        errors.add(new ValidationError(
                            this,
                            "port-mappings",
                            "Exposing the same container port for multiple protocols is forbidden."
                        ));
                        break;
                    }
                }
            }
        }

        return errors;
    }
}
