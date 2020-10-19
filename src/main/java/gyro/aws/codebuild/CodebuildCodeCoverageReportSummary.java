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

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import gyro.core.validation.Min;
import gyro.core.validation.Range;
import software.amazon.awssdk.services.codebuild.model.CodeCoverageReportSummary;

public class CodebuildCodeCoverageReportSummary extends Diffable implements Copyable<CodeCoverageReportSummary> {

    private Double branchCoveragePercentage;
    private Integer branchesCovered;
    private Integer branchesMissed;
    private Double lineCoveragePercentage;
    private Integer linesCovered;
    private Integer linesMissed;

    /**
     * The percentage of branches that are covered by the tests. Valid values between ``0`` to ``100``.
     */
    @Range(min = 0, max = 100)
    @Output
    public Double getBranchCoveragePercentage() {
        return branchCoveragePercentage;
    }

    public void setBranchCoveragePercentage(Double branchCoveragePercentage) {
        this.branchCoveragePercentage = branchCoveragePercentage;
    }

    /**
     * The number of conditional branches that are covered by the tests. Minimum ``0`` branches covered.
     */
    @Min(0)
    @Output
    public Integer getBranchesCovered() {
        return branchesCovered;
    }

    public void setBranchesCovered(Integer branchesCovered) {
        this.branchesCovered = branchesCovered;
    }

    /**
     * The number of conditional branches that are not covered by the tests. Minimum ``0`` branches missed.
     */
    @Min(0)
    @Output
    public Integer getBranchesMissed() {
        return branchesMissed;
    }

    public void setBranchesMissed(Integer branchesMissed) {
        this.branchesMissed = branchesMissed;
    }

    /**
     * The percentage of lines that are covered by the tests. Valid values between ``0`` to ``100``.
     */
    @Range(min = 0, max = 100)
    @Output
    public Double getLineCoveragePercentage() {
        return lineCoveragePercentage;
    }

    public void setLineCoveragePercentage(Double lineCoveragePercentage) {
        this.lineCoveragePercentage = lineCoveragePercentage;
    }

    /**
     * The number of lines that are covered by the tests. Minimum ``0`` lines covered.
     */
    @Min(0)
    @Output
    public Integer getLinesCovered() {
        return linesCovered;
    }

    public void setLinesCovered(Integer linesCovered) {
        this.linesCovered = linesCovered;
    }

    /**
     * The number of lines that are not covered by the tests. Minimum ``0`` lines missed.
     */
    @Min(0)
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
