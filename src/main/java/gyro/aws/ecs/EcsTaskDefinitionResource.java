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
import java.util.Objects;
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
    private List<EcsContainerDefinition> containerDefinition;
    private List<EcsVolume> volume;
    private NetworkMode networkMode;
    private Integer cpu;
    private Integer memory;
    private RoleResource executionRole;
    private List<EcsInferenceAccelerator> inferenceAccelerator;
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
    public List<EcsContainerDefinition> getContainerDefinition() {
        if (containerDefinition == null) {
            containerDefinition = new ArrayList<>();
        }

        return containerDefinition;
    }

    public void setContainerDefinition(List<EcsContainerDefinition> containerDefinition) {
        this.containerDefinition = containerDefinition;
    }

    /**
     *
     * @subresource gyro.aws.ecs.EcsVolume
     */
    public List<EcsVolume> getVolume() {
        if (volume == null) {
            volume = new ArrayList<>();
        }

        return volume;
    }

    public void setVolume(List<EcsVolume> volume) {
        this.volume = volume;
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
    public List<EcsInferenceAccelerator> getInferenceAccelerator() {
        if (inferenceAccelerator == null) {
            inferenceAccelerator = new ArrayList<>();
        }

        return inferenceAccelerator;
    }

    public void setInferenceAccelerator(List<EcsInferenceAccelerator> inferenceAccelerator) {
        this.inferenceAccelerator = inferenceAccelerator;
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
        setContainerDefinition(
            model.containerDefinitions().stream().map(o -> {
                EcsContainerDefinition containerDefinition = newSubresource(EcsContainerDefinition.class);
                containerDefinition.copyFrom(o);
                return containerDefinition;
            }).collect(Collectors.toList())
        );
        setVolume(
            model.volumes().stream().map(o -> {
                EcsVolume ecsVolume = newSubresource(EcsVolume.class);
                ecsVolume.copyFrom(o);
                return ecsVolume;
            }).collect(Collectors.toList())
        );
        setNetworkMode(model.networkMode());
        setCpu(ObjectUtils.isBlank(model.cpu()) ? null : Integer.decode(model.cpu()));
        setMemory(ObjectUtils.isBlank(model.memory()) ? null : Integer.decode(model.memory()));
        setExecutionRole(!ObjectUtils.isBlank(model.executionRoleArn())
            ? findById(RoleResource.class, model.executionRoleArn())
            : null);
        setInferenceAccelerator(
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
                .containerDefinitions(getContainerDefinition().stream()
                    .map(EcsContainerDefinition::copyTo)
                    .collect(Collectors.toList()))
                .volumes(getVolume().stream()
                    .map(EcsVolume::copyTo)
                    .collect(Collectors.toList()))
                .networkMode(getNetworkMode())
                .cpu(getCpu() != null ? getCpu().toString() : null)
                .memory(getMemory() != null ? getMemory().toString() : null)
                .executionRoleArn(getExecutionRole() != null ? getExecutionRole().getArn() : null)
                .inferenceAccelerators(getInferenceAccelerator().stream()
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
            if (getNetworkMode() != NetworkMode.AWSVPC) {
                errors.add(new ValidationError(
                    this,
                    "network-mode",
                    "The 'network-mode' must be set to 'awsvpc' when 'requires-compatibilities' contains 'FARGATE'."
                ));
            }

            if (configuredFields.contains("cpu") && configuredFields.contains("memory")) {
                switch (getCpu()) {
                    case 256:
                        if (getMemory() > 2048 || !fargateMemValues.contains(getMemory())) {
                            errors.add(new ValidationError(
                                this,
                                "memory",
                                "When 'requires-compatibilities' contains 'FARGATE', the valid 'memory' values for 256 'cpu' units are: 512 (0.5 GB), 1024 (1 GB), 2048 (2 GB)."
                            ));
                        }
                        break;

                    case 512:
                        if (getMemory() < 1024 || getMemory() > 4096 || !fargateMemValues.contains(getMemory())) {
                            errors.add(new ValidationError(
                                this,
                                "memory",
                                "When 'requires-compatibilities' contains 'FARGATE', the valid 'memory' values for 512 'cpu' units are: 1024 (1 GB), 2048 (2 GB), 3072 (3 GB), 4096 (4 GB)."
                            ));
                        }
                        break;

                    case 1024:
                        if (getMemory() < 2048 || getMemory() > 8192 || !fargateMemValues.contains(getMemory())) {
                            errors.add(new ValidationError(
                                this,
                                "memory",
                                "When 'requires-compatibilities' contains 'FARGATE', the valid 'memory' values for 1024 'cpu' units are: 2048 (2 GB), 3072 (3 GB), 4096 (4 GB), 5120 (5 GB), 6144 (6 GB), 7168 (7 GB), 8192 (8 GB)."
                            ));
                        }
                        break;

                    case 2048:
                        if (getMemory() < 4096 || getMemory() > 16384 || !fargateMemValues.contains(getMemory())) {
                            errors.add(new ValidationError(
                                this,
                                "memory",
                                "When 'requires-compatibilities' contains 'FARGATE', the valid 'memory' values for 2048 'cpu' units are: between 4096 (4 GB) and 16384 (16 GB) in increments of 1024 (1 GB)."
                            ));
                        }
                        break;

                    case 4096:
                        if (getMemory() < 8192 || getMemory() > 30720 || !fargateMemValues.contains(getMemory())) {
                            errors.add(new ValidationError(
                                this,
                                "memory",
                                "When 'requires-compatibilities' contains 'FARGATE', the valid 'memory' values for 4096 'cpu' units are: between 8192 (8 GB) and 30720 (30 GB) in increments of 1024 (1 GB)."
                            ));
                        }
                        break;

                    default:
                        errors.add(new ValidationError(
                            this,
                            "cpu",
                            "When 'requires-compatibilities' contains 'FARGATE', the valid 'cpu' values are: 256 (.25 vCPU), 512 (.5 vCPU), 1024 (1 vCPU), 2048 (2 vCPU), 4096 (4 vCPU)."
                        ));
                }

            } else {
                if (!configuredFields.contains("cpu")) {
                    errors.add(new ValidationError(
                        this,
                        "cpu",
                        "A task-level 'cpu' value must be specified when 'requires-compatibilities' contains 'FARGATE'."
                    ));

                }

                if (!configuredFields.contains("memory")) {
                    errors.add(new ValidationError(
                        this,
                        "memory",
                        "A task-level 'memory' value must be specified when 'requires-compatibilities' contains 'FARGATE'."
                    ));
                }
            }

            if (configuredFields.contains("inference-accelerator")) {
                errors.add(new ValidationError(
                    this,
                    "inference-accelerator",
                    "'inference-accelerator' may not be specified when 'requires-compatibilities' contains 'FARGATE'."
                ));
            }
        }

        if (configuredFields.contains("memory")) {
            int totalMemoryReservation = getContainerDefinition().stream()
                .map(EcsContainerDefinition::getMemoryReservation)
                .mapToInt(i -> i != null ? i : 0)
                .sum();

            if (totalMemoryReservation > getMemory()) {
                errors.add(new ValidationError(
                    this,
                    "memory",
                    "The total of all container-level 'memory-reservation' values must not exceed the task-level 'memory' value."
                ));
            }
        }

        if (configuredFields.contains("cpu")) {
            int totalCpu = getContainerDefinition().stream()
                .map(EcsContainerDefinition::getCpu)
                .mapToInt(i -> i != null ? i : 0)
                .sum();

            if (totalCpu > getCpu()) {
                errors.add(new ValidationError(
                    this,
                    "cpu",
                    "The total of all container-level 'cpu' values must not exceed the task-level 'cpu' value."
                ));
            }
        }

        int totalGpu = getContainerDefinition().stream()
            .map(c -> c.getResourceRequirement().stream()
                .filter(r -> r.getType() == ResourceType.GPU)
                .map(r -> Integer.valueOf(r.getValue()))
                .findFirst())
            .mapToInt(o -> o.orElse(0))
            .sum();

        if (totalGpu > 16) {
            errors.add(new ValidationError(
                this,
                "container-definition",
                "The total value of every 'resource-requirement' with 'type' set to 'GPU' across every 'container-definition' must not exceed 16."
            ));
        }

        if (getContainerDefinition().stream().noneMatch(EcsContainerDefinition::getEssential)) {
            errors.add(new ValidationError(
                this,
                "container-definition",
                "There must be at least one 'container-definition' with 'essential' set to 'true'."
            ));
        }

        return errors;
    }
}
