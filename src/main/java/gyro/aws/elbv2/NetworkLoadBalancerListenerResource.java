package gyro.aws.elbv2;

import gyro.core.Type;
import gyro.core.resource.Resource;
import gyro.core.resource.Updatable;

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
 *     aws::nlb-listener listener-example
 *         port: "80"
 *         protocol: "TCP"
 *         nlb: $(aws::nlb nlb-example | load-balancer)
 *
 *         default-action
 *             target-group-arn: $(aws::target-group target-group-example | target-group-arn)
 *             type: "forward"
 *         end
 *     end
 */

@Type("nlb-listener")
public class NetworkLoadBalancerListenerResource extends ListenerResource {

    private NetworkActionResource defaultAction;
    private NetworkLoadBalancerResource nlb;

    /**
     *  The default action associated with the listener (Optional)
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
    public boolean refresh() {
        Listener listener = super.internalRefresh();

        if (listener != null) {
            setDefaultAction(fromDefaultActions(listener.defaultActions()));
            setNlb(findById(NetworkLoadBalancerResource.class, listener.loadBalancerArn()));
            return true;
        }

        return false;
    }

    @Override
    public void create() {
        ElasticLoadBalancingV2Client client = createClient(ElasticLoadBalancingV2Client.class);

        CreateListenerResponse response =
                client.createListener(r -> r.certificates(Certificate.builder().certificateArn(getDefaultCertificate()).build())
                        .defaultActions(toDefaultActions())
                        .loadBalancerArn(getNlb().getArn())
                        .port(getPort())
                        .protocol(getProtocol())
                        .sslPolicy(getSslPolicy()));

        setArn(response.listeners().get(0).listenerArn());
    }

    @Override
    public void update(Resource current, Set<String> changedFieldNames) {
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
    public void delete() {
        super.delete();
    }

    @Override
    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        if (getArn() != null) {
            sb.append("nlb listener " + getArn());
        } else {
            sb.append("nlb listener ");
        }
        return sb.toString();
    }

    private Action toDefaultActions() {
        return Action.builder()
                .type(getDefaultAction().getType())
                .targetGroupArn(getDefaultAction().getTargetGroup().getArn())
                .build();
    }

    private NetworkActionResource fromDefaultActions(List<Action> defaultAction) {
        NetworkActionResource actionResource = new NetworkActionResource();

        for (Action action : defaultAction) {
            actionResource.setTargetGroup(findById(TargetGroupResource.class, action.targetGroupArn()));
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
