package gyro.aws.elbv2;

import gyro.aws.Copyable;
import gyro.core.resource.Diffable;
import gyro.core.resource.Updatable;

import software.amazon.awssdk.services.elasticloadbalancingv2.model.TargetGroup;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     health-check
 *         interval: "90"
 *         path: "/"
 *         port: "traffic-port"
 *         protocol: "HTTP"
 *         timeout: "30"
 *         healthy-threshold: "2"
 *         matcher: "200"
 *         unhealthy-threshold: "2"
 *     end
 */

public class HealthCheck extends Diffable implements Copyable<TargetGroup> {

    private Integer interval;
    private String path;
    private String port;
    private String protocol;
    private Integer timeout;
    private Integer healthyThreshold;
    private String matcher;
    private Integer unhealthyThreshold;

    /**
     *  The approximate amount of time between health checks of a target. (Optional)
     */
    @Updatable
    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    /**
     *  The ping path destination on targets for health checks. (Optional)
     */
    @Updatable
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     *  The port used when an alb performs health checks on targets. Required when used with ``instance`` and ``ip`` target types. (Optional)
     */
    @Updatable
    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    /**
     *  The port used when an alb performs health checks on targets. (Optional)
     */
    @Updatable
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     *  The amount of time, in seconds, an unresponsive target means a failed health check. (Optional)
     */
    @Updatable
    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    /**
     *  Health check successes required for an unhealthy target to be considered healthy. (Optional)
     */
    @Updatable
    public Integer getHealthyThreshold() {
        return healthyThreshold;
    }

    public void setHealthyThreshold(Integer healthyThreshold) {
        this.healthyThreshold = healthyThreshold;
    }

    /**
     *  HTTP code that signals a successful response from a target. (Optional)
     */
    @Updatable
    public String getMatcher() {
        return matcher;
    }

    public void setMatcher(String matcher) {
        this.matcher = matcher;
    }

    /**
     *  Health check failures required by an unhealthy target to be considered unhealthy. (Optional)
     */
    @Updatable
    public Integer getUnhealthyThreshold() {
        return unhealthyThreshold;
    }

    public void setUnhealthyThreshold(Integer unhealthyThreshold) {
        this.unhealthyThreshold = unhealthyThreshold;
    }

    public String toDisplayString() {
        return "health check";
    }

    public String primaryKey() {
        return getPath();
    }

    @Override
    public void copyFrom(TargetGroup targetGroup) {
        setInterval(targetGroup.healthCheckIntervalSeconds());
        setPath(targetGroup.healthCheckPath());
        setPort(targetGroup.healthCheckPort());
        setProtocol(targetGroup.healthCheckProtocolAsString());
        setTimeout(targetGroup.healthCheckTimeoutSeconds());
        setHealthyThreshold(targetGroup.healthyThresholdCount());
        if (targetGroup.matcher() != null) {
            setMatcher(targetGroup.matcher().httpCode());
        }
        setUnhealthyThreshold(targetGroup.unhealthyThresholdCount());
    }
}
