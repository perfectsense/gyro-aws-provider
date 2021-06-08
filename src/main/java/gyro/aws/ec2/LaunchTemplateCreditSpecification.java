/*
 * Copyright 2021, Perfect Sense.
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

package gyro.aws.ec2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.ec2.model.CreditSpecification;
import software.amazon.awssdk.services.ec2.model.CreditSpecificationRequest;

public class LaunchTemplateCreditSpecification extends Diffable implements Copyable<CreditSpecification> {

    private String cpuCredits;

    /**
     * The credit option for CPU usage of a ``t2``, ``t3``, or ``t3a`` instance. Valid values are ``standard`` and ``unlimited``.
     */
    @Required
    @Updatable
    @ValidStrings({ "standard", "unlimited" })
    public String getCpuCredits() {
        return cpuCredits;
    }

    public void setCpuCredits(String cpuCredits) {
        this.cpuCredits = cpuCredits;
    }

    @Override
    public void copyFrom(CreditSpecification model) {
        setCpuCredits(model.cpuCredits());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    CreditSpecificationRequest toCreditSpecification() {
        return CreditSpecificationRequest.builder().cpuCredits(getCpuCredits()).build();
    }
}
