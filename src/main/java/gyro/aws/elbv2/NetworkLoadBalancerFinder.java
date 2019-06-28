package gyro.aws.elbv2;

import gyro.core.Type;

/**
 * Query network load balancers.
 *
 * .. code-block:: gyro
 *
 *    nlb: $(aws::network-load-balancer EXTERNAL/* | arn = '') -or-
 *    nlb: $(aws::network-load-balancer EXTERNAL/* | name = '')
 */
@Type("network-load-balancer")
public class NetworkLoadBalancerFinder extends LoadBalancerFinder<NetworkLoadBalancerResource> {
}
