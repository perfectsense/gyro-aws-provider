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

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.aws.iam.RoleResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.codebuild.model.ProjectBuildBatchConfig;

public class CodebuildProjectBuildBatchConfig extends Diffable implements Copyable<ProjectBuildBatchConfig> {

    private Boolean combineArtifacts;
    private CodebuildProjectBatchRestrictions restriction;
    private RoleResource serviceRole;
    private Integer timeoutInMins;

    /**
     * When set to ``true`` the build artifacts for the batch build are combined into a single artifact.
     */
    @Required
    @Updatable
    public Boolean getCombineArtifacts() {
        return combineArtifacts;
    }

    public void setCombineArtifacts(Boolean combineArtifacts) {
        this.combineArtifacts = combineArtifacts;
    }

    /**
     * The configuration for project build batch restriction.
     *
     * @subresource gyro.aws.codebuild.CodebuildProjectBatchRestrictions
     */
    @Updatable
    public CodebuildProjectBatchRestrictions getRestrictions() {
        return restriction;
    }

    public void setRestrictions(CodebuildProjectBatchRestrictions restriction) {
        this.restriction = restriction;
    }

    /**
     * The service role for the batch build project.
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
    @Required
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
        setServiceRole(!ObjectUtils.isBlank(getServiceRole()) ? getServiceRole() : null);
        setTimeoutInMins(model.timeoutInMins());

        setRestrictions(null);
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
        return ProjectBuildBatchConfig.builder()
            .combineArtifacts(getCombineArtifacts())
            .restrictions(getRestrictions() != null ? getRestrictions().toBatchRestriction() : null)
            .serviceRole(getServiceRole() != null ? getServiceRole().getArn() : null)
            .timeoutInMins(getTimeoutInMins())
            .build();
    }
}
