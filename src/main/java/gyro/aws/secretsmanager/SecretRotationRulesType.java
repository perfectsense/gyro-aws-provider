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

package gyro.aws.secretsmanager;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import software.amazon.awssdk.services.secretsmanager.model.RotationRulesType;

public class SecretRotationRulesType extends Diffable implements Copyable<RotationRulesType> {

    private Long automaticallyAfterDays;

    /**
     * The number of days between automatic scheduled rotations of the secret.
     */
    @Output
    public Long getAutomaticallyAfterDays() {
        return automaticallyAfterDays;
    }

    public void setAutomaticallyAfterDays(Long automaticallyAfterDays) {
        this.automaticallyAfterDays = automaticallyAfterDays;
    }

    @Override
    public void copyFrom(RotationRulesType model) {
        setAutomaticallyAfterDays(model.automaticallyAfterDays());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
