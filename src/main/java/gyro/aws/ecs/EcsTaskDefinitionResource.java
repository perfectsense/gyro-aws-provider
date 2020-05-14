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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.iam.RoleResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.scope.State;
import gyro.core.validation.CollectionMax;
import gyro.core.validation.Range;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.Compatibility;
import software.amazon.awssdk.services.ecs.model.DescribeTaskDefinitionResponse;
import software.amazon.awssdk.services.ecs.model.EcsException;
import software.amazon.awssdk.services.ecs.model.NetworkMode;
import software.amazon.awssdk.services.ecs.model.RegisterTaskDefinitionResponse;
import software.amazon.awssdk.services.ecs.model.ResourceType;
import software.amazon.awssdk.services.ecs.model.TaskDefinition;

/**
 * Create an ECS task definition.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 */
@Type("ecs-task-definition")
public class EcsTaskDefinitionResource extends AwsResource implements Copyable<TaskDefinition> {

    private String family;
    private Set<String> requiresCompatibilities;
    private List<EcsContainerDefinition> containerDefinitions;
    private List<EcsVolume> volumes;
    private NetworkMode networkMode;
    private Integer cpu;
    private Integer memory;
    private RoleResource executionRole;
    private List<EcsInferenceAccelerator> inferenceAccelerators;
    private Integer revision;
    private String arn;

    @Required
    @Regex(value = "[-a-zA-Z0-9]{1,255}", message = "1 to 255 letters, numbers, and hyphens.")
    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    @ValidStrings({"EC2", "FARGATE"})
    public Set<String> getRequiresCompatibilities() {
        if (requiresCompatibilities == null) {
            requiresCompatibilities = new HashSet<>();
        }

        return requiresCompatibilities;
    }

    public void setRequiresCompatibilities(Set<String> requiresCompatibilities) {
        this.requiresCompatibilities = requiresCompatibilities;
    }

    /**
     *
     * @subresource gyro.aws.ecs.EcsContainerDefinition
     */
    @Required
    public List<EcsContainerDefinition> getContainerDefinitions() {
        if (containerDefinitions == null) {
            containerDefinitions = new ArrayList<>();
        }

        return containerDefinitions;
    }

    public void setContainerDefinitions(List<EcsContainerDefinition> containerDefinitions) {
        this.containerDefinitions = containerDefinitions;
    }

    /**
     *
     * @subresource gyro.aws.ecs.EcsVolume
     */
    public List<EcsVolume> getVolumes() {
        if (volumes == null) {
            volumes = new ArrayList<>();
        }

        return volumes;
    }

    public void setVolumes(List<EcsVolume> volumes) {
        this.volumes = volumes;
    }

    public NetworkMode getNetworkMode() {
        return networkMode;
    }

    public void setNetworkMode(NetworkMode networkMode) {
        this.networkMode = networkMode;
    }

    @Range(min = 128, max = 10240)
    public Integer getCpu() {
        return cpu;
    }

    public void setCpu(Integer cpu) {
        this.cpu = cpu;
    }

    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer memory) {
        this.memory = memory;
    }

    public RoleResource getExecutionRole() {
        return executionRole;
    }

    public void setExecutionRole(RoleResource executionRole) {
        this.executionRole = executionRole;
    }

    /**
     *
     * @subresource gyro.aws.ecs.EcsInferenceAccelerator
     */
    @CollectionMax(1)
    public List<EcsInferenceAccelerator> getInferenceAccelerators() {
        if (inferenceAccelerators == null) {
            inferenceAccelerators = new ArrayList<>();
        }

        return inferenceAccelerators;
    }

    public void setInferenceAccelerators(List<EcsInferenceAccelerator> inferenceAccelerators) {
        this.inferenceAccelerators = inferenceAccelerators;
    }

    @Output
    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    @Output
    @Id
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public void copyFrom(TaskDefinition model) {
        setFamily(model.family());
        setRequiresCompatibilities(new HashSet<>(model.requiresCompatibilitiesAsStrings()));
        setContainerDefinitions(
            model.containerDefinitions().stream().map(o -> {
                EcsContainerDefinition containerDefinition = newSubresource(EcsContainerDefinition.class);
                containerDefinition.copyFrom(o);
                return containerDefinition;
            }).collect(Collectors.toList())
        );
        setVolumes(
            model.volumes().stream().map(o -> {
                EcsVolume volume = newSubresource(EcsVolume.class);
                volume.copyFrom(o);
                return volume;
            }).collect(Collectors.toList())
        );
        setNetworkMode(model.networkMode());
        setCpu(ObjectUtils.isBlank(model.cpu()) ? null : Integer.decode(model.cpu()));
        setMemory(ObjectUtils.isBlank(model.memory()) ? null : Integer.decode(model.memory()));
        setExecutionRole(!ObjectUtils.isBlank(model.executionRoleArn())
            ? findById(RoleResource.class, model.executionRoleArn())
            : null);
        setInferenceAccelerators(
            model.inferenceAccelerators().stream().map(o -> {
                EcsInferenceAccelerator inferenceAccelerator = newSubresource(EcsInferenceAccelerator.class);
                inferenceAccelerator.copyFrom(o);
                return inferenceAccelerator;
            }).collect(Collectors.toList())
        );
        setRevision(model.revision());
        setArn(model.taskDefinitionArn());
    }

    @Override
    public boolean refresh() {
        EcsClient client = createClient(EcsClient.class);

        TaskDefinition definition = getTaskDefinition(client);

        if (definition == null) {
            return false;
        }

        copyFrom(definition);
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        EcsClient client = createClient(EcsClient.class);

        RegisterTaskDefinitionResponse response = client.registerTaskDefinition(
            r -> r.family(getFamily())
                .requiresCompatibilitiesWithStrings(getRequiresCompatibilities())
                .containerDefinitions(getContainerDefinitions().stream()
                    .map(EcsContainerDefinition::copyTo)
                    .collect(Collectors.toList()))
                .volumes(getVolumes().stream()
                    .map(EcsVolume::copyTo)
                    .collect(Collectors.toList()))
                .networkMode(getNetworkMode())
                .cpu(getCpu() != null ? getCpu().toString() : null)
                .memory(getMemory() != null ? getMemory().toString() : null)
                .executionRoleArn(getExecutionRole() != null ? getExecutionRole().getArn() : null)
                .inferenceAccelerators(getInferenceAccelerators().stream()
                    .map(EcsInferenceAccelerator::copyTo)
                    .collect(Collectors.toList()))
        );

        if (response.taskDefinition() != null) {
            setArn(response.taskDefinition().taskDefinitionArn());
        }

        Wait.atMost(10, TimeUnit.MINUTES)
            .checkEvery(30, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> isActive(client));
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {

    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        EcsClient client = createClient(EcsClient.class);

        client.deregisterTaskDefinition(r -> r.taskDefinition(getArn()));

        Wait.atMost(10, TimeUnit.MINUTES)
            .checkEvery(30, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> !isActive(client));
    }

    private TaskDefinition getTaskDefinition(EcsClient client) {
        if (ObjectUtils.isBlank(getArn())) {
            throw new GyroException("ARN is missing, unable to load task definition.");
        }

        TaskDefinition definition = null;

        try {
            DescribeTaskDefinitionResponse response = client.describeTaskDefinition(
                r -> r.taskDefinition(getArn()).includeWithStrings("TAGS")
            );

            if (response.taskDefinition() != null) {
                definition = response.taskDefinition();
            }
        } catch (EcsException ex) {
            // ignore
            System.out.println(ex.awsErrorDetails());
        }

        return definition;
    }

    private boolean isActive(EcsClient client) {
        TaskDefinition definition = getTaskDefinition(client);

        return definition != null && definition.statusAsString().equals("ACTIVE");
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();
        List<Integer> fargateMemValues = Arrays.asList(
            512, 1024, 2048, 3072,
            4096, 5120, 6144, 7168,
            8192, 92166, 10240, 11264,
            12288, 13312, 14336, 15360,
            16384, 17408, 18432, 19456,
            20480, 21504, 22528, 23552,
            24576, 25600, 26624, 27648,
            28672, 29696, 30720
        );

        if (getRequiresCompatibilities().contains(Compatibility.FARGATE.toString())) {
            if (!configuredFields.contains("cpu")) {
                errors.add(new ValidationError(
                    this,
                    "cpu",
                    "A cpu value must be specified when using the Fargate launch type."
                ));
            }

            if (getNetworkMode() != NetworkMode.AWSVPC) {
                errors.add(new ValidationError(
                    this,
                    "network-mode",
                    "The 'awsvpc' network-mode is required when using the Fargate launch type."
                ));
            }

            if (configuredFields.contains("cpu") && configuredFields.contains("memory")) {
                switch (getCpu()) {
                    case 256:
                        if (getMemory() > 2048 || !fargateMemValues.contains(getMemory())) {
                            errors.add(new ValidationError(
                                this,
                                "memory",
                                "When using the Fargate launch type, the valid memory values for 256 cpu units are: 512 (0.5 GB), 1024 (1 GB), 2048 (2 GB)."
                            ));
                        }
                        break;

                    case 512:
                        if (getMemory() < 1024 || getMemory() > 4096 || !fargateMemValues.contains(getMemory())) {
                            errors.add(new ValidationError(
                                this,
                                "memory",
                                "When using the Fargate launch type, the valid memory values for 512 cpu units are: 1024 (1 GB), 2048 (2 GB), 3072 (3 GB), 4096 (4 GB)."
                            ));
                        }
                        break;

                    case 1024:
                        if (getMemory() < 2048 || getMemory() > 8192 || !fargateMemValues.contains(getMemory())) {
                            errors.add(new ValidationError(
                                this,
                                "memory",
                                "When using the Fargate launch type, the valid memory values for 1024 cpu units are: 2048 (2 GB), 3072 (3 GB), 4096 (4 GB), 5120 (5 GB), 6144 (6 GB), 7168 (7 GB), 8192 (8 GB)."
                            ));
                        }
                        break;

                    case 2048:
                        if (getMemory() < 4096 || getMemory() > 16384 || !fargateMemValues.contains(getMemory())) {
                            errors.add(new ValidationError(
                                this,
                                "memory",
                                "When using the Fargate launch type, the valid memory values for 2048 cpu units are: between 4096 (4 GB) and 16384 (16 GB) in increments of 1024 (1 GB)."
                            ));
                        }
                        break;

                    case 4096:
                        if (getMemory() < 8192 || getMemory() > 30720 || !fargateMemValues.contains(getMemory())) {
                            errors.add(new ValidationError(
                                this,
                                "memory",
                                "When using the Fargate launch type, the valid memory values for 4096 cpu units are: between 8192 (8 GB) and 30720 (30 GB) in increments of 1024 (1 GB)."
                            ));
                        }
                        break;

                    default:
                        errors.add(new ValidationError(
                            this,
                            "cpu",
                            "When using the Fargate launch type, the valid cpu values are: 256 (.25 vCPU), 512 (.5 vCPU), 1024 (1 vCPU), 2048 (2 vCPU), 4096 (4 vCPU)."
                        ));
                }

            } else {
                if (!configuredFields.contains("cpu")) {
                    errors.add(new ValidationError(
                        this,
                        "cpu",
                        "A task-level cpu value must be specified when using the Fargate launch type."
                    ));

                }

                if (!configuredFields.contains("memory")) {
                    errors.add(new ValidationError(
                        this,
                        "memory",
                        "A task-level memory value must be specified when using the Fargate launch type."
                    ));
                }
            }

            if (configuredFields.contains("volumes")) {
                for (EcsVolume volume : getVolumes()) {
                    if (!getRequiresCompatibilities().contains(Compatibility.EC2.toString()) && volume.getDockerVolumeConfiguration() != null) {
                        errors.add(new ValidationError(
                            this,
                            "volumes",
                            "A Docker volume configuration may only be specified when using the EC2 launch type."
                        ));
                        break;
                    }

                    if (volume.getHostSourcePath() != null) {
                        errors.add(new ValidationError(
                            this,
                            "volumes",
                            "A host source path may not be specified for volumes when using the Fargate launch type."
                        ));
                        break;
                    }
                }
            }

            if (configuredFields.contains("inference-accelerators")) {
                errors.add(new ValidationError(
                    this,
                    "inference-accelerators",
                    "When using the Fargate launch type, inference accelerators may not be specified."
                ));
            }
        }

        int totalGPU = 0;
        int totalCPU = 0;
        int totalMemoryReservation = 0;
        boolean essentialContainer = false;
        for (EcsContainerDefinition container : getContainerDefinitions()) {
            if (!configuredFields.contains("memory") && container.getMemory() == null && container.getMemoryReservation() == null) {
                errors.add(new ValidationError(
                    this,
                    "memory",
                    "A task-level memory value or a container-level memory value must be specified when using the EC2 launch type."
                ));
                break;

            } else if (configuredFields.contains("memory")) {
                if (container.getMemoryReservation() != null) {
                    totalMemoryReservation += container.getMemoryReservation();

                    if (totalMemoryReservation > getMemory()) {
                        errors.add(new ValidationError(
                            this,
                            "container-definitions",
                            "The total of all container-level memory-reservation values within a task definition must not exceed the task-level memory value."
                        ));
                        break;
                    }
                }

                if (container.getMemory() != null && container.getMemory() > getMemory()) {
                    errors.add(new ValidationError(
                        this,
                        "container-definitions",
                        "Each container-level memory value must not exceed the task-level memory value."
                    ));
                    break;
                }
            }

            if (container.getMemoryReservation() != null && container.getMemory() != null && container.getMemory() <= container.getMemoryReservation()) {
                errors.add(new ValidationError(
                    this,
                    "container-definitions",
                    "If both a container-level memory and memory-reservation value are specified, memory must be greater than memory-reservation."
                ));
                break;
            }

            if (configuredFields.contains("cpu") && container.getCpu() != null) {
                totalCPU += container.getCpu();

                if (totalCPU > getCpu()) {
                    errors.add(new ValidationError(
                        this,
                        "container-definitions",
                        "The total of all container-level CPU values within a task definition must not exceed the task-level CPU value."
                    ));
                    break;
                }
            }

            if (!ObjectUtils.isBlank(container.getResourceRequirements())) {
                if (getRequiresCompatibilities().contains(Compatibility.FARGATE.toString())) {
                    errors.add(new ValidationError(
                        this,
                        "container-definitions",
                        "Resource requirements may not be configured when using the Fargate launch type."
                    ));
                    break;
                }

                Optional<EcsResourceRequirement> gpuRequirement = container.getResourceRequirements().stream()
                    .filter(o -> o.getType() == ResourceType.GPU)
                    .findFirst();

                if (gpuRequirement.isPresent()) {
                    totalGPU += Integer.valueOf(gpuRequirement.get().getValue());
                }

                if (totalGPU > 16) {
                    errors.add(new ValidationError(
                        this,
                        "container-definitions",
                        "The total of all GPU resource requirements' values across a task definition may not exceed 16."
                    ));
                    break;
                }
            }

            if (configuredFields.contains("network-mode")) {
                if (getNetworkMode() != NetworkMode.BRIDGE && !ObjectUtils.isBlank(container.getLinks())) {
                    errors.add(new ValidationError(
                        this,
                        "network-mode",
                        "The 'bridge' network-mode is required to configure the links parameter for a container definition."
                    ));
                    break;
                }

                if (getNetworkMode() == NetworkMode.AWSVPC && !ObjectUtils.isBlank(container.getPortMappings())) {
                    for (EcsPortMapping portMapping : container.getPortMappings()) {
                        if (!ObjectUtils.isBlank(portMapping.getHostPort()) && !portMapping.getHostPort().equals(portMapping.getContainerPort())) {
                            errors.add(new ValidationError(
                                this,
                                "container-definitions",
                                "When using the 'awsvpc' network-mode, only the container port should be specified. The host port can be left blank or must be the same value as the container port."
                            ));
                            break;
                        }
                    }
                }
            }

            if (container.getEssential() == null || container.getEssential()) {
                essentialContainer = true;
            }
        }

        if (!essentialContainer) {
            errors.add(new ValidationError(
                this,
                "container-definitions",
                "A task definition must have at least one essential container."
            ));
        }

        return errors;
    }
}
