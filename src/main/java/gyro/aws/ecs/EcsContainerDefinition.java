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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import gyro.core.resource.Diffable;
import gyro.core.validation.Max;
import gyro.core.validation.Min;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ecs.model.Compatibility;
import software.amazon.awssdk.services.ecs.model.ContainerDefinition;
import software.amazon.awssdk.services.ecs.model.KeyValuePair;
import software.amazon.awssdk.services.ecs.model.NetworkMode;
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
    private String user;
    private String workingDirectory;
    private Boolean disableNetworking;
    private Boolean privileged;
    private Boolean readonlyRootFilesystem;
    private List<String> dnsServers;
    private List<String> dnsSearchDomains;
    private List<EcsHostEntry> extraHost;
    private List<String> dockerSecurityOptions;
    private Boolean interactive;
    private Boolean pseudoTerminal;
    private Map<String, String> dockerLabels;
    private List<EcsUlimit> ulimit;
    private EcsLogConfiguration logConfiguration;
    private EcsHealthCheck healthCheck;
    private List<EcsSystemControl> systemControl;
    private List<EcsResourceRequirement> resourceRequirement;
    private EcsFirelensConfiguration firelensConfiguration;

    /**
     * The name of the container.
     */
    @Required
    @Regex(value = "[-a-zA-Z0-9]{1,255}", message = "a string 1 to 255 characters long containing letters, numbers, and hyphens")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The image used to start a container. This string is passed directly to the Docker daemon.
     */
    @Required
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    /**
     * The number of cpu units reserved for the container.
     */
    public Integer getCpu() {
        return cpu;
    }

    public void setCpu(Integer cpu) {
        this.cpu = cpu;
    }

    /**
     * The amount (in MiB) of memory to present to the container. If your container attempts to exceed the memory specified here, the container is killed.
     * The total amount of memory reserved for all containers within a task must be lower than the task ``memory`` value, if one is specified.
     * If a task-level ``memory`` value is not specified, you must specify a non-zero integer for one or both of ``memory`` or ``memory-reservation`` in a container definition. If you specify both, ``memory`` must be greater than ``memory-reservation``.
     */
    @Min(4)
    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer memory) {
        this.memory = memory;
    }

    /**
     * The soft limit (in MiB) of memory to reserve for the container. When system memory is under heavy contention, Docker attempts to keep the container memory to this soft limit. However, the container can consume more memory when it needs to, up to either the hard limit specified with the ``memory`` parameter (if applicable), or all of the available memory on the container instance, whichever comes first.
     * If a task-level ``memory`` value is not specified, you must specify a non-zero integer for one or both of ``memory`` or ``memory-reservation`` in a container definition. If you specify both, ``memory`` must be greater than ``memory-reservation``.
     */
    @Min(4)
    public Integer getMemoryReservation() {
        return memoryReservation;
    }

    public void setMemoryReservation(Integer memoryReservation) {
        this.memoryReservation = memoryReservation;
    }

    /**
     * Allows containers to communicate with each other without the need for any ``port-mapping``.
     * This parameter is only supported if the ``network-mode`` of a task definition is ``bridge``.
     */
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
     * The list of port mappings for the container. Port mappings allow containers to access ports on the host container instance to send or receive traffic.
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

    /**
     * If this parameter is set to ``true``, and the container fails or stops for any reason, all other containers that are part of the task are stopped.
     * Defaults to ``true``.
     */
    public Boolean getEssential() {
        if (essential == null) {
            essential = true;
        }

        return essential;
    }

    public void setEssential(Boolean essential) {
        this.essential = essential;
    }

    /**
     * The entry point that is passed to the container.
     */
    public List<String> getEntryPoint() {
        if (entryPoint == null) {
            entryPoint = new ArrayList<>();
        }

        return entryPoint;
    }

    public void setEntryPoint(List<String> entryPoint) {
        this.entryPoint = entryPoint;
    }

    /**
     * The command that is passed to the container. If there are multiple arguments, each argument should be a separate string in the list.
     */
    public List<String> getCommand() {
        if (command == null) {
            command = new ArrayList<>();
        }

        return command;
    }

    public void setCommand(List<String> command) {
        this.command = command;
    }

    /**
     * The environment variables to pass to a container.
     */
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
     * The mount points for data volumes in your container.
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
     * Data volumes to mount from another container.
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
     * Linux-specific modifications that are applied to the container, such as Linux kernel capabilities.
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
     * The dependencies defined for container startup and shutdown. A container can contain multiple dependencies. When a dependency is defined for container startup, for container shutdown it is reversed.
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

    /**
     * Time duration (in seconds) to wait before giving up on resolving dependencies for a container.
     */
    public Integer getStartTimeout() {
        return startTimeout;
    }

    public void setStartTimeout(Integer startTimeout) {
        this.startTimeout = startTimeout;
    }

    /**
     * Time duration (in seconds) to wait before the container is forcefully killed if it doesn't exit normally on its own. Defaults to ``30``.
     */
    @Max(120)
    public Integer getStopTimeout() {
        return stopTimeout;
    }

    public void setStopTimeout(Integer stopTimeout) {
        this.stopTimeout = stopTimeout;
    }

    /**
     * The hostname to use for your container.
     * This parameter is not supported if the task definition's ``network-mode`` is ``awsvpc``.
     */
    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * The user name to use inside the container.
     * You can use the following formats. If specifying a UID or GID, you must specify it as a positive integer.
     *      ``user``
     *      ``user:group``
     *      ``uid``
     *      ``uid:gid``
     *      ``user:gid``
     *      ``uid:group``
     */
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    /**
     * The working directory in which to run commands inside the container.
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * If ``true``, networking is disabled within the container.
     */
    public Boolean getDisableNetworking() {
        return disableNetworking;
    }

    public void setDisableNetworking(Boolean disableNetworking) {
        this.disableNetworking = disableNetworking;
    }

    /**
     * If ``true``, the container is given elevated privileges on the host container instance (similar to the root user).
     * This parameter is not supported when the task definition's 'requires-compatibilities' parameter contains 'FARGATE'.
     */
    public Boolean getPrivileged() {
        return privileged;
    }

    public void setPrivileged(Boolean privileged) {
        this.privileged = privileged;
    }

    /**
     * If ``true``, the container is given read-only access to its root file system.
     */
    public Boolean getReadonlyRootFilesystem() {
        return readonlyRootFilesystem;
    }

    public void setReadonlyRootFilesystem(Boolean readonlyRootFilesystem) {
        this.readonlyRootFilesystem = readonlyRootFilesystem;
    }

    /**
     * A list of DNS servers that are presented to the container.
     */
    public List<String> getDnsServers() {
        return dnsServers;
    }

    public void setDnsServers(List<String> dnsServers) {
        this.dnsServers = dnsServers;
    }

    /**
     * A list of DNS search domains that are presented to the container.
     */
    public List<String> getDnsSearchDomains() {
        return dnsSearchDomains;
    }

    public void setDnsSearchDomains(List<String> dnsSearchDomains) {
        this.dnsSearchDomains = dnsSearchDomains;
    }

    /**
     * A list of hostnames and IP address mappings to append to the /etc/hosts file on the container.
     * This parameter is not supported if the task definition's ``network-mode`` is ``awsvpc``.
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
     * A list of strings to provide custom labels for SELinux and AppArmor multi-level security systems.
     * This parameter is not supported when the task definition's 'requires-compatibilities' parameter contains 'FARGATE'
     */
    public List<String> getDockerSecurityOptions() {
        if (dockerSecurityOptions == null) {
            dockerSecurityOptions = new ArrayList<>();
        }

        return dockerSecurityOptions;
    }

    public void setDockerSecurityOptions(List<String> dockerSecurityOptions) {
        this.dockerSecurityOptions = dockerSecurityOptions;
    }

    /**
     * If ``true``, this allows you to deploy containerized applications that require stdin or a tty to be allocated.
     */
    public Boolean getInteractive() {
        return interactive;
    }

    public void setInteractive(Boolean interactive) {
        this.interactive = interactive;
    }

    /**
     * If ``true``, a TTY is allocated.
     */
    public Boolean getPseudoTerminal() {
        return pseudoTerminal;
    }

    public void setPseudoTerminal(Boolean pseudoTerminal) {
        this.pseudoTerminal = pseudoTerminal;
    }

    /**
     * A key/value map of labels to add to the container.
     */
    public Map<String, String> getDockerLabels() {
        if (dockerLabels == null) {
            dockerLabels = new HashMap<>();
        }

        return dockerLabels;
    }

    public void setDockerLabels(Map<String, String> dockerLabels) {
        this.dockerLabels = dockerLabels;
    }

    /**
     * A list of ulimits to set in the container.
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
     * The log configuration specification for the container.
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
     * The container health check command and associated configuration parameters for the container.
     *
     * @subresource gyro.aws.ecs.EcsHealthCheck
     */
    public EcsHealthCheck getHealthCheck() {
        return healthCheck;
    }

    public void setHealthCheck(EcsHealthCheck healthCheck) {
        this.healthCheck = healthCheck;
    }

    /**
     * A list of namespaced kernel parameters to set in the container.
     * This parameter is not supported when the task definition's 'requires-compatibilities' parameter contains 'FARGATE'.
     *
     * @subresource gyro.aws.ecs.EcsSystemControl
     */
    public List<EcsSystemControl> getSystemControl() {
        if (systemControl == null) {
            systemControl = new ArrayList<>();
        }

        return systemControl;
    }

    public void setSystemControl(List<EcsSystemControl> systemControl) {
        this.systemControl = systemControl;
    }

    /**
     * The type and amount of a resource to assign to a container.
     * This parameter is not supported when the task definition's 'requires-compatibilities' parameter contains 'FARGATE'.
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

    /**
     * The FireLens configuration for the container. This is used to specify and configure a log router for container logs.
     *
     * @subresource gyro.aws.ecs.EcsFirelensConfiguration
     */
    public EcsFirelensConfiguration getFirelensConfiguration() {
        return firelensConfiguration;
    }

    public void setFirelensConfiguration(EcsFirelensConfiguration firelensConfiguration) {
        this.firelensConfiguration = firelensConfiguration;
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
            model.environment().stream()
                .collect(Collectors.toMap(KeyValuePair::name, KeyValuePair::value))
        );

        // missing field: model.environmentFiles()

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
        setUser(model.user());
        setWorkingDirectory(model.workingDirectory());
        setDisableNetworking(model.disableNetworking());
        setPrivileged(model.privileged());
        setReadonlyRootFilesystem(model.readonlyRootFilesystem());
        setDnsServers(model.dnsServers());
        setDnsSearchDomains(model.dnsSearchDomains());
        setExtraHost(
            model.extraHosts().stream().map(o -> {
                EcsHostEntry hostEntry = newSubresource(EcsHostEntry.class);
                hostEntry.copyFrom(o);
                return hostEntry;
            }).collect(Collectors.toList())
        );
        setDockerSecurityOptions(model.dockerSecurityOptions());
        setInteractive(model.interactive());
        setPseudoTerminal(model.pseudoTerminal());
        setDockerLabels(model.dockerLabels());
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

        EcsHealthCheck check = null;
        if (model.healthCheck() != null) {
            check = newSubresource(EcsHealthCheck.class);
            check.copyFrom(model.healthCheck());
        }
        setHealthCheck(check);

        setSystemControl(
            model.systemControls().stream().map(o -> {
                EcsSystemControl control = newSubresource(EcsSystemControl.class);
                control.copyFrom(o);
                return control;
            }).collect(Collectors.toList())
        );
        setResourceRequirement(
            model.resourceRequirements().stream().map(o -> {
                EcsResourceRequirement requirement = newSubresource(EcsResourceRequirement.class);
                requirement.copyFrom(o);
                return requirement;
            }).collect(Collectors.toList())
        );

        EcsFirelensConfiguration configuration = null;
        if (model.firelensConfiguration() != null) {
            configuration = newSubresource(EcsFirelensConfiguration.class);
            configuration.copyFrom(model.firelensConfiguration());
        }
        setFirelensConfiguration(configuration);
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
            .user(getUser())
            .workingDirectory(getWorkingDirectory())
            .disableNetworking(getDisableNetworking())
            .privileged(getPrivileged())
            .readonlyRootFilesystem(getReadonlyRootFilesystem())
            .dnsServers(getDnsServers())
            .dnsSearchDomains(getDnsSearchDomains())
            .extraHosts(getExtraHost().stream()
                .map(EcsHostEntry::copyTo)
                .collect(Collectors.toList()))
            .dockerSecurityOptions(getDockerSecurityOptions())
            .interactive(getInteractive())
            .pseudoTerminal(getPseudoTerminal())
            .dockerLabels(getDockerLabels())
            .ulimits(getUlimit().stream()
                .map(EcsUlimit::copyTo)
                .collect(Collectors.toList()))
            .logConfiguration(getLogConfiguration() != null ? getLogConfiguration().copyTo() : null)
            .healthCheck(getHealthCheck() != null ? getHealthCheck().copyTo() : null)
            .systemControls(getSystemControl().stream()
                .map(EcsSystemControl::copyTo)
                .collect(Collectors.toList()))
            .resourceRequirements(getResourceRequirement().stream()
                .map(EcsResourceRequirement::copyTo)
                .collect(Collectors.toList()))
            .firelensConfiguration(getFirelensConfiguration() != null ? getFirelensConfiguration().copyTo() : null)
            .build();
    }

    EcsTaskDefinitionResource getParentTaskDefinition() {
        return (EcsTaskDefinitionResource) parent();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();
        EcsTaskDefinitionResource taskDefinition = getParentTaskDefinition();

        if (taskDefinition.getMemory() == null && !configuredFields.contains("memory") && !configuredFields.contains("memory-reservation")) {
            errors.add(new ValidationError(
                this,
                "memory",
                "A task-level 'memory' value or a container-level 'memory' or 'memory-reservation' value must be specified."
            ));

        } else {
            if (taskDefinition.getMemory() != null && configuredFields.contains("memory") && getMemory() > taskDefinition.getMemory()) {
                errors.add(new ValidationError(
                    this,
                    "memory",
                    "Each container-level 'memory' value must not exceed the task-level 'memory' value."
                ));
            }

            if (configuredFields.contains("memory") && configuredFields.contains("memory-reservation") && getMemory() <= getMemoryReservation()) {
                errors.add(new ValidationError(
                    this,
                    "memory-reservation",
                    "If both a container-level 'memory' and 'memory-reservation' value are specified, 'memory' must be greater than 'memory-reservation'."
                ));
            }
        }

        if (configuredFields.contains("links") && taskDefinition.getNetworkMode() != NetworkMode.BRIDGE) {
            errors.add(new ValidationError(
                this,
                "links",
                "'links' may only be specified when the task definition's 'network-mode' parameter is set to 'bridge'."
            ));
        }

        if (taskDefinition.getNetworkMode() == NetworkMode.AWSVPC) {
            if (configuredFields.contains("hostname")) {
                errors.add(new ValidationError(
                    this,
                    "hostname",
                    "'hostname' may not be specified when the task definition's 'network-mode' parameter is set to 'awsvpc'."
                ));
            }

            if (configuredFields.contains("extra-host")) {
                errors.add(new ValidationError(
                    this,
                    "extra-host",
                    "'extra-host' may not be specified when the task definition's 'network-mode' parameter is set to 'awsvpc'."
                ));
            }

            if (configuredFields.contains("dns-servers")) {
                errors.add(new ValidationError(
                    this,
                    "dns-servers",
                    "'dns-servers' may not be specified when the task definition's 'network-mode' parameter is set to 'awsvpc'."
                ));
            }
        }

        if (configuredFields.contains("port-mapping")) {
            if (taskDefinition.getNetworkMode() == NetworkMode.NONE) {
                errors.add(new ValidationError(
                    this,
                    "port-mapping",
                    "'port-mapping' may not be specified when the task definition's 'network-mode' parameter is set to 'none'."
                ));
            }

            List<Integer> tcpPorts = getPortMapping().stream()
                .filter(o -> o.getProtocol() == TransportProtocol.TCP)
                .map(EcsPortMapping::getContainerPort)
                .collect(Collectors.toList());

            boolean duplicatePort = getPortMapping().stream()
                .filter(o -> o.getProtocol() == TransportProtocol.UDP)
                .map(EcsPortMapping::getContainerPort)
                .anyMatch(tcpPorts::contains);

            if (duplicatePort) {
                errors.add(new ValidationError(
                    this,
                    "port-mapping",
                    "'port-mapping' may not be configured to expose the same 'container-port' for more than one 'protocol'."
                ));
            }
        }

        if (taskDefinition.getRequiresCompatibilities().contains(Compatibility.FARGATE.toString())) {
            if (configuredFields.contains("resource-requirement")) {
                errors.add(new ValidationError(
                    this,
                    "resource-requirement",
                    "'resource-requirement' may not be specified when the task definition's 'requires-compatibilities' parameter contains 'FARGATE'."
                ));
            }

            if (configuredFields.contains("privileged")) {
                errors.add(new ValidationError(
                    this,
                    "privileged",
                    "'privileged' may not be specified when the task definition's 'requires-compatibilities' parameter contains 'FARGATE'."
                ));
            }

            if (configuredFields.contains("docker-security-options")) {
                errors.add(new ValidationError(
                    this,
                    "docker-security-options",
                    "'docker-security-options' may not be specified when the task definition's 'requires-compatibilities' parameter contains 'FARGATE'."
                ));
            }

            if (configuredFields.contains("system-control")) {
                errors.add(new ValidationError(
                    this,
                    "system-control",
                    "'system-control' may not be specified when the task definition's 'requires-compatibilities' parameter contains 'FARGATE'."
                ));
            }
        }

        return errors;
    }
}
