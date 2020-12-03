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

import gyro.aws.autoscaling.AutoScalingGroupResource;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import gyro.core.validation.ValidationError;
import software.amazon.awssdk.services.ecs.model.AutoScalingGroupProvider;
import software.amazon.awssdk.services.ecs.model.ManagedScalingStatus;
import software.amazon.awssdk.services.ecs.model.ManagedTerminationProtection;

public class EcsAutoScalingGroupProvider extends Diffable {

    private AutoScalingGroupResource autoScalingGroup;
    private EcsManagedScaling managedScaling;
    private ManagedTerminationProtection managedTerminationProtection;

    /**
     * The Auto Scaling group for the capacity provider.
     */
    @Required
    public AutoScalingGroupResource getAutoScalingGroup() {
        return autoScalingGroup;
    }

    public void setAutoScalingGroup(AutoScalingGroupResource autoScalingGroup) {
        this.autoScalingGroup = autoScalingGroup;
    }

    /**
     * The managed scaling settings for the Auto Scaling group capacity provider.
     *
     * @subresource gyro.aws.ecs.EcsManagedScaling
     */
    public EcsManagedScaling getManagedScaling() {
        return managedScaling;
    }

    public void setManagedScaling(EcsManagedScaling managedScaling) {
        this.managedScaling = managedScaling;
    }

    /**
     * The managed termination protection setting to use for the Auto Scaling group capacity provider.
     * To use ``managed-termination-protection``, ``managed-scaling`` must have its ``status`` set to ``ENABLED``, and the ``auto-scaling-group`` must have ``new-instances-protected-from-scale-in`` set to ``true``.
     */
    @ValidStrings({"ENABLED", "DISABLED"})
    public ManagedTerminationProtection getManagedTerminationProtection() {
        return managedTerminationProtection;
    }

    public void setManagedTerminationProtection(ManagedTerminationProtection managedTerminationProtection) {
        this.managedTerminationProtection = managedTerminationProtection;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public void copyFrom(AutoScalingGroupProvider model) {
        String asgArnNamePrefix = "autoScalingGroupName/";
        int asgNameIndex = model.autoScalingGroupArn().indexOf(asgArnNamePrefix) + asgArnNamePrefix.length();
        String asgName = model.autoScalingGroupArn().substring(asgNameIndex);

        setAutoScalingGroup(findById(AutoScalingGroupResource.class, asgName));

        EcsManagedScaling scaling = newSubresource(EcsManagedScaling.class);
        scaling.copyFrom(model.managedScaling());
        setManagedScaling(scaling);

        setManagedTerminationProtection(model.managedTerminationProtection());
    }

    public AutoScalingGroupProvider copyTo() {
        return AutoScalingGroupProvider.builder()
            .autoScalingGroupArn(getAutoScalingGroup().getArn())
            .managedScaling(getManagedScaling() != null ? getManagedScaling().copyTo() : null)
            .managedTerminationProtection(getManagedTerminationProtection())
            .build();
    }

    @Override
    public List<ValidationError> validate(Set<String> configuredFields) {
        List<ValidationError> errors = new ArrayList<>();

        if (configuredFields.contains("managed-termination-protection")) {
            if (getManagedTerminationProtection().equals(ManagedTerminationProtection.ENABLED)) {
                if (!getAutoScalingGroup().getNewInstancesProtectedFromScaleIn()) {
                    errors.add(new ValidationError(
                        this,
                        "managed-termination-protection",
                        "'managed-termination-protection' can be set to 'ENABLED' only when the 'auto-scaling-group' has 'new-instances-protected-from-scale-in' set to 'true'."
                    ));
                }

                if (configuredFields.contains("managed-scaling") && !getManagedScaling().getStatus().equals(ManagedScalingStatus.ENABLED)) {
                    errors.add(new ValidationError(
                        this,
                        "managed-termination-protection",
                        "'managed-termination-protection' can be set to 'ENABLED' only when 'managed-scaling' has its 'status' set to 'ENABLED'."
                    ));
                }
            }
        }

        return errors;
    }
}
