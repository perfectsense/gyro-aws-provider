package gyro.aws.elbv2;

import gyro.core.Type;

/**
 * Query application load balancer listeners.
 *
 * .. code-block:: gyro
 *
 *    alb-listener: $(aws::alb-listener EXTERNAL/* | arn = '')
 */
@Type("alb-listener")
public class ApplicationLoadBalancerListenerFinder extends ListenerFinder<ApplicationLoadBalancerListenerResource> {
}
