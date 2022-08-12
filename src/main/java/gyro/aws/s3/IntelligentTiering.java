/*
 * Copyright 2022, Brightspot.
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

package gyro.aws.s3;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.s3.model.IntelligentTieringAccessTier;
import software.amazon.awssdk.services.s3.model.Tiering;

public class IntelligentTiering extends Diffable implements Copyable<Tiering> {

    private Integer days;
    private IntelligentTieringAccessTier accessTier;

    /**
     * The days untill archive/deep archive based on selected value of ``access-tier``.
     */
    @Required
    @Updatable
    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    /**
     * The archive tier.
     */
    @Required
    @ValidStrings({"ARCHIVE_ACCESS", "DEEP_ARCHIVE_ACCESS"})
    public IntelligentTieringAccessTier getAccessTier() {
        return accessTier;
    }

    public void setAccessTier(IntelligentTieringAccessTier accessTier) {
        this.accessTier = accessTier;
    }

    @Override
    public void copyFrom(Tiering model) {
        setAccessTier(model.accessTier());
        setDays(model.days());
    }

    @Override
    public String primaryKey() {
        return getAccessTier().name();
    }

    protected Tiering toTiering() {
        return Tiering.builder()
            .accessTier(getAccessTier())
            .days(getDays())
            .build();
    }
}
