package gyro.aws.codebuild;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import software.amazon.awssdk.services.codebuild.model.CodeCoverageReportSummary;

public class CodebuildCodeCoverageReportSummary extends Diffable implements Copyable<CodeCoverageReportSummary> {

    private Double branchCoveragePercentage;
    private Integer branchesCovered;
    private Integer branchesMissed;
    private Double lineCoveragePercentage;
    private Integer linesCovered;
    private Integer linesMissed;

    @Output
    public Double getBranchCoveragePercentage() {
        return branchCoveragePercentage;
    }

    public void setBranchCoveragePercentage(Double branchCoveragePercentage) {
        this.branchCoveragePercentage = branchCoveragePercentage;
    }

    @Output
    public Integer getBranchesCovered() {
        return branchesCovered;
    }

    public void setBranchesCovered(Integer branchesCovered) {
        this.branchesCovered = branchesCovered;
    }

    @Output
    public Integer getBranchesMissed() {
        return branchesMissed;
    }

    public void setBranchesMissed(Integer branchesMissed) {
        this.branchesMissed = branchesMissed;
    }

    @Output
    public Double getLineCoveragePercentage() {
        return lineCoveragePercentage;
    }

    public void setLineCoveragePercentage(Double lineCoveragePercentage) {
        this.lineCoveragePercentage = lineCoveragePercentage;
    }

    @Output
    public Integer getLinesCovered() {
        return linesCovered;
    }

    public void setLinesCovered(Integer linesCovered) {
        this.linesCovered = linesCovered;
    }

    @Output
    public Integer getLinesMissed() {
        return linesMissed;
    }

    public void setLinesMissed(Integer linesMissed) {
        this.linesMissed = linesMissed;
    }

    @Override
    public void copyFrom(CodeCoverageReportSummary model) {
        setBranchCoveragePercentage(model.branchCoveragePercentage());
        setBranchesCovered(model.branchesCovered());
        setBranchesMissed(model.branchesMissed());
        setLineCoveragePercentage(model.lineCoveragePercentage());
        setLinesCovered(model.linesCovered());
        setLinesMissed(model.linesMissed());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
