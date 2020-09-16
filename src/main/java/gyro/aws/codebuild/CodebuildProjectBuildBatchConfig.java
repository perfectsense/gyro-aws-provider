package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.codebuild.model.ProjectBuildBatchConfig;

public class CodebuildProjectBuildBatchConfig extends Diffable implements Copyable<ProjectBuildBatchConfig> {

    private Boolean combineArtifacts;
    private CodebuildProjectBatchRestrictions restrictions;
    private String serviceRole;
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
    public String getServiceRole() {
        return serviceRole;
    }

    public void setServiceRole(String serviceRole) {
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
        setServiceRole(model.serviceRole());
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
}
