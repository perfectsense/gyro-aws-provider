package gyro.aws.elbv2;

import gyro.core.Type;

/**
 * Query application load balancers.
 *
 * .. code-block:: gyro
 *
 *    alb: $(external-query aws::application-load-balancer { arn: ''})
 *    alb: $(external-query aws::application-load-balancer { name: ''})
 */
@Type("application-load-balancer")
public class ApplicationLoadBalancerFinder extends LoadBalancerFinder<ApplicationLoadBalancerResource> {
}
