package gyro.aws.elb;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     attribute
 * 			access-log
 * 				emit-interval: 5
 * 				enabled: true
 * 				bucket: $(aws::bucket bucket)
 * 				bucket-prefix: "logs/"
 * 			end
 *
 * 			connection-draining
 * 				enabled: true
 * 				timeout: 800
 * 			end
 *
 * 			cross-zone-load-balancing
 * 				enabled: true
 * 			end
 *
 * 			connection-settings
 * 				idle-timeout: 800
 * 			end
 * 		end
 */
public class LoadBalancerAttributes extends Diffable implements Copyable<software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerAttributes> {
    private LoadBalancerAccessLog accessLog;
    private LoadBalancerConnectionDraining connectionDraining;
    private LoadBalancerCrossZoneLoadBalancing crossZoneLoadBalancing;
    private LoadBalancerConnectionSettings connectionSettings;

    /**
     * The Access Log setting for the Load Balancer.
     */
    @Updatable
    public LoadBalancerAccessLog getAccessLog() {
        if (accessLog == null) {
            accessLog = newSubresource(LoadBalancerAccessLog.class);
        }

        return accessLog;
    }

    public void setAccessLog(LoadBalancerAccessLog accessLog) {
        this.accessLog = accessLog;
    }

    /**
     * Connection Draining setting for Load Balancer.
     */
    @Updatable
    public LoadBalancerConnectionDraining getConnectionDraining() {
        if (connectionDraining == null) {
            connectionDraining = newSubresource(LoadBalancerConnectionDraining.class);
        }

        return connectionDraining;
    }

    public void setConnectionDraining(LoadBalancerConnectionDraining connectionDraining) {
        this.connectionDraining = connectionDraining;
    }

    /**
     * Cross Zone Load Balancing setting for the Load Balancer.
     */
    @Updatable
    public LoadBalancerCrossZoneLoadBalancing getCrossZoneLoadBalancing() {
        if (crossZoneLoadBalancing == null) {
            crossZoneLoadBalancing = newSubresource(LoadBalancerCrossZoneLoadBalancing.class);
        }

        return crossZoneLoadBalancing;
    }

    public void setCrossZoneLoadBalancing(LoadBalancerCrossZoneLoadBalancing crossZoneLoadBalancing) {
        this.crossZoneLoadBalancing = crossZoneLoadBalancing;
    }

    /**
     * The connection setting for the Load balancer.
     */
    @Updatable
    public LoadBalancerConnectionSettings getConnectionSettings() {
        if (connectionSettings == null) {
            connectionSettings = newSubresource(LoadBalancerConnectionSettings.class);
        }

        return connectionSettings;
    }

    public void setConnectionSettings(LoadBalancerConnectionSettings connectionSettings) {
        this.connectionSettings = connectionSettings;
    }

    @Override
    public void copyFrom(software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerAttributes loadBalancerAttributes) {
        LoadBalancerAccessLog accessLog = newSubresource(LoadBalancerAccessLog.class);
        accessLog.copyFrom(loadBalancerAttributes.accessLog());
        setAccessLog(accessLog);

        LoadBalancerCrossZoneLoadBalancing crossZoneLoadBalancing = newSubresource(LoadBalancerCrossZoneLoadBalancing.class);
        crossZoneLoadBalancing.copyFrom(loadBalancerAttributes.crossZoneLoadBalancing());
        setCrossZoneLoadBalancing(crossZoneLoadBalancing);

        LoadBalancerConnectionDraining connectionDraining = newSubresource(LoadBalancerConnectionDraining.class);
        connectionDraining.copyFrom(loadBalancerAttributes.connectionDraining());
        setConnectionDraining(connectionDraining);

        LoadBalancerConnectionSettings connectionSettings = newSubresource(LoadBalancerConnectionSettings.class);
        connectionSettings.copyFrom(loadBalancerAttributes.connectionSettings());
        setConnectionSettings(connectionSettings);
    }

    @Override
    public String primaryKey() {
        return "load balancer attribute";
    }

    software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerAttributes toLoadBalancerAttributes(boolean overrideConnectionDraining) {
        return software.amazon.awssdk.services.elasticloadbalancing.model.LoadBalancerAttributes.builder()
            .crossZoneLoadBalancing(getCrossZoneLoadBalancing().toCrossZoneLoadBalancing())
            .connectionSettings(getConnectionSettings().toConnectionSettings())
            .connectionDraining(getConnectionDraining().toConnectionDraining(overrideConnectionDraining))
            .accessLog(getAccessLog().toAccessLog())
            .build();
    }
}
