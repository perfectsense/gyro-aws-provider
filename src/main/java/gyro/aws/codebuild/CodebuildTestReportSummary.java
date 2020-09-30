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

import java.util.HashMap;
import java.util.Map;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Output;
import software.amazon.awssdk.services.codebuild.model.TestReportSummary;

public class CodebuildTestReportSummary extends Diffable implements Copyable<TestReportSummary> {

    private Long durationInNanoSeconds;
    private Map<String, Integer> statusCounts;
    private Integer total;

    /**
     * The number of nanoseconds to run all of the test cases in the report.
     */
    @Output
    public Long getDurationInNanoSeconds() {
        return durationInNanoSeconds;
    }

    public void setDurationInNanoSeconds(Long durationInNanoSeconds) {
        this.durationInNanoSeconds = durationInNanoSeconds;
    }

    /**
     * The list that contains the number of each type of status returned by the test results in the test report summary.
     */
    @Output
    public Map<String, Integer> getStatusCounts() {
        if (statusCounts == null) {
            statusCounts = new HashMap<>();
        }

        return statusCounts;
    }

    public void setStatusCounts(Map<String, Integer> statusCounts) {
        this.statusCounts = statusCounts;
    }

    /**
     * The number of test cases in the test report summary.
     */
    @Output
    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    @Override
    public void copyFrom(TestReportSummary model) {
        setDurationInNanoSeconds(model.durationInNanoSeconds());
        setStatusCounts(model.statusCounts());
        setTotal(model.total());
    }

    @Override
    public String primaryKey() {
        return "";
    }
}
