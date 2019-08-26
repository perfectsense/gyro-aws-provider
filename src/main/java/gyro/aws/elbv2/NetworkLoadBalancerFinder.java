package gyro.aws.elbv2;

import gyro.core.Type;

/**
 * Query network load balancers.
 *
 * .. code-block:: gyro
 *
 *    nlb: $(external-query aws::network-load-balancer { arn: ''})
 *    nlb: $(external-query aws::network-load-balancer { name: ''})
 */
@Type("network-load-balancer")
public class NetworkLoadBalancerFinder extends LoadBalancerFinder<NetworkLoadBalancerResource> {
}
