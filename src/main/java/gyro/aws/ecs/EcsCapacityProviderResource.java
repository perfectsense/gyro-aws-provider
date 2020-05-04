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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.aws.autoscaling.AutoScalingGroupResource;
import gyro.core.GyroException;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.Wait;
import gyro.core.resource.Id;
import gyro.core.resource.Output;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Range;
import gyro.core.validation.Regex;
import gyro.core.validation.Required;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ecs.EcsClient;
import software.amazon.awssdk.services.ecs.model.AutoScalingGroupProvider;
import software.amazon.awssdk.services.ecs.model.CapacityProvider;
import software.amazon.awssdk.services.ecs.model.DescribeCapacityProvidersResponse;
import software.amazon.awssdk.services.ecs.model.EcsException;
import software.amazon.awssdk.services.ecs.model.Tag;

/**
 * Create an ECS capacity provider.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 * aws::ecs-capacity-provider ecs-capacity-provider-example
 *     name: "capacity-provider-example"
 *     auto-scaling-group: $(aws::autoscaling-group auto-scaling-group-capacity-provider-example)
 *     managed-scaling: true
 *     minimum-scaling-step-size: 1
 *     maximum-scaling-step-size: 50
 *     target-capacity: 75
 *     managed-termination-protection: true
 *
 *     tags: {
 *         'Name': 'capacity-provider-example'
 *     }
 *
 * end
 */
@Type("ecs-capacity-provider")
public class EcsCapacityProviderResource extends AwsResource implements Copyable<CapacityProvider> {

    private String name;
    private AutoScalingGroupResource autoScalingGroup;
    private Boolean managedScaling;
    private Integer minimumScalingStepSize;
    private Integer maximumScalingStepSize;
    private Integer targetCapacity;
    private Boolean managedTerminationProtection;
    private Map<String, String> tags;
    private String arn;

    /**
     * The name of the capacity provider. Up to 255 characters are allowed, including letters, numbers, underscores, and hyphens.
     * The name cannot be prefixed with ``aws``, ``ecs``, or ``fargate``, regardless of character case.
     */
    @Required
    @Id
    @Regex(value = "[-_a-zA-Z0-9]{1,255}", message = "1 to 255 letters, numbers, and hyphens.")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The Auto Scaling group for the capacity provider. (Required)
     */
    @Required
    public AutoScalingGroupResource getAutoScalingGroup() {
        return autoScalingGroup;
    }

    public void setAutoScalingGroup(AutoScalingGroupResource autoScalingGroup) {
        this.autoScalingGroup = autoScalingGroup;
    }

    /**
     * Enables managed scaling for the capacity provider.
     */
    public Boolean getManagedScaling() {
        return managedScaling;
    }

    public void setManagedScaling(Boolean managedScaling) {
        this.managedScaling = managedScaling;
    }

    /**
     * The minimum number of container instances that Amazon ECS will scale in or scale out at one time.
     */
    public Integer getMinimumScalingStepSize() {
        return minimumScalingStepSize;
    }

    public void setMinimumScalingStepSize(Integer minimumScalingStepSize) {
        this.minimumScalingStepSize = minimumScalingStepSize;
    }

    /**
     * The maximum number of container instances that Amazon ECS will scale in or scale out at one time.
     */
    public Integer getMaximumScalingStepSize() {
        return maximumScalingStepSize;
    }

    public void setMaximumScalingStepSize(Integer maximumScalingStepSize) {
        this.maximumScalingStepSize = maximumScalingStepSize;
    }

    /**
     * The target capacity percentage value for the capacity provider.
     * Valid values are greater than 0 and less than or equal to 100. A value of 100 will result in the EC2 instances in the Auto Scaling group being completely utilized.
     */
    @Range(min = 1, max = 100)
    public Integer getTargetCapacity() {
        return targetCapacity;
    }

    public void setTargetCapacity(Integer targetCapacity) {
        this.targetCapacity = targetCapacity;
    }

    /**
     * Enables managed termination protection for the Auto Scaling group capacity provider.
     * Managed termination protection will not work unless managed scaling is also enabled. The Auto Scaling group and each instance in the Auto Scaling group must have instance protection from scale-in actions enabled as well.
     */
    public Boolean getManagedTerminationProtection() {
        return managedTerminationProtection;
    }

    public void setManagedTerminationProtection(Boolean managedTerminationProtection) {
        this.managedTerminationProtection = managedTerminationProtection;
    }

    /**
     * The metadata applied to the cluster. Each tag consists of a key and an optional value.
     * Up to 50 tags per resource are allowed. The maximum character length is 128 for keys and 256 for values.
     * Tags may not be prefixed with ``aws:``, regardless of character case.
     */
    @Updatable
    public Map<String, String> getTags() {
        if (tags == null) {
            tags = new HashMap<>();
        }

        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    /**
     * The Amazon Resource Name (ARN) that identifies the capacity provider.
     */
    @Output
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Override
    public void copyFrom(CapacityProvider model) {
        AutoScalingGroupProvider asgProvider = model.autoScalingGroupProvider();

        // extract the AutoScalingGroup's name from its ARN
        String asgArnNamePrefix = "autoScalingGroupName/";
        int asgNameIndex = asgProvider.autoScalingGroupArn().indexOf(asgArnNamePrefix) + asgArnNamePrefix.length();
        String asgName = asgProvider.autoScalingGroupArn().substring(asgNameIndex);

        setName(model.name());
        setAutoScalingGroup(findById(AutoScalingGroupResource.class, asgName));
        setManagedScaling(asgProvider.managedScaling().statusAsString().equals("ENABLED"));
        setMinimumScalingStepSize(asgProvider.managedScaling().minimumScalingStepSize());
        setMaximumScalingStepSize(asgProvider.managedScaling().maximumScalingStepSize());
        setTargetCapacity(asgProvider.managedScaling().targetCapacity());
        setManagedTerminationProtection(asgProvider.managedTerminationProtectionAsString().equals("ENABLED"));
        setTags(
            model.tags().stream()
                .collect(Collectors.toMap(Tag::key, Tag::value))
        );
        setArn(model.capacityProviderArn());
    }

    @Override
    public boolean refresh() {
        EcsClient client = createClient(EcsClient.class);

        CapacityProvider provider = getCapacityProvider(client);

        if (provider == null) {
            return false;
        }

        copyFrom(provider);
        return true;
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        EcsClient client = createClient(EcsClient.class);

        client.createCapacityProvider(
            r -> r.name(getName())
                .autoScalingGroupProvider(
                o -> o.autoScalingGroupArn(getAutoScalingGroup().getArn())
                    .managedScaling(
                        m -> m.status(getManagedScaling() ? "ENABLED" : "DISABLED")
                            .minimumScalingStepSize(getMinimumScalingStepSize())
                            .maximumScalingStepSize(getMaximumScalingStepSize())
                            .targetCapacity(getTargetCapacity())
                    )
                    .managedTerminationProtection(getManagedTerminationProtection() ? "ENABLED" : "DISABLED")
            )
        );

        Wait.atMost(10, TimeUnit.MINUTES)
            .checkEvery(30, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> isActive(client));
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        EcsClient client = createClient(EcsClient.class);

        EcsCapacityProviderResource currentResource = (EcsCapacityProviderResource) current;
        Set<String> currentKeys = currentResource.getTags().keySet();

        if (!currentKeys.isEmpty()) {
            client.untagResource(r -> r.resourceArn(getArn()).tagKeys(currentKeys));
        }

        if (!getTags().isEmpty()) {
            client.tagResource(
                r -> r.resourceArn(getArn()).tags(getTags().entrySet().stream()
                    .map(o -> Tag.builder().key(o.getKey()).value(o.getValue()).build())
                    .collect(Collectors.toList()))
            );
        }

        Wait.atMost(10, TimeUnit.MINUTES)
            .checkEvery(30, TimeUnit.SECONDS)
            .prompt(false)
            .until(() -> isActive(client));
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {

    }

    private CapacityProvider getCapacityProvider(EcsClient client) {
        if (ObjectUtils.isBlank(getName())) {
            throw new GyroException("name is missing, unable to load capacity provider.");
        }

        CapacityProvider provider = null;

        try {
            DescribeCapacityProvidersResponse response = client.describeCapacityProviders(
                r -> r.capacityProviders(getName()).includeWithStrings("TAGS")
            );

            if (response.hasCapacityProviders()) {
                provider = response.capacityProviders().get(0);
            }
        } catch (EcsException ex) {
            // ignore
            System.out.println(ex.awsErrorDetails());
        }

        return provider;
    }

    private boolean isActive(EcsClient client) {
        CapacityProvider provider = getCapacityProvider(client);

        return provider != null && provider.statusAsString().equals("ACTIVE");
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (configuredFields.contains("name")) {
            if (getName().toLowerCase().startsWith("aws") || getName().toLowerCase().startsWith("ecs") || getName().toLowerCase().startsWith("fargate")) {
                errors.add(new ValidationError(
                    this,
                    "name",
                    "The capacity provider name cannot be prefixed with 'aws', 'ecs', or 'fargate'."
                ));
            }
        }

        if (configuredFields.contains("managed-termination-protection")) {
            if (getManagedTerminationProtection()) {
                if (!getAutoScalingGroup().getNewInstancesProtectedFromScaleIn()) {
                    errors.add(new ValidationError(
                        this,
                        "managed-termination-protection",
                        "To enable managed termination protection, the auto scaling group must have instance protection from scale-in actions enabled."
                    ));
                }

                if (!getManagedScaling()) {
                    errors.add(new ValidationError(
                        this,
                        "managed-termination-protection",
                        "To enable managed termination protection, managed scaling must be enabled."
                    ));
                }
            }
        }

        return errors;
    }
}
