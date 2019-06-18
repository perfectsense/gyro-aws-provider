package gyro.aws.elbv2;

import gyro.core.Type;

/**
 * Query network load balancers.
 *
 * .. code-block:: gyro
 *
 *    nlb: $(aws::nlb EXTERNAL/* | arn = '') -or-
 *    nlb: $(aws::nlb EXTERNAL/* | name = '')
 */
@Type("nlb")
public class NetworkLoadBalancerFinder extends LoadBalancerFinder<NetworkLoadBalancerResource> {
}
