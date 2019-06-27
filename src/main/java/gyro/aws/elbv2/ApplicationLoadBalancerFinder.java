package gyro.aws.elbv2;

import gyro.core.Type;

/**
 * Query application load balancers.
 *
 * .. code-block:: gyro
 *
 *    alb: $(aws::application-load-balancer EXTERNAL/* | arn = '') -or-
 *    alb: $(aws::application-load-balancer EXTERNAL/* | name = '')
 */
@Type("application-load-balancer")
public class ApplicationLoadBalancerFinder extends LoadBalancerFinder<ApplicationLoadBalancerResource> {
}
