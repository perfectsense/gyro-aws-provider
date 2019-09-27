package gyro.aws.elbv2;

import gyro.core.Type;

/**
 * Query network load balancer listeners.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    nlb-listener: $(external-query aws::network-load-balancer-listener { arn: ''})
 */
@Type("network-load-balancer-listener")
public class NetworkLoadBalancerListenerFinder extends ListenerFinder<NetworkLoadBalancerListenerResource> {
}
