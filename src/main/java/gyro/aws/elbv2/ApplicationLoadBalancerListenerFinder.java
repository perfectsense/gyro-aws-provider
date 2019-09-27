package gyro.aws.elbv2;

import gyro.core.Type;

/**
 * Query application load balancer listeners.
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *    alb-listener: $(external-query aws::application-load-balancer-listener { arn: ''})
 */
@Type("application-load-balancer-listener")
public class ApplicationLoadBalancerListenerFinder extends ListenerFinder<ApplicationLoadBalancerListenerResource> {
}
