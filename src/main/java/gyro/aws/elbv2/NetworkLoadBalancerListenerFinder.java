package gyro.aws.elbv2;

import gyro.core.Type;

/**
 * Query network load balancer listeners.
 *
 * .. code-block:: gyro
 *
 *    nlb-listener: $(aws::network-load-balancer-listener EXTERNAL/* | arn = '')
 */
@Type("network-load-balancer-listener")
public class NetworkLoadBalancerListenerFinder extends ListenerFinder<NetworkLoadBalancerListenerResource> {
}
