package gyro.aws.elbv2;

import gyro.aws.Copyable;
import gyro.core.GyroUI;
import gyro.core.Type;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;

import gyro.core.scope.State;
import org.apache.commons.lang.NotImplementedException;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Action;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Certificate;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.CreateListenerResponse;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.Listener;

import java.util.List;
import java.util.Set;

/**
 *
 * Example
 * -------
 *
 * .. code-block:: gyro
 *
 *     aws::network-load-balancer-listener listener-example
 *         port: "80"
 *         protocol: "TCP"
 *         nlb: $(aws::network-load-balancer nlb-example | load-balancer)
 *
 *         default-action
 *             target-group-arn: $(aws::load-balancer-target-group target-group-example | target-group-arn)
 *             type: "forward"
 *         end
 *     end
 */

@Type("network-load-balancer-listener")
public class NetworkLoadBalancerListenerResource extends ListenerResource implements Copyable<Listener> {

    private NetworkActionResource defaultAction;
    private NetworkLoadBalancerResource nlb;

    /**
     *  The default action associated with the listener. (Required)
     *
     *  @subresource gyro.aws.elbv2.ActionResource
     */
    @Updatable
    public NetworkActionResource getDefaultAction() {
        return defaultAction;
    }

    public void setDefaultAction(NetworkActionResource defaultAction) {
        this.defaultAction = defaultAction;
    }

    /**
     *  The nlb that the listener is attached to. (Required)
     **/
    public NetworkLoadBalancerResource getNlb() {
        return nlb;
    }

    public void setNlb(NetworkLoadBalancerResource nlb) {
        this.nlb = nlb;
    }

    @Override
    public void copyFrom(Listener listener) {
        setDefaultAction(fromDefaultActions(listener.defaultActions()));
        setNlb(findById(NetworkLoadBalancerResource.class, listener.loadBalancerArn()));
    }

    @Override
    public boolean refresh() {
        Listener listener = super.internalRefresh();

        if (listener != null) {

            this.copyFrom(listener);

            return true;
        }

        return false;
    }

    @Override
    public void create(GyroUI ui, State state) {
        throw new NotImplementedException();
    }

    @Override
    public void update(GyroUI ui, State state, Resource current, Set<String> changedFieldNames) {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);

        if (getDefaultCertificate() == null && getProtocol().equals("TCP")) {
            client.modifyListener(r -> r.certificates(Certificate.builder().certificateArn(getDefaultCertificate()).build())
                    .defaultActions(toDefaultActions())
                    .listenerArn(getArn())
                    .port(getPort())
                    .protocol(getProtocol())
                    .sslPolicy(null));
        } else {

            client.modifyListener(r -> r.certificates(Certificate.builder().certificateArn(getDefaultCertificate()).build())
                    .defaultActions(toDefaultActions())
                    .listenerArn(getArn())
                    .port(getPort())
                    .protocol(getProtocol())
                    .sslPolicy(getSslPolicy()));
        }
    }

    @Override
    public void delete(GyroUI ui, State state) {
        super.delete(ui, state);
    }

    private Action toDefaultActions() {
            return Action.builder()
                    .type(getDefaultAction() != null ? getDefaultAction().getType() : null)
                    .targetGroupArn(getDefaultAction() != null ? getDefaultAction().getTargetGroup().getArn() : null)
                    .build();
    }

    private NetworkActionResource fromDefaultActions(List<Action> defaultAction) {
        NetworkActionResource actionResource = new NetworkActionResource();

        for (Action action : defaultAction) {
            if (action.targetGroupArn() != null) {
                actionResource.setTargetGroup(findById(TargetGroupResource.class, action.targetGroupArn()));
            }
            actionResource.setType(action.typeAsString());
        }

        return actionResource;
    }

    public void updateDefaultAction() {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);
        client.modifyListener(r -> r.certificates(toCertificates())
                .defaultActions(toDefaultActions())
                .listenerArn(getArn())
                .port(getPort())
                .protocol(getProtocol())
                .sslPolicy(getSslPolicy()));
    }
}
