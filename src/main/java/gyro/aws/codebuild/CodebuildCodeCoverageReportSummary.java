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
