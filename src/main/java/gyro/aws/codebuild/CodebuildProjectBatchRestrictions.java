package gyro.aws.codebuild;

import java.util.List;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;
import software.amazon.awssdk.services.codebuild.model.BatchRestrictions;

public class CodebuildProjectBatchRestrictions extends Diffable implements Copyable<BatchRestrictions> {

    private List<String> computedTypesAllowed;
    private Integer maximumBuildsAllowed;

    @Updatable
    public List<String> getComputedTypesAllowed() {
        return computedTypesAllowed;
    }

    public void setComputedTypesAllowed(List<String> computedTypesAllowed) {
        this.computedTypesAllowed = computedTypesAllowed;
    }

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
        return "batch restrictions";
    }
}
