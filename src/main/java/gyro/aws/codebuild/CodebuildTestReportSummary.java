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

    @Output
    public Long getDurationInNanoSeconds() {
        return durationInNanoSeconds;
    }

    public void setDurationInNanoSeconds(Long durationInNanoSeconds) {
        this.durationInNanoSeconds = durationInNanoSeconds;
    }

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
