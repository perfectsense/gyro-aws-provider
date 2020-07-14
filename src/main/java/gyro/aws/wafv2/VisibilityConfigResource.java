package gyro.aws.wafv2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import software.amazon.awssdk.services.wafv2.model.VisibilityConfig;

public class VisibilityConfigResource extends Diffable implements Copyable<VisibilityConfig> {

    private String metricName;
    private Boolean cloudWatchMetricsEnabled;
    private Boolean sampledRequestsEnabled;

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public Boolean getCloudWatchMetricsEnabled() {
        if (cloudWatchMetricsEnabled == null) {
            cloudWatchMetricsEnabled = false;
        }

        return cloudWatchMetricsEnabled;
    }

    public void setCloudWatchMetricsEnabled(Boolean cloudWatchMetricsEnabled) {
        this.cloudWatchMetricsEnabled = cloudWatchMetricsEnabled;
    }

    public Boolean getSampledRequestsEnabled() {
        if (sampledRequestsEnabled == null) {
            sampledRequestsEnabled = false;
        }

        return sampledRequestsEnabled;
    }

    public void setSampledRequestsEnabled(Boolean sampledRequestsEnabled) {
        this.sampledRequestsEnabled = sampledRequestsEnabled;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void copyFrom(VisibilityConfig config) {
        setMetricName(config.metricName());
        setCloudWatchMetricsEnabled(config.cloudWatchMetricsEnabled());
        setSampledRequestsEnabled(config.sampledRequestsEnabled());
    }

    VisibilityConfig toVisibilityConfig() {
        return VisibilityConfig.builder()
            .metricName(getMetricName())
            .cloudWatchMetricsEnabled(getCloudWatchMetricsEnabled())
            .sampledRequestsEnabled(getSampledRequestsEnabled())
            .build();
    }
}
