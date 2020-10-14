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

package gyro.aws.efs;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.efs.model.LifecyclePolicy;
import software.amazon.awssdk.services.efs.model.TransitionToIARules;

public class EfsLifecyclePolicy extends Diffable implements Copyable<LifecyclePolicy> {

    public TransitionToIARules transitionToIaRules;

    /**
     * The value that describes the period of time that a file is not accessed, after which it transitions to the IA storage class.
     */
    @Required
    @ValidStrings({"AFTER_7_DAYS", "AFTER_14_DAYS", "AFTER_30_DAYS", "AFTER_60_DAYS", "AFTER_90_DAYS"})
    public TransitionToIARules getTransitionToIaRules() {
        return transitionToIaRules;
    }

    public void setTransitionToIaRules(TransitionToIARules transitionToIaRules) {
        this.transitionToIaRules = transitionToIaRules;
    }

    @Override
    public void copyFrom(LifecyclePolicy model) {
        setTransitionToIaRules(model.transitionToIA());
    }

    @Override
    public String primaryKey() {
        return getTransitionToIaRules().toString();
    }

    public LifecyclePolicy toLifecyclePolicy() {
        return LifecyclePolicy.builder().transitionToIA(getTransitionToIaRules()).build();
    }
}
