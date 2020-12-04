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

package gyro.aws.codebuild;

import java.util.ArrayList;
import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.codebuild.model.BatchRestrictions;

public class CodebuildProjectBatchRestrictions extends Diffable implements Copyable<BatchRestrictions> {

    private List<String> computedTypesAllowed;
    private Integer maximumBuildsAllowed;

    /**
     * The list that specifies the compute types that are allowed for the batch build.
     */
    @Updatable
    @ValidStrings({ "BUILD_GENERAL1_SMALL", "BUILD_GENERAL1_MEDIUM", "BUILD_GENERAL1_LARGE", "BUILD_GENERAL1_2XLARGE" })
    public List<String> getComputedTypesAllowed() {
        if (computedTypesAllowed == null) {
            computedTypesAllowed = new ArrayList<>();
        }

        return computedTypesAllowed;
    }

    public void setComputedTypesAllowed(List<String> computedTypesAllowed) {
        this.computedTypesAllowed = computedTypesAllowed;
    }

    /**
     * The maximum number of builds allowed.
     */
    @Updatable
    public Integer getMaximumBuildsAllowed() {
        return maximumBuildsAllowed;
    }

    public void setMaximumBuildsAllowed(Integer maximumBuildsAllowed) {
        this.maximumBuildsAllowed = maximumBuildsAllowed;
    }

    @Override
    public void copyFrom(BatchRestrictions model) {
        setComputedTypesAllowed(model.computeTypesAllowed());
        setMaximumBuildsAllowed(model.maximumBuildsAllowed());
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public BatchRestrictions toBatchRestriction() {
        return BatchRestrictions.builder()
            .computeTypesAllowed(getComputedTypesAllowed())
            .maximumBuildsAllowed(getMaximumBuildsAllowed())
            .build();
    }
}
