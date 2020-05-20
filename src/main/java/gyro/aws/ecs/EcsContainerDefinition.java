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
    private List<EcsPortMapping> portMapping;
    private Boolean essential;
    private List<String> entryPoint;
    private List<String> command;
    private Map<String, String> environment;
    private List<EcsMountPoint> mountPoint;
    private List<EcsVolumeFrom> volumeFrom;
    private EcsLinuxParameters linuxParameters;
    private List<EcsContainerDependency> dependsOn;
    private Integer startTimeout;
    private Integer stopTimeout;
    private String hostname;
    private List<EcsHostEntry> extraHost;
    private List<EcsUlimit> ulimit;
    private EcsLogConfiguration logConfiguration;
    private List<EcsResourceRequirement> resourceRequirement;

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
    public List<EcsPortMapping> getPortMapping() {
        if (portMapping == null) {
            portMapping = new ArrayList<>();
        }

        return portMapping;
    }

    public void setPortMapping(List<EcsPortMapping> portMapping) {
        this.portMapping = portMapping;
    }

    public Boolean getEssential() {
        if (essential == null) {
            essential = true;
        }

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
    public List<EcsMountPoint> getMountPoint() {
        if (mountPoint == null) {
            mountPoint = new ArrayList<>();
        }

        return mountPoint;
    }

    public void setMountPoint(List<EcsMountPoint> mountPoint) {
        this.mountPoint = mountPoint;
    }

    /**
     *
     * @subresource gyro.aws.ecs.EcsVolumeFrom
     */
    public List<EcsVolumeFrom> getVolumeFrom() {
        if (volumeFrom == null) {
            volumeFrom = new ArrayList<>();
        }

        return volumeFrom;
    }

    public void setVolumeFrom(List<EcsVolumeFrom> volumeFrom) {
        this.volumeFrom = volumeFrom;
    }

    /**
     *
     * @subresource gyro.aws.ecs.EcsLinuxParameters
     */
    public EcsLinuxParameters getLinuxParameters() {
        return linuxParameters;
    }

    public void setLinuxParameters(EcsLinuxParameters linuxParameters) {
        this.linuxParameters = linuxParameters;
    }

    /**
     *
     * @subresource gyro.aws.ecs.EcsContainerDependency
     */
    public List<EcsContainerDependency> getDependsOn() {
        if (dependsOn == null) {
            dependsOn = new ArrayList<>();
        }

        return dependsOn;
    }

    public void setDependsOn(List<EcsContainerDependency> dependsOn) {
        this.dependsOn = dependsOn;
    }

    public Integer getStartTimeout() {
        return startTimeout;
    }

    public void setStartTimeout(Integer startTimeout) {
        this.startTimeout = startTimeout;
    }

    @Max(120)
    public Integer getStopTimeout() {
        return stopTimeout;
    }

    public void setStopTimeout(Integer stopTimeout) {
        this.stopTimeout = stopTimeout;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     *
     * @subresource gyro.aws.ecs.EcsHostEntry
     */
    public List<EcsHostEntry> getExtraHost() {
        if (extraHost == null) {
            extraHost = new ArrayList<>();
        }

        return extraHost;
    }

    public void setExtraHost(List<EcsHostEntry> extraHost) {
        this.extraHost = extraHost;
    }

    /**
     *
     * @subresource gyro.aws.ecs.EcsUlimit
     */
    public List<EcsUlimit> getUlimit() {
        if (ulimit == null) {
            ulimit = new ArrayList<>();
        }

        return ulimit;
    }

    public void setUlimit(List<EcsUlimit> ulimit) {
        this.ulimit = ulimit;
    }

    /**
     *
     * @subresource gyro.aws.ecs.EcsLogConfiguration
     */
    public EcsLogConfiguration getLogConfiguration() {
        return logConfiguration;
    }

    public void setLogConfiguration(EcsLogConfiguration logConfiguration) {
        this.logConfiguration = logConfiguration;
    }

    /**
     *
     * @subresource gyro.aws.ecs.EcsResourceRequirement
     */
    public List<EcsResourceRequirement> getResourceRequirement() {
        if (resourceRequirement == null) {
            resourceRequirement = new ArrayList<>();
        }

        return resourceRequirement;
    }

    public void setResourceRequirement(List<EcsResourceRequirement> resourceRequirement) {
        this.resourceRequirement = resourceRequirement;
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
        setPortMapping(
            model.portMappings().stream().map(o -> {
                EcsPortMapping mapping = newSubresource(EcsPortMapping.class);
                mapping.copyFrom(o);
                return mapping;
            }).collect(Collectors.toList())
        );
        setEssential(model.essential());
        setEntryPoint(model.entryPoint());
        setCommand(model.command());
        setEnvironment(
            model.environment().stream().collect(Collectors.toMap(KeyValuePair::name, KeyValuePair::value))
        );
        setMountPoint(
            model.mountPoints().stream().map(o -> {
                EcsMountPoint mountPoint = newSubresource(EcsMountPoint.class);
                mountPoint.copyFrom(o);
                return mountPoint;
            }).collect(Collectors.toList())
        );
        setVolumeFrom(
            model.volumesFrom().stream().map(o -> {
                EcsVolumeFrom volumeFrom = newSubresource(EcsVolumeFrom.class);
                volumeFrom.copyFrom(o);
                return volumeFrom;
            }).collect(Collectors.toList())
        );

        EcsLinuxParameters parameters = null;
        if (model.linuxParameters() != null) {
            parameters = newSubresource(EcsLinuxParameters.class);
            parameters.copyFrom(model.linuxParameters());
        }
        setLinuxParameters(parameters);

        setDependsOn(
            model.dependsOn().stream().map(o -> {
                EcsContainerDependency dependency = newSubresource(EcsContainerDependency.class);
                dependency.copyFrom(o);
                return dependency;
            }).collect(Collectors.toList())
        );
        setStartTimeout(model.startTimeout());
        setStopTimeout(model.stopTimeout());
        setHostname(model.hostname());
        setExtraHost(
            model.extraHosts().stream().map(o -> {
                EcsHostEntry hostEntry = newSubresource(EcsHostEntry.class);
                hostEntry.copyFrom(o);
                return hostEntry;
            }).collect(Collectors.toList())
        );
        setUlimit(
            model.ulimits().stream().map(o -> {
                EcsUlimit limit = newSubresource(EcsUlimit.class);
                limit.copyFrom(o);
                return limit;
            }).collect(Collectors.toList())
        );

        EcsLogConfiguration logConfig = null;
        if (model.logConfiguration() != null) {
            logConfig = newSubresource(EcsLogConfiguration.class);
            logConfig.copyFrom(model.logConfiguration());
        }
        setLogConfiguration(logConfig);

        setResourceRequirement(
            model.resourceRequirements().stream().map(o -> {
                EcsResourceRequirement requirement = newSubresource(EcsResourceRequirement.class);
                requirement.copyFrom(o);
                return requirement;
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
            .portMappings(getPortMapping().stream()
                .map(EcsPortMapping::copyTo)
                .collect(Collectors.toList()))
            .essential(getEssential())
            .entryPoint(getEntryPoint())
            .command(getCommand())
            .environment(getEnvironment().entrySet().stream()
                .map(o -> KeyValuePair.builder().name(o.getKey()).value(o.getValue()).build())
                .collect(Collectors.toList()))
            .mountPoints(getMountPoint().stream()
                .map(EcsMountPoint::copyTo)
                .collect(Collectors.toList()))
            .volumesFrom(getVolumeFrom().stream()
                .map(EcsVolumeFrom::copyTo)
                .collect(Collectors.toList()))
            .linuxParameters(getLinuxParameters() != null ? getLinuxParameters().copyTo() : null)
            .dependsOn(getDependsOn().stream()
                .map(EcsContainerDependency::copyTo)
                .collect(Collectors.toList()))
            .startTimeout(getStartTimeout())
            .stopTimeout(getStopTimeout())
            .hostname(getHostname())
            .extraHosts(getExtraHost().stream()
                .map(EcsHostEntry::copyTo)
                .collect(Collectors.toList()))
            .ulimits(getUlimit().stream()
                .map(EcsUlimit::copyTo)
                .collect(Collectors.toList()))
            .logConfiguration(getLogConfiguration() != null ? getLogConfiguration().copyTo() : null)
            .resourceRequirements(getResourceRequirement().stream()
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
