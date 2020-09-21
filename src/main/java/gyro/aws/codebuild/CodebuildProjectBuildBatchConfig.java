package gyro.aws.codebuild;

import com.psddev.dari.util.ObjectUtils;
import gyro.aws.Copyable;
import gyro.aws.iam.RoleResource;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import gyro.core.validation.Required;
import software.amazon.awssdk.services.codebuild.model.BatchRestrictions;
import software.amazon.awssdk.services.codebuild.model.ProjectBuildBatchConfig;

public class CodebuildProjectBuildBatchConfig extends Diffable implements Copyable<ProjectBuildBatchConfig> {

    private Boolean combineArtifacts;
    private CodebuildProjectBatchRestrictions restrictions;
    private RoleResource serviceRole;
    private Integer timeoutInMins;

    @Updatable
    public Boolean getCombineArtifacts() {
        return combineArtifacts;
    }

    public void setCombineArtifacts(Boolean combineArtifacts) {
        this.combineArtifacts = combineArtifacts;
    }

    @Updatable
    public CodebuildProjectBatchRestrictions getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(CodebuildProjectBatchRestrictions restrictions) {
        this.restrictions = restrictions;
    }

    @Updatable
    public RoleResource getServiceRole() {
        return serviceRole;
    }

    public void setServiceRole(RoleResource serviceRole) {
        this.serviceRole = serviceRole;
    }

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
