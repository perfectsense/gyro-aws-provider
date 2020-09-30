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

package gyro.aws.codebuild;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.aws.iam.RoleResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.codebuild.model.BatchRestrictions;
import software.amazon.awssdk.services.codebuild.model.ProjectBuildBatchConfig;

public class CodebuildProjectBuildBatchConfig extends Diffable implements Copyable<ProjectBuildBatchConfig> {

    private Boolean combineArtifacts;
    private CodebuildProjectBatchRestrictions restrictions;
    private RoleResource serviceRole;
    private Integer timeoutInMins;

    /**
     * The field that specifies if the build artifacts for the batch build should be combined into a single artifact
     * location.
     */
    @Updatable
    public Boolean getCombineArtifacts() {
        return combineArtifacts;
    }

    public void setCombineArtifacts(Boolean combineArtifacts) {
        this.combineArtifacts = combineArtifacts;
    }

    /**
     * The restrictrions for the batch build.
     */
    @Updatable
    public CodebuildProjectBatchRestrictions getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(CodebuildProjectBatchRestrictions restrictions) {
        this.restrictions = restrictions;
    }

    /**
     * The service role ARN for the batch build project.
     */
    @Updatable
    public RoleResource getServiceRole() {
        return serviceRole;
    }

    public void setServiceRole(RoleResource serviceRole) {
        this.serviceRole = serviceRole;
    }

    /**
     * The maximum amount of time that the batch build must be completed in.
     */
    @Updatable
    public Integer getTimeoutInMins() {
        return timeoutInMins;
    }

    public void setTimeoutInMins(Integer timeoutInMins) {
        this.timeoutInMins = timeoutInMins;
    }

    @Override
    public void copyFrom(ProjectBuildBatchConfig model) {
        setCombineArtifacts(model.combineArtifacts());
        setServiceRole(!ObjectUtils.isBlank(getServiceRole())
            ? findById(RoleResource.class, getServiceRole())
            : null);
        setTimeoutInMins(model.timeoutInMins());

        if (model.restrictions() != null) {
            CodebuildProjectBatchRestrictions batchRestrictions = newSubresource(CodebuildProjectBatchRestrictions.class);
            batchRestrictions.copyFrom(model.restrictions());
            setRestrictions(batchRestrictions);
        }
    }

    @Override
    public String primaryKey() {
        return "";
    }

    public ProjectBuildBatchConfig toProjectBuildBatchConfig() {
        BatchRestrictions batchRestrictions = BatchRestrictions.builder()
            .computeTypesAllowed(getRestrictions().getComputedTypesAllowed())
            .maximumBuildsAllowed(getRestrictions().getMaximumBuildsAllowed())
            .build();

        return ProjectBuildBatchConfig.builder()
            .combineArtifacts(getCombineArtifacts())
            .restrictions(batchRestrictions)
            .serviceRole(getServiceRole() != null ? getServiceRole().getArn() : null)
            .timeoutInMins(getTimeoutInMins())
            .build();
    }
}
