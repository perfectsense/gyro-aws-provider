package gyro.aws.cloudfront;

import java.util.Set;

import gyro.aws.AwsResource;
import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;
import gyro.core.scope.State;
import gyro.core.validation.Required;
import gyro.core.validation.ValidStrings;
import software.amazon.awssdk.services.cloudfront.CloudFrontClient;
import software.amazon.awssdk.services.cloudfront.model.RealtimeMetricsSubscriptionStatus;

public class MonitoringSubscription extends AwsResource implements Copyable<software.amazon.awssdk.services.cloudfront.model.MonitoringSubscription> {

    private RealtimeMetricsSubscriptionStatus status;

    /**
     * When set to ``true`` enables realtime metric subscription.
     */
    @Required
    @Updatable
    @ValidStrings({"Enabled", "Disabled"})
    public RealtimeMetricsSubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(RealtimeMetricsSubscriptionStatus enabled) {
        this.status = enabled;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.cloudfront.model.MonitoringSubscription model) {
        setStatus(model.realtimeMetricsSubscriptionConfig().realtimeMetricsSubscriptionStatus());
    }

    @Override
    public boolean refresh() {
        return false;
    }

    @Override
    public String primaryKey() {
        return "";
    }

    @Override
    public void create(GyroUI ui, State state) throws Exception {
        if (getStatus().equals(RealtimeMetricsSubscriptionStatus.ENABLED)) {
            CloudFrontResource parent = (CloudFrontResource) parent();

            CloudFrontClient client = createClient(
                CloudFrontClient.class,
                "us-east-1",
                "https://cloudfront.amazonaws.com");

            client.createMonitoringSubscription(r -> r.distributionId(parent.getId())
                .monitoringSubscription(rr -> rr
                    .realtimeMetricsSubscriptionConfig(rrr -> rrr
                        .realtimeMetricsSubscriptionStatus(RealtimeMetricsSubscriptionStatus.ENABLED))));
        }
    }

    @Override
    public void update(
        GyroUI ui, State state, Resource current, Set<String> changedFieldNames) throws Exception {
        CloudFrontResource parent = (CloudFrontResource) parent();

        CloudFrontClient client = createClient(
            CloudFrontClient.class,
            "us-east-1",
            "https://cloudfront.amazonaws.com");

        if (getStatus() == RealtimeMetricsSubscriptionStatus.DISABLED) {
            client.deleteMonitoringSubscription(r -> r.distributionId(parent.getId()));
        } else {
            client.createMonitoringSubscription(r -> r.distributionId(parent.getId())
                .monitoringSubscription(rr -> rr
                    .realtimeMetricsSubscriptionConfig(rrr -> rrr
                        .realtimeMetricsSubscriptionStatus(RealtimeMetricsSubscriptionStatus.ENABLED))));
        }
    }

    @Override
    public void delete(GyroUI ui, State state) throws Exception {
        CloudFrontResource parent = (CloudFrontResource) parent();

        CloudFrontClient client = createClient(
            CloudFrontClient.class,
            "us-east-1",
            "https://cloudfront.amazonaws.com");

        client.deleteMonitoringSubscription(r -> r.distributionId(parent.getId()));
    }
}
