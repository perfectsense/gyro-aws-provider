package gyro.aws.elbv2;

import gyro.core.Type;

/**
 * Query application load balancers.
 *
 * .. code-block:: gyro
 *
 *    alb: $(aws::alb EXTERNAL/* | arn = '') -or-
 *    alb: $(aws::alb EXTERNAL/* | name = '')
 */
@Type("alb")
public class ApplicationLoadBalancerFinder extends LoadBalancerFinder<ApplicationLoadBalancerResource> {
}
