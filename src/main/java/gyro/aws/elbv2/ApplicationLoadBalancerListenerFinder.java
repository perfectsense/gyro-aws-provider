package gyro.aws.elbv2;

import gyro.core.Type;

/**
 * Query application load balancer listeners.
 *
 * .. code-block:: gyro
 *
 *    alb-listener: $(aws::application-load-balancer-listener EXTERNAL/* | arn = '')
 */
@Type("application-load-balancer-listener")
public class ApplicationLoadBalancerListenerFinder extends ListenerFinder<ApplicationLoadBalancerListenerResource> {
}
