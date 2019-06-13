package gyro.aws.elbv2;

import gyro.core.Type;

/**
 * Query network load balancer listeners.
 *
 * .. code-block:: gyro
 *
 *    nlb-listener: $(aws::nlb-listener EXTERNAL/* | arn = '')
 */
@Type("nlb-listener")
public class NetworkLoadBalancerListenerFinder extends ListenerFinder<NetworkLoadBalancerListenerResource> {
}
